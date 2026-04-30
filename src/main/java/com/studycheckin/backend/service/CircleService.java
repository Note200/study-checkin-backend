package com.studycheckin.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studycheckin.backend.entity.CirclePost;
import com.studycheckin.backend.entity.CircleLike;
import com.studycheckin.backend.entity.CircleComment;
import com.studycheckin.backend.entity.User;
import com.studycheckin.backend.mapper.CirclePostMapper;
import com.studycheckin.backend.mapper.CircleLikeMapper;
import com.studycheckin.backend.mapper.CircleCommentMapper;
import com.studycheckin.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CircleService extends ServiceImpl<CirclePostMapper, CirclePost> {

    private final CircleLikeMapper likeMapper;
    private final CircleCommentMapper commentMapper;
    private final UserMapper userMapper;

    /** 发布动态 */
    public void publish(CirclePost post, Long userId) {
        post.setUserId(userId);
        post.setLikeCount(0);
        post.setCommentCount(0);
        save(post);
    }

    /** 获取动态列表（带用户信息 + 当前用户是否点赞） */
    public List<Map<String, Object>> listPosts(Long currentUserId) {
        List<CirclePost> posts = lambdaQuery().orderByDesc(CirclePost::getCreateTime).list();
        if (posts.isEmpty()) return Collections.emptyList();

        // 收集所有 userId
        Set<Long> userIds = posts.stream().map(CirclePost::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        // 查询当前用户的点赞
        Set<Long> likedPostIds = new HashSet<>();
        if (currentUserId != null) {
            List<CircleLike> likes = likeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CircleLike>()
                    .eq("user_id", currentUserId)
                    .in("post_id", posts.stream().map(CirclePost::getId).collect(Collectors.toList()))
            );
            likedPostIds = likes.stream().map(CircleLike::getPostId).collect(Collectors.toSet());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (CirclePost post : posts) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", post.getId());
            map.put("content", post.getContent());
            map.put("type", post.getType());
            map.put("likeCount", post.getLikeCount());
            map.put("commentCount", post.getCommentCount());
            map.put("createTime", post.getCreateTime());
            map.put("userId", post.getUserId());
            map.put("liked", likedPostIds.contains(post.getId()));

            User author = userMap.get(post.getUserId());
            if (author != null) {
                map.put("authorName", author.getNickname());
                map.put("authorAvatar", author.getAvatar());
                // 是否同班（简单判断）
                if (currentUserId != null) {
                    User current = userMapper.selectById(currentUserId);
                    map.put("sameClass", current != null && current.getClassId() != null
                        && current.getClassId().equals(author.getClassId()));
                }
            }
            // 是否可删除（作者本人或管理员）
            map.put("canDelete", post.getUserId().equals(currentUserId));

            result.add(map);
        }
        return result;
    }

    /** 点赞/取消点赞 */
    public boolean toggleLike(Long postId, Long userId) {
        CircleLike existing = likeMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CircleLike>()
                .eq("post_id", postId).eq("user_id", userId));
        if (existing != null) {
            likeMapper.deleteById(existing.getId());
            lambdaUpdate().eq(CirclePost::getId, postId)
                .setSql("like_count = like_count - 1").update();
            return false; // 取消点赞
        } else {
            CircleLike like = new CircleLike();
            like.setPostId(postId);
            like.setUserId(userId);
            likeMapper.insert(like);
            lambdaUpdate().eq(CirclePost::getId, postId)
                .setSql("like_count = like_count + 1").update();
            return true; // 点赞
        }
    }

    /** 评论 */
    public void comment(Long postId, Long userId, String content) {
        CircleComment comment = new CircleComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        commentMapper.insert(comment);
        lambdaUpdate().eq(CirclePost::getId, postId)
            .setSql("comment_count = comment_count + 1").update();
    }

    /** 获取评论列表 */
    public List<Map<String, Object>> getComments(Long postId) {
        List<CircleComment> comments = commentMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CircleComment>()
                .eq("post_id", postId).orderByAsc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (CircleComment c : comments) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("content", c.getContent());
            map.put("createTime", c.getCreateTime());
            map.put("userId", c.getUserId());
            User user = userMapper.selectById(c.getUserId());
            if (user != null) {
                map.put("nickname", user.getNickname());
                map.put("avatar", user.getAvatar());
            }
            result.add(map);
        }
        return result;
    }

    /** 删除动态（仅作者） */
    public boolean deletePost(Long postId, Long userId) {
        CirclePost post = getById(postId);
        if (post == null || !post.getUserId().equals(userId)) return false;
        // 删除关联数据
        likeMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CircleLike>()
            .eq("post_id", postId));
        commentMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CircleComment>()
            .eq("post_id", postId));
        removeById(postId);
        return true;
    }
}
