package com.studycheckin.backend.controller;

import com.studycheckin.backend.common.Result;
import com.studycheckin.backend.service.CircleService;
import com.studycheckin.backend.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/circle")
public class CircleController {

    @Autowired
    private CircleService circleService;

    /** 获取动态列表 */
    @GetMapping("/list")
    public Result<?> list() {
        try {
            return Result.ok(circleService.listPosts(UserContext.getUserId()));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 发布动态 */
    @PostMapping("/publish")
    public Result<?> publish(@RequestBody Map<String, Object> body) {
        String content = (String) body.get("content");
        Integer type = body.get("type") != null ? Integer.valueOf(body.get("type").toString()) : 0;
        try {
            return Result.ok(circleService.publish(UserContext.getUserId(), content, type));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 删除动态 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            circleService.delete(id, UserContext.getUserId());
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 点赞/取消点赞 */
    @PostMapping("/like")
    public Result<?> like(@RequestBody Map<String, Object> body) {
        Long postId = Long.valueOf(body.get("postId").toString());
        try {
            circleService.toggleLike(UserContext.getUserId(), postId);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 发表评论 */
    @PostMapping("/comment")
    public Result<?> comment(@RequestBody Map<String, Object> body) {
        Long postId = Long.valueOf(body.get("postId").toString());
        String content = (String) body.get("content");
        try {
            return Result.ok(circleService.addComment(UserContext.getUserId(), postId, content));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 获取评论列表 */
    @GetMapping("/comments/{postId}")
    public Result<?> comments(@PathVariable Long postId) {
        try {
            return Result.ok(circleService.getComments(postId));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
