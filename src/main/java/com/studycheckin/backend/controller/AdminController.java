package com.studycheckin.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycheckin.backend.common.Result;
import com.studycheckin.backend.entity.CheckinRecord;
import com.studycheckin.backend.entity.Classes;
import com.studycheckin.backend.entity.Notice;
import com.studycheckin.backend.entity.User;
import com.studycheckin.backend.mapper.CheckinRecordMapper;
import com.studycheckin.backend.mapper.NoticeMapper;
import com.studycheckin.backend.service.ClassesService;
import com.studycheckin.backend.service.UserService;
import com.studycheckin.backend.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ClassesService classesService;
    private final CheckinRecordMapper checkinRecordMapper;
    private final NoticeMapper noticeMapper;

    private void checkAdmin() {
        Integer role = UserContext.getRole();
        if (role == null || role != 1) throw new RuntimeException("无权限");
    }

    /** 获取所有用户 */
    @GetMapping("/users")
    public Result<List<User>> users() {
        checkAdmin();
        return Result.ok(userService.list());
    }

    /** 禁用/启用用户 */
    @PutMapping("/user/{id}/status")
    public Result<?> setStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        checkAdmin();
        User u = new User();
        u.setId(id);
        u.setStatus(body.get("status"));
        userService.updateById(u);
        return Result.ok();
    }

    /** 获取所有班级 */
    @GetMapping("/classes")
    public Result<List<Classes>> classes() {
        checkAdmin();
        return Result.ok(classesService.list());
    }

    /** 创建班级 */
    @PostMapping("/class/add")
    public Result<?> addClass(@RequestBody Map<String, String> body) {
        checkAdmin();
        return Result.ok(classesService.createClass(body.get("name"), UserContext.getUserId()));
    }

    /** 删除班级 */
    @DeleteMapping("/class/{id}")
    public Result<?> deleteClass(@PathVariable Long id) {
        checkAdmin();
        classesService.removeById(id);
        return Result.ok();
    }

    /** 获取班级成员 */
    @GetMapping("/class/{id}/members")
    public Result<List<User>> members(@PathVariable Long id) {
        checkAdmin();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getClassId, id);
        return Result.ok(userService.list(wrapper));
    }

    /** 获取统计数据 */
    @GetMapping("/stats")
    public Result<?> stats() {
        checkAdmin();
        Map<String, Object> data = new HashMap<>();

        // 用户总数
        data.put("userCount", userService.count());

        // 班级总数
        data.put("classCount", classesService.count());

        // 公告总数
        data.put("noticeCount", noticeMapper.selectCount(null));

        // 今日打卡总数
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getCheckinDate, java.time.LocalDate.now());
        data.put("todayCheckinCount", checkinRecordMapper.selectCount(wrapper));

        // 打卡记录总数
        data.put("totalCheckinCount", checkinRecordMapper.selectCount(null));

        return Result.ok(data);
    }

    /** 获取公告列表 */
    @GetMapping("/notices")
    public Result<List<Notice>> notices() {
        checkAdmin();
        return Result.ok(noticeMapper.selectList(
                new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getCreateTime)
        ));
    }

    /** 删除公告 */
    @DeleteMapping("/notice/{id}")
    public Result<?> deleteNotice(@PathVariable Long id) {
        checkAdmin();
        noticeMapper.deleteById(id);
        return Result.ok();
    }

    /** 生成班级邀请码 */
    @PostMapping("/invite-code")
    public Result<?> generateInviteCode(@RequestBody Map<String, Long> body) {
        checkAdmin();
        Long classId = body.get("classId");
        if (classId == null) return Result.fail("班级ID不能为空");

        // 生成6位邀请码
        String code = String.format("%06d", (int)(Math.random() * 1000000));

        // 更新班级的邀请码
        Classes cls = new Classes();
        cls.setId(classId);
        cls.setInviteCode(code);
        classesService.updateById(cls);

        return Result.ok(Map.of("inviteCode", code));
    }
}
