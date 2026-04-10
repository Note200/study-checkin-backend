package com.studycheckin.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studycheckin.backend.entity.StudyPlan;
import com.studycheckin.backend.mapper.StudyPlanMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudyPlanService extends ServiceImpl<StudyPlanMapper, StudyPlan> {

    public List<StudyPlan> listByUserAndDate(Long userId, LocalDate date) {
        return lambdaQuery()
                .eq(StudyPlan::getUserId, userId)
                .eq(StudyPlan::getPlanDate, date)
                .orderByAsc(StudyPlan::getCreateTime)
                .list();
    }

    public void addPlan(StudyPlan plan, Long userId) {
        plan.setUserId(userId);
        plan.setStatus(0);
        save(plan);
    }

    public void finishPlan(Long id, Long userId) {
        lambdaUpdate()
                .eq(StudyPlan::getId, id)
                .eq(StudyPlan::getUserId, userId)
                .set(StudyPlan::getStatus, 1)
                .update();
    }

    public void deletePlan(Long id, Long userId) {
        lambdaUpdate()
                .eq(StudyPlan::getId, id)
                .eq(StudyPlan::getUserId, userId)
                .remove();
    }
}
