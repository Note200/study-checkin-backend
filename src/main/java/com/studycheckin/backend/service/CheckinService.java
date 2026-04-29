package com.studycheckin.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studycheckin.backend.entity.CheckinRecord;
import com.studycheckin.backend.entity.CheckinTask;
import com.studycheckin.backend.entity.User;
import com.studycheckin.backend.mapper.CheckinRecordMapper;
import com.studycheckin.backend.mapper.CheckinTaskMapper;
import com.studycheckin.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinTaskMapper taskMapper;
    private final CheckinRecordMapper recordMapper;

    @Autowired
    private UserMapper userMapper;

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
        // 先级联删除该任务的所有打卡记录
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> recordWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        recordWrapper.eq(CheckinRecord::getTaskId, taskId);
        recordMapper.delete(recordWrapper);
        // 再删除任务本身
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

    /** 撤销今日打卡 */
    public void undoCheckin(Long taskId, Long userId) {
        LocalDate today = LocalDate.now();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getTaskId, taskId)
               .eq(CheckinRecord::getUserId, userId)
               .eq(CheckinRecord::getCheckinDate, today);
        int deleted = recordMapper.delete(wrapper);
        if (deleted == 0) {
            throw new RuntimeException("今天还没有打卡记录");
        }
    }

    public List<CheckinRecord> listRecords(Long taskId, Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getTaskId, taskId)
               .eq(CheckinRecord::getUserId, userId)
               .orderByDesc(CheckinRecord::getCheckinDate);
        return recordMapper.selectList(wrapper);
    }

    /** 获取某月打卡详情（日期+次数），用于热力图 */
    public List<Map<String, Object>> getCalendarDays(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId)
               .ge(CheckinRecord::getCheckinDate, startDate)
               .le(CheckinRecord::getCheckinDate, endDate);
        List<CheckinRecord> records = recordMapper.selectList(wrapper);

        // 按日期分组，统计每天打卡次数
        Map<Integer, Long> dayCountMap = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCheckinDate().getDayOfMonth(),
                        Collectors.counting()
                ));

        // 找出最大打卡次数
        long maxCount = dayCountMap.values().stream().max(Long::compare).orElse(1);
        if (maxCount == 0) maxCount = 1;

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : dayCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("day", entry.getKey());
            item.put("count", entry.getValue());
            // 计算热力等级 0-4
            int level = (int) Math.ceil((double) entry.getValue() / maxCount * 4);
            if (level > 4) level = 4;
            item.put("level", level);
            result.add(item);
        }
        return result;
    }

    /** 获取用户打卡统计 */
    public Map<String, Object> getStats(Long userId) {
        LocalDate today = LocalDate.now();

        // 累计打卡总天数（去重：有打卡记录的不同日期数）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> distinctWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        distinctWrapper.eq(CheckinRecord::getUserId, userId)
                       .select(CheckinRecord::getCheckinDate)
                       .groupBy(CheckinRecord::getCheckinDate);
        List<CheckinRecord> distinctRecords = recordMapper.selectList(distinctWrapper);
        long totalDays = distinctRecords.size();

        // 计算连续打卡天数（跨所有任务）
        long streakDays = calculateUserStreak(userId);

        // 本月打卡天数
        LocalDate monthStart = today.withDayOfMonth(1);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> monthWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        monthWrapper.eq(CheckinRecord::getUserId, userId)
                    .ge(CheckinRecord::getCheckinDate, monthStart)
                    .le(CheckinRecord::getCheckinDate, today)
                    .select(CheckinRecord::getCheckinDate)
                    .groupBy(CheckinRecord::getCheckinDate);
        long monthDays = recordMapper.selectList(monthWrapper).size();

        // 总任务数
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinTask> taskWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        taskWrapper.eq(CheckinTask::getUserId, userId);
        long totalTasks = taskMapper.selectCount(taskWrapper);

        // 今日完成的任务数
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> todayWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        todayWrapper.eq(CheckinRecord::getUserId, userId)
                    .eq(CheckinRecord::getCheckinDate, today)
                    .select(CheckinRecord::getTaskId)
                    .groupBy(CheckinRecord::getTaskId);
        long todayDone = recordMapper.selectList(todayWrapper).size();
        int rate = totalTasks > 0 ? (int) (todayDone * 100 / totalTasks) : 0;

        // 累计学习时长（每个打卡记录算30分钟）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> allWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        allWrapper.eq(CheckinRecord::getUserId, userId);
        long totalRecords = recordMapper.selectCount(allWrapper);
        double totalHours = Math.round(totalRecords * 30 / 60.0 * 10) / 10.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDays", totalDays);
        stats.put("streakDays", streakDays);
        stats.put("monthDays", monthDays);
        stats.put("totalTasks", totalTasks);
        stats.put("totalHours", totalHours);
        // 兼容首页使用的字段
        stats.put("total", totalTasks);
        stats.put("done", todayDone);
        stats.put("rate", rate);
        return stats;
    }

    /** 计算用户跨所有任务的连续打卡天数 */
    private long calculateUserStreak(Long userId) {
        // 查询用户所有打卡记录的日期，按日期降序
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId)
               .select(CheckinRecord::getCheckinDate)
               .groupBy(CheckinRecord::getCheckinDate)
               .orderByDesc(CheckinRecord::getCheckinDate);
        List<CheckinRecord> records = recordMapper.selectList(wrapper);

        if (records.isEmpty()) return 0;

        long streak = 0;
        LocalDate checkDate = LocalDate.now();

        for (CheckinRecord record : records) {
            if (record.getCheckinDate().equals(checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else if (record.getCheckinDate().equals(checkDate.minusDays(1))) {
                // 今天还没打卡，但从昨天开始连续
                checkDate = record.getCheckinDate();
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
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

    // ===== 排行榜 =====

    /**
     * 获取本周班级排行榜
     * 按本周打卡次数排名
     */
    public List<Map<String, Object>> getWeekRank(Long currentUserId) {
        // 获取本周一的日期
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 获取当前用户的班级
        User currentUser = userMapper.selectById(currentUserId);
        Long classId = currentUser != null ? currentUser.getClassId() : null;

        // 查询本周的打卡记录
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.ge(CheckinRecord::getCheckinDate, weekStart)
               .le(CheckinRecord::getCheckinDate, today);
        List<CheckinRecord> weekRecords = recordMapper.selectList(wrapper);

        // 按 userId 分组统计打卡次数
        Map<Long, Long> checkinCounts = weekRecords.stream()
                .collect(Collectors.groupingBy(CheckinRecord::getUserId, Collectors.counting()));

        // 查询相关用户信息
        Set<Long> userIds = checkinCounts.keySet();
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        // 组装排名列表并排序
        List<Map<String, Object>> rankList = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : checkinCounts.entrySet()) {
            Long userId = entry.getKey();
            Long count = entry.getValue();
            User user = userMap.get(userId);

            // 如果指定了班级，只返回同班同学
            if (classId != null && user != null && !classId.equals(user.getClassId())) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("userId", userId);
            item.put("nickname", user != null ? user.getNickname() : "未知用户");
            item.put("avatar", user != null ? user.getAvatar() : null);
            item.put("checkinCount", count);
            item.put("isMe", userId.equals(currentUserId));
            rankList.add(item);
        }

        // 按打卡次数降序排序
        rankList.sort((a, b) -> ((Long) b.get("checkinCount")).compareTo((Long) a.get("checkinCount")));

        // 加上排名序号
        for (int i = 0; i < rankList.size(); i++) {
            rankList.get(i).put("rank", i + 1);
        }

        return rankList;
    }

    /**
     * 本周每日学习时长（小时），前端柱状图用
     * 返回 [{day: "周一", hours: 2.5}, ...] 共7条
     */
    public List<Map<String, Object>> getWeekHours(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 查询本周所有打卡记录
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId)
               .ge(CheckinRecord::getCheckinDate, weekStart)
               .le(CheckinRecord::getCheckinDate, today);
        List<CheckinRecord> records = recordMapper.selectList(wrapper);

        // 按日期分组：同一天完成的任务去重
        Map<LocalDate, Set<Long>> dailyTasks = records.stream()
                .collect(Collectors.groupingBy(
                    CheckinRecord::getCheckinDate,
                    Collectors.mapping(CheckinRecord::getTaskId, Collectors.toSet())
                ));

        // 每个打卡任务默认 30 分钟
        final int MINUTES_PER_TASK = 30;

        String[] dayNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            if (date.isAfter(today)) {
                result.add(Map.of("day", dayNames[i], "hours", 0));
                continue;
            }
            Set<Long> tasksDone = dailyTasks.getOrDefault(date, Collections.emptySet());
            double hours = Math.round((tasksDone.size() * MINUTES_PER_TASK / 60.0) * 10) / 10.0;
            result.add(Map.of("day", dayNames[i], "hours", hours));
        }
        return result;
    }

    /**
     * 本月打卡日历数据（热力图用）
     * 返回 [{day: 1, count: 2, level: 1}, ...]
     */
    public List<Map<String, Object>> getCalendarData(Long userId, int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        // 不查未来的数据
        LocalDate today = LocalDate.now();
        if (monthEnd.isAfter(today)) monthEnd = today;

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId)
               .ge(CheckinRecord::getCheckinDate, monthStart)
               .le(CheckinRecord::getCheckinDate, monthEnd);
        List<CheckinRecord> records = recordMapper.selectList(wrapper);

        // 按天分组
        Map<Integer, Long> dayCounts = records.stream()
                .collect(Collectors.groupingBy(
                    r -> r.getCheckinDate().getDayOfMonth(),
                    Collectors.counting()
                ));

        // 找出最大打卡数（用于计算 level）
        long maxCount = dayCounts.values().stream().max(Long::compare).orElse(0L);

        List<Map<String, Object>> result = new ArrayList<>();
        for (int d = 1; d <= monthStart.lengthOfMonth(); d++) {
            LocalDate date = LocalDate.of(year, month, d);
            if (date.isAfter(today)) break;
            long count = dayCounts.getOrDefault(d, 0L);
            int level = 0;
            if (maxCount > 0 && count > 0) {
                level = count >= maxCount ? 4 : (int) ((count * 3.0 / maxCount) + 0.5);
                level = Math.min(level, 4);
            }
            Map<String, Object> item = new HashMap<>();
            item.put("day", d);
            item.put("count", count);
            item.put("level", level);
            result.add(item);
        }
        return result;
    }

    /**
     * 本月已打卡日期列表（返回日期字符串数组，供 stats.js 使用）
     * 返回 ["2026-04-01", "2026-04-05", ...]
     */
    public List<String> getCalendarDayList(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId)
               .ge(CheckinRecord::getCheckinDate, startDate)
               .le(CheckinRecord::getCheckinDate, endDate)
               .select(CheckinRecord::getCheckinDate)
               .groupBy(CheckinRecord::getCheckinDate);
        List<CheckinRecord> records = recordMapper.selectList(wrapper);

        return records.stream()
                .map(r -> r.getCheckinDate().toString())
                .collect(Collectors.toList());
    }
}
