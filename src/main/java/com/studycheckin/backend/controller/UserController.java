package com.studycheckin.backend.controller;

import com.studycheckin.backend.common.Result;
import com.studycheckin.backend.entity.User;
import com.studycheckin.backend.service.ClassesService;
import com.studycheckin.backend.service.UserService;
import com.studycheckin.backend.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ClassesService classesService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080}")
    private String baseUrl;

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

    /** 账号密码登录 */
    @PostMapping("/loginByPassword")
    public Result<?> loginByPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return Result.fail("用户名和密码不能为空");
        }
        try {
            return Result.ok(userService.loginByPassword(username, password));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 账号密码注册 */
    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nickname = body.get("nickname");
        
        if (username == null || username.isEmpty()) return Result.fail("用户名不能为空");
        if (password == null || password.isEmpty()) return Result.fail("密码不能为空");
        if (nickname == null || nickname.isEmpty()) nickname = username;
        
        try {
            return Result.ok(userService.register(username, password, nickname));
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

    /** 修改密码（需验证旧密码） */
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.fail("旧密码和新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.fail("新密码长度不能少于6位");
        }
        try {
            userService.changePassword(UserContext.getUserId(), oldPassword, newPassword);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 上传头像 */
    @PostMapping("/avatar")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.fail("请选择图片");
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf(".")) : ".png";
        String fileName = "avatar_" + UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            File dest = new File(dir, fileName);
            file.transferTo(dest);
            String url = baseUrl + "/uploads/" + fileName;
            // 更新用户头像
            Long userId = UserContext.getUserId();
            userService.updateAvatar(userId, url);
            return Result.ok(url);
        } catch (Exception e) {
            return Result.fail("上传失败：" + e.getMessage());
        }
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
