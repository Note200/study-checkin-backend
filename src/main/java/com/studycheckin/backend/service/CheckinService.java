package com.studycheckin.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studycheckin.backend.entity.CheckinRecord;
import com.studycheckin.backend.entity.CheckinTask;
import com.studycheckin.backend.mapper.CheckinRecordMapper;
import com.studycheckin.backend.mapper.CheckinTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinTaskMapper taskMapper;
    private final CheckinRecordMapper recordMapper;

    // ===== 打卡任务 =====

    public List<Map<String, Object>> listTasksByUser(Long userId) {
        List<CheckinTask> tasks = new com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<CheckinTaskMapper, CheckinTask>() {}
                .lambdaQuery().eq(CheckinTask::getUserId, userId).list();
        
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        
        for (CheckinTask task : tasks) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", task.getId());
            map.put("title", task.getTitle());
            map.put("type", task.getType());
            map.put("targetDays", task.getTargetDays());
            map.put("isPublic", task.getIsPublic());
            
            // 计算累计天数
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> countWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            countWrapper.eq(CheckinRecord::getTaskId, task.getId())
                        .eq(CheckinRecord::getUserId, userId);
            long totalDays = recordMapper.selectCount(countWrapper);
            map.put("totalDays", totalDays);
            
            // 检查今日是否已打卡
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> todayWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            todayWrapper.eq(CheckinRecord::getTaskId, task.getId())
                        .eq(CheckinRecord::getUserId, userId)
                        .eq(CheckinRecord::getCheckinDate, today);
            boolean todayChecked = recordMapper.selectCount(todayWrapper) > 0;
            map.put("todayChecked", todayChecked);
            
            // 计算连续打卡天数（简化版：查询最近连续打卡）
            long continueDays = calculateContinueDays(task.getId(), userId);
            map.put("continueDays", continueDays);
            
            // 计算进度
            int progress = task.getTargetDays() != null && task.getTargetDays() > 0 
                    ? (int) Math.min(100, totalDays * 100 / task.getTargetDays()) 
                    : 0;
            map.put("progress", progress);
            
            result.add(map);
        }
        return result;
    }
    
    /** 计算连续打卡天数 */
    private long calculateContinueDays(Long taskId, Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getTaskId, taskId)
               .eq(CheckinRecord::getUserId, userId)
               .orderByDesc(CheckinRecord::getCheckinDate);
        List<CheckinRecord> records = recordMapper.selectList(wrapper);
        
        if (records.isEmpty()) return 0;
        
        long continueDays = 0;
        LocalDate checkDate = LocalDate.now();
        
        for (CheckinRecord record : records) {
            if (record.getCheckinDate().equals(checkDate)) {
                continueDays++;
                checkDate = checkDate.minusDays(1);
            } else if (record.getCheckinDate().equals(checkDate.minusDays(1))) {
                // 如果今天还没打卡，但昨天打了，也算连续
                checkDate = record.getCheckinDate();
                continueDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        return continueDays;
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

    /** 获取某月已打卡的日期列表 */
    public List<Integer> getCalendarDays(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId)
               .ge(CheckinRecord::getCheckinDate, startDate)
               .le(CheckinRecord::getCheckinDate, endDate);
        List<CheckinRecord> records = recordMapper.selectList(wrapper);

        return records.stream()
                .map(r -> r.getCheckinDate().getDayOfMonth())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** 获取用户打卡统计 */
    public Map<String, Object> getStats(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> recordWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        recordWrapper.eq(CheckinRecord::getUserId, userId);
        long totalDays = recordMapper.selectCount(recordWrapper);

        // 统计今日已打卡数
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinTask> taskWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        taskWrapper.eq(CheckinTask::getUserId, userId);
        long totalTasks = taskMapper.selectCount(taskWrapper);

        // 今日已打卡数（今天有打卡记录的任务数）
        LocalDate today = LocalDate.now();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> todayWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        todayWrapper.eq(CheckinRecord::getUserId, userId)
                    .eq(CheckinRecord::getCheckinDate, today);
        long todayDone = recordMapper.selectCount(todayWrapper);

        // 计算完成率
        int rate = totalTasks > 0 ? (int) (todayDone * 100 / totalTasks) : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalTasks);
        stats.put("done", todayDone);
        stats.put("rate", rate);
        return stats;
    }

    /** 获取打卡历史记录 */
    public List<CheckinRecord> listHistory(Long userId, Long taskId, int limit) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId);
        if (taskId != null) {
            wrapper.eq(CheckinRecord::getTaskId, taskId);
        }
        wrapper.orderByDesc(CheckinRecord::getCheckinDate)
               .last("LIMIT " + limit);
        return recordMapper.selectList(wrapper);
    }
}
