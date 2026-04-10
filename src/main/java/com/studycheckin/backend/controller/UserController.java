package com.studycheckin.backend.controller;

import com.studycheckin.backend.common.Result;
import com.studycheckin.backend.entity.User;
import com.studycheckin.backend.service.ClassesService;
import com.studycheckin.backend.service.UserService;
import com.studycheckin.backend.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ClassesService classesService;

    /** 微信登录 */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isEmpty()) return Result.fail("code不能为空");
        try {
            return Result.ok(userService.wxLogin(code));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 获取当前用户信息 */
    @GetMapping("/info")
    public Result<User> info() {
        Long userId = UserContext.getUserId();
        return Result.ok(userService.getById(userId));
    }

    /** 更新用户资料 */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody Map<String, String> body) {
        Long userId = UserContext.getUserId();
        userService.updateProfile(userId, body.get("nickname"), body.get("avatar"), body.get("major"));
        return Result.ok();
    }

    /** 通过邀请码加入班级 */
    @PostMapping("/joinClass")
    public Result<?> joinClass(@RequestBody Map<String, String> body) {
        String code = body.get("inviteCode");
        var cls = classesService.getByInviteCode(code);
        if (cls == null) return Result.fail("邀请码无效");
        userService.joinClass(UserContext.getUserId(), cls.getId());
        return Result.ok(cls);
    }
}
