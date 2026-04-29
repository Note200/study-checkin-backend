package com.studycheckin.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycheckin.backend.entity.CircleComment;
import com.studycheckin.backend.entity.CircleLike;
import com.studycheckin.backend.entity.CirclePost;
import com.studycheckin.backend.entity.User;
import com.studycheckin.backend.mapper.CircleCommentMapper;
import com.studycheckin.backend.mapper.CircleLikeMapper;
import com.studycheckin.backend.mapper.CirclePostMapper;
import com.studycheckin.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CircleService {

    @Autowired
    private CirclePostMapper postMapper;

    @Autowired
    private CircleLikeMapper likeMapper;

    @Autowired
    private CircleCommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 发布动态
     */
    public CirclePost publish(Long userId, String content, Integer type) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("内容不能为空");
        }
        if (content.length() > 500) {
            throw new RuntimeException("内容不能超过500字");
        }
        CirclePost post = new CirclePost();
        post.setUserId(userId);
        post.setContent(content.trim());
        post.setType(type != null ? type : 0);
        postMapper.insert(post);
        return post;
    }

    /**
     * 删除动态（只能删除自己的）
     */
    public void delete(Long postId, Long userId) {
        LambdaQueryWrapper<CirclePost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CirclePost::getId, postId).eq(CirclePost::getUserId, userId);
        int deleted = postMapper.delete(wrapper);
        if (deleted == 0) {
            throw new RuntimeException("动态不存在或无权删除");
        }
        // 同时删除该动态的点赞和评论
        likeMapper.delete(new LambdaQueryWrapper<CircleLike>().eq(CircleLike::getPostId, postId));
        commentMapper.delete(new LambdaQueryWrapper<CircleComment>().eq(CircleComment::getPostId, postId));
    }

    /**
     * 点赞/取消点赞
     */
    public void toggleLike(Long userId, Long postId) {
        LambdaQueryWrapper<CircleLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CircleLike::getUserId, userId).eq(CircleLike::getPostId, postId);
        CircleLike existing = likeMapper.selectOne(wrapper);
        if (existing != null) {
            likeMapper.deleteById(existing.getId());
        } else {
            CircleLike like = new CircleLike();
            like.setUserId(userId);
            like.setPostId(postId);
            likeMapper.insert(like);
        }
    }

    /**
     * 发表评论
     */
    public CircleComment addComment(Long userId, Long postId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }
        CircleComment comment = new CircleComment();
        comment.setUserId(userId);
        comment.setPostId(postId);
        comment.setContent(content.trim());
        commentMapper.insert(comment);
        return comment;
    }

    /**
     * 获取评论列表
     */
    public List<Map<String, Object>> getComments(Long postId) {
        LambdaQueryWrapper<CircleComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CircleComment::getPostId, postId).orderByAsc(CircleComment::getCreateTime);
        List<CircleComment> comments = commentMapper.selectList(wrapper);

        Set<Long> userIds = comments.stream().map(CircleComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (CircleComment c : comments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("content", c.getContent());
            item.put("createTime", c.getCreateTime());
            item.put("userId", c.getUserId());
            User author = userMap.get(c.getUserId());
            item.put("nickname", author != null ? author.getNickname() : "未知用户");
            item.put("avatar", author != null ? author.getAvatar() : null);
            result.add(item);
        }
        return result;
    }

    /**
     * 获取班级圈动态列表（按时间倒序）
     */
    public List<Map<String, Object>> listPosts(Long userId) {
        User user = userMapper.selectById(userId);
        Long classId = user != null ? user.getClassId() : null;

        LambdaQueryWrapper<CirclePost> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CirclePost::getCreateTime).last("LIMIT 50");
        List<CirclePost> posts = postMapper.selectList(wrapper);

        Set<Long> userIds = posts.stream().map(CirclePost::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        // 查询当前用户的点赞记录
        Set<Long> likedPostIds = new HashSet<>();
        if (!posts.isEmpty()) {
            List<Long> postIds = posts.stream().map(CirclePost::getId).collect(Collectors.toList());
            LambdaQueryWrapper<CircleLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(CircleLike::getUserId, userId).in(CircleLike::getPostId, postIds);
            likeMapper.selectList(likeWrapper).forEach(l -> likedPostIds.add(l.getPostId()));
        }

        // 查询每个动态的点赞数和评论数
        List<Map<String, Object>> result = new ArrayList<>();
        for (CirclePost post : posts) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", post.getId());
            item.put("content", post.getContent());
            item.put("type", post.getType());
            item.put("createTime", post.getCreateTime());
            item.put("canDelete", post.getUserId().equals(userId));

            User author = userMap.get(post.getUserId());
            item.put("authorId", post.getUserId());
            item.put("authorName", author != null ? author.getNickname() : "未知用户");
            item.put("authorAvatar", author != null ? author.getAvatar() : null);
            item.put("sameClass", classId != null && classId.equals(author != null ? author.getClassId() : null));

            // 点赞信息
            item.put("liked", likedPostIds.contains(post.getId()));
            Long likeCount = likeMapper.selectCount(
                new LambdaQueryWrapper<CircleLike>().eq(CircleLike::getPostId, post.getId()));
            item.put("likeCount", likeCount);

            // 评论数
            Long commentCount = commentMapper.selectCount(
                new LambdaQueryWrapper<CircleComment>().eq(CircleComment::getPostId, post.getId()));
            item.put("commentCount", commentCount);

            result.add(item);
        }
        return result;
    }
}
