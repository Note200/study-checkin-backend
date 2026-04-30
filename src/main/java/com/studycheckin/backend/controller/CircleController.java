package com.studycheckin.backend.controller;

import com.studycheckin.backend.common.Result;
import com.studycheckin.backend.entity.CirclePost;
import com.studycheckin.backend.service.CircleService;
import com.studycheckin.backend.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/circle")
@RequiredArgsConstructor
public class CircleController {

    private final CircleService circleService;

    /** 获取动态列表 */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        Long userId = UserContext.getUserId();
        return Result.ok(circleService.listPosts(userId));
    }

    /** 发布动态 */
    @PostMapping("/publish")
    public Result<Void> publish(@RequestBody CirclePost post) {
        Long userId = UserContext.getUserId();
        circleService.publish(post, userId);
        return Result.ok();
    }

    /** 点赞/取消点赞 */
    @PostMapping("/like")
    public Result<Boolean> like(@RequestBody Map<String, Long> body) {
        Long userId = UserContext.getUserId();
        Long postId = body.get("postId");
        boolean liked = circleService.toggleLike(postId, userId);
        return Result.ok(liked);
    }

    /** 评论 */
    @PostMapping("/comment")
    public Result<Void> comment(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getUserId();
        Long postId = Long.valueOf(body.get("postId").toString());
        String content = body.get("content").toString();
        circleService.comment(postId, userId, content);
        return Result.ok();
    }

    /** 获取评论列表 */
    @GetMapping("/comments/{postId}")
    public Result<List<Map<String, Object>>> comments(@PathVariable Long postId) {
        return Result.ok(circleService.getComments(postId));
    }

    /** 删除动态 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        boolean ok = circleService.deletePost(id, userId);
        return ok ? Result.ok() : Result.fail("删除失败");
    }
}
