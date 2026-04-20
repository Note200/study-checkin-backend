package com.studycheckin.backend.controller;

import com.studycheckin.backend.common.Result;
import com.studycheckin.backend.entity.StudyPlan;
import com.studycheckin.backend.service.StudyPlanService;
import com.studycheckin.backend.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService planService;

    /** 获取某天的学习计划 */
    @GetMapping("/list")
    public Result<List<StudyPlan>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return Result.ok(planService.listByUserAndDate(UserContext.getUserId(), date));
    }

    /** 添加学习计划 */
    @PostMapping("/add")
    public Result<?> add(@RequestBody StudyPlan plan) {
        planService.addPlan(plan, UserContext.getUserId());
        return Result.ok(plan);
    }

    /** 完成计划 */
    @PutMapping("/{id}/finish")
    public Result<?> finish(@PathVariable Long id) {
        planService.finishPlan(id, UserContext.getUserId());
        return Result.ok();
    }

    /** 切换计划完成状态 */
    @PutMapping("/{id}/toggle")
    public Result<?> toggle(@PathVariable Long id) {
        planService.togglePlan(id, UserContext.getUserId());
        return Result.ok();
    }

    /** 删除计划 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        planService.deletePlan(id, UserContext.getUserId());
        return Result.ok();
    }
}
