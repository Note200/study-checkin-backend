package com.studycheckin.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studycheckin.backend.entity.CheckinRecord;
import com.studycheckin.backend.entity.CheckinTask;
import com.studycheckin.backend.mapper.CheckinRecordMapper;
import com.studycheckin.backend.mapper.CheckinTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinTaskMapper taskMapper;
    private final CheckinRecordMapper recordMapper;

    // ===== 打卡任务 =====

    public List<CheckinTask> listTasksByUser(Long userId) {
        return new com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<CheckinTaskMapper, CheckinTask>() {}
                .lambdaQuery().eq(CheckinTask::getUserId, userId).list();
    }

    public CheckinTask addTask(CheckinTask task, Long userId) {
        task.setUserId(userId);
        taskMapper.insert(task);
        return task;
    }

    public void deleteTask(Long taskId, Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinTask> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinTask::getId, taskId).eq(CheckinTask::getUserId, userId);
        taskMapper.delete(wrapper);
    }

    // ===== 打卡记录 =====

    public CheckinRecord doCheckin(Long taskId, Long userId, String remark) {
        // 检查今天是否已打卡
        LocalDate today = LocalDate.now();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getTaskId, taskId)
               .eq(CheckinRecord::getUserId, userId)
               .eq(CheckinRecord::getCheckinDate, today);
        if (recordMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("今天已经打卡了");
        }
        CheckinRecord record = new CheckinRecord();
        record.setTaskId(taskId);
        record.setUserId(userId);
        record.setCheckinDate(today);
        record.setRemark(remark == null ? "" : remark);
        recordMapper.insert(record);
        return record;
    }

    public List<CheckinRecord> listRecords(Long taskId, Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getTaskId, taskId)
               .eq(CheckinRecord::getUserId, userId)
               .orderByDesc(CheckinRecord::getCheckinDate);
        return recordMapper.selectList(wrapper);
    }

    /** 获取用户打卡统计（总天数、连续天数） */
    public Map<String, Object> getStats(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId);
        long total = recordMapper.selectCount(wrapper);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDays", total);
        return stats;
    }
}
