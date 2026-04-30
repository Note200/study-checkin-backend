package com.studycheckin.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studycheckin.backend.entity.Course;
import com.studycheckin.backend.mapper.CourseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    public List<Course> listByUser(Long userId) {
        return lambdaQuery().eq(Course::getUserId, userId).list();
    }

    public List<Course> listByUserAndWeekDay(Long userId, Integer weekDay) {
        return lambdaQuery()
                .eq(Course::getUserId, userId)
                .eq(Course::getWeekDay, weekDay)
                .orderByAsc(Course::getStartSection)
                .list();
    }

    public void addCourse(Course course, Long userId) {
        course.setUserId(userId);
        save(course);
    }

    public void deleteCourse(Long id, Long userId) {
        lambdaUpdate()
                .eq(Course::getId, id)
                .eq(Course::getUserId, userId)
                .remove();
    }

    public void updateCourse(Course course, Long userId) {
        // 校验课程归属：只能修改自己的课程
        Course existing = lambdaQuery()
                .eq(Course::getId, course.getId())
                .eq(Course::getUserId, userId)
                .one();
        if (existing == null) {
            throw new RuntimeException("课程不存在或无权修改");
        }
        // 不允许修改 userId
        course.setUserId(userId);
        updateById(course);
    }

    /** 获取今日课程 */
    public List<Course> listTodayByUser(Long userId) {
        // 获取今天是星期几 (1=周一, 7=周日)
        int dayOfWeek = java.time.LocalDate.now().getDayOfWeek().getValue();
        return lambdaQuery()
                .eq(Course::getUserId, userId)
                .eq(Course::getWeekDay, dayOfWeek)
                .orderByAsc(Course::getStartSection)
                .list();
    }
}
