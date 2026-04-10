package com.studycheckin.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycheckin.backend.common.Result;
import com.studycheckin.backend.entity.Classes;
import com.studycheckin.backend.entity.User;
import com.studycheckin.backend.service.ClassesService;
import com.studycheckin.backend.service.UserService;
import com.studycheckin.backend.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ClassesService classesService;

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
}
