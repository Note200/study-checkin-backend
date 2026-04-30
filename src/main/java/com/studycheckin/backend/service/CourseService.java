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

    /** 获取今日课程（按当前教学周+星期过滤） */
    public List<Course> listTodayByUser(Long userId) {
        java.time.LocalDate today = java.time.LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue();

        // 计算当前教学周
        java.time.LocalDate semesterStart = java.time.LocalDate.of(2026, 2, 23);
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(semesterStart, today);
        int currentWeek = (int) (daysBetween / 7) + 1;
        if (currentWeek < 1) currentWeek = 1;

        List<Course> all = lambdaQuery()
                .eq(Course::getUserId, userId)
                .eq(Course::getWeekDay, dayOfWeek)
                .orderByAsc(Course::getStartSection)
                .list();

        // 按周次和周类型过滤
        return all.stream().filter(c -> {
            if (currentWeek < c.getStartWeek() || currentWeek > c.getEndWeek()) return false;
            int wt = c.getWeekType() != null ? c.getWeekType() : 0;
            if (wt == 0) return true;                           // 全部周
            if (wt == 1) return currentWeek % 2 == 1;           // 单周
            if (wt == 2) return currentWeek % 2 == 0;           // 双周
            if (wt == 3) return currentWeek <= 8;               // 前8周
            if (wt == 4) return currentWeek > 8;                // 后8周
            return true;
        }).collect(java.util.stream.Collectors.toList());
    }
}
