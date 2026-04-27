package com.studycheckin.backend.controller;

import com.studycheckin.backend.common.Result;
import com.studycheckin.backend.entity.Notice;
import com.studycheckin.backend.service.NoticeService;
import com.studycheckin.backend.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /** 获取最新一条公告（首页用） - 必须放在 {id} 之前 */
    @GetMapping("/latest")
    public Result<Notice> latest() {
        List<Notice> list = noticeService.listAll();
        return Result.ok(list.isEmpty() ? null : list.get(0));
    }

    /** 获取公告列表（所有人可访问） */
    @GetMapping("/list")
    public Result<List<Notice>> list() {
        return Result.ok(noticeService.listAll());
    }

    /** 发布公告（管理员） */
    @PostMapping("/publish")
    public Result<?> publish(@RequestBody Notice notice) {
        Integer role = UserContext.getRole();
        if (role == null || role != 1) return Result.fail(403, "无权限");
        noticeService.publish(notice, UserContext.getUserId());
        return Result.ok();
    }

    /** 删除公告（管理员） */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Integer role = UserContext.getRole();
        if (role == null || role != 1) return Result.fail(403, "无权限");
        noticeService.deleteNotice(id);
        return Result.ok();
    }
}
