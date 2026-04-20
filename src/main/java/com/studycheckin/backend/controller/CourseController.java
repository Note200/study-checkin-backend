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
    public Result<List<Course>> list() {
        return Result.ok(courseService.listByUser(UserContext.getUserId()));
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
    @PutMapping("/update")
    public Result<?> update(@RequestBody Course course) {
        courseService.updateById(course);
        return Result.ok();
    }

    /** 获取今日课程 */
    @GetMapping("/today")
    public Result<List<Course>> today() {
        return Result.ok(courseService.listTodayByUser(UserContext.getUserId()));
    }
}
