package com.studycheckin.backend.controller;

import com.studycheckin.backend.common.Result;
import com.studycheckin.backend.entity.Course;
import com.studycheckin.backend.service.CourseService;
import com.studycheckin.backend.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /** 获取我的课程表 */
    @GetMapping("/list")
    public Result<List<Course>> list(
            @RequestParam(required = false) Integer weekDay,
            @RequestParam(required = false) Integer week) {
        Long userId = UserContext.getUserId();
        if (weekDay != null) {
            // 列表视图：按星期筛选
            return Result.ok(courseService.listByUserAndWeekDay(userId, weekDay));
        }
        // 格子视图：返回全部
        return Result.ok(courseService.listByUser(userId));
    }

    /** 添加课程 */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Course course) {
        courseService.addCourse(course, UserContext.getUserId());
        return Result.ok(course);
    }

    /** 删除课程 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        courseService.deleteCourse(id, UserContext.getUserId());
        return Result.ok();
    }

    /** 修改课程 */
    @PostMapping("/update")
    public Result<?> update(@RequestBody Course course) {
        courseService.updateCourse(course, UserContext.getUserId());
        return Result.ok();
    }

    /** 获取单个课程详情 */
    @GetMapping("/{id}")
    public Result<Course> detail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        Course course = courseService.getById(id);
        if (course == null || !course.getUserId().equals(userId)) {
            return Result.fail("课程不存在");
        }
        return Result.ok(course);
    }

    /** 获取今日课程 */
    @GetMapping("/today")
    public Result<List<Course>> today() {
        return Result.ok(courseService.listTodayByUser(UserContext.getUserId()));
    }
}
