package com.studycheckin.backend.controller;

import com.studycheckin.backend.common.Result;
import com.studycheckin.backend.entity.CheckinTask;
import com.studycheckin.backend.entity.CheckinRecord;
import com.studycheckin.backend.service.CheckinService;
import com.studycheckin.backend.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    /** 获取我的打卡任务列表 */
    @GetMapping("/tasks")
    public Result<List<Map<String, Object>>> tasks() {
        return Result.ok(checkinService.listTasksByUser(UserContext.getUserId()));
    }

    /** 创建打卡任务 */
    @PostMapping("/task/add")
    public Result<?> addTask(@RequestBody CheckinTask task) {
        return Result.ok(checkinService.addTask(task, UserContext.getUserId()));
    }

    /** 删除打卡任务 */
    @DeleteMapping("/task/{id}")
    public Result<?> deleteTask(@PathVariable Long id) {
        checkinService.deleteTask(id, UserContext.getUserId());
        return Result.ok();
    }

    /** 执行打卡 */
    @PostMapping("/do")
    public Result<?> doCheckin(@RequestBody Map<String, Object> body) {
        Long taskId = Long.valueOf(body.get("taskId").toString());
        String remark = (String) body.get("remark");
        try {
            return Result.ok(checkinService.doCheckin(taskId, UserContext.getUserId(), remark));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 撤销今日打卡 */
    @PostMapping("/undo")
    public Result<?> undoCheckin(@RequestBody Map<String, Object> body) {
        Long taskId = Long.valueOf(body.get("taskId").toString());
        try {
            checkinService.undoCheckin(taskId, UserContext.getUserId());
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 获取某任务的打卡记录 */
    @GetMapping("/records/{taskId}")
    public Result<List<CheckinRecord>> records(@PathVariable Long taskId) {
        return Result.ok(checkinService.listRecords(taskId, UserContext.getUserId()));
    }

    /** 获取本月打卡日历（已打卡的日期列表） */
    @GetMapping("/calendar")
    public Result<?> calendar(@RequestParam int year, @RequestParam int month) {
        return Result.ok(checkinService.getCalendarDays(UserContext.getUserId(), year, month));
    }

    /** 获取打卡统计 */
    @GetMapping("/stats")
    public Result<?> stats() {
        return Result.ok(checkinService.getStats(UserContext.getUserId()));
    }

    /** 获取打卡历史记录 */
    @GetMapping("/history")
    public Result<?> history(
            @RequestParam(required = false) Long taskId,
            @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(checkinService.listHistory(UserContext.getUserId(), taskId, limit));
    }

    /** 获取本周排行榜（班级内） */
    @GetMapping("/rank")
    public Result<?> rank() {
        return Result.ok(checkinService.getWeekRank(UserContext.getUserId()));
    }

    /** 本周每日学习时长（首页柱状图） */
    @GetMapping("/week")
    public Result<?> week() {
        return Result.ok(checkinService.getWeekHours(UserContext.getUserId()));
    }

    /** 本月打卡日历（热力图） - 返回day列表供前端用 */
    @GetMapping("/calendar/days")
    public Result<?> calendarDays(@RequestParam int year, @RequestParam int month) {
        return Result.ok(checkinService.getCalendarDayList(UserContext.getUserId(), year, month));
    }
}
