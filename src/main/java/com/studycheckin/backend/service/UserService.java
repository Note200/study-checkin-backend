package com.studycheckin.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studycheckin.backend.entity.User;
import com.studycheckin.backend.mapper.UserMapper;
import com.studycheckin.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final JwtUtil jwtUtil;

    @Value("${wx.appid}")
    private String appid;

    @Value("${wx.secret}")
    private String secret;

    /**
     * 微信登录：用code换openid，自动注册/登录，返回token
     */
    public Map<String, Object> wxLogin(String code) {
        // 调微信接口换取openid，用String接收避免content-type不匹配
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid
                + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
        RestTemplate restTemplate = new RestTemplate();
        String responseStr = restTemplate.getForObject(url, String.class);
        Map result;
        try {
            result = new ObjectMapper().readValue(responseStr, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("解析微信响应失败：" + responseStr);
        }
        String openid = (String) result.get("openid");
        if (openid == null) {
            throw new RuntimeException("微信登录失败：" + result.get("errmsg"));
        }

        // 查找或创建用户
        User user = lambdaQuery().eq(User::getOpenid, openid).one();
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("用户" + openid.substring(0, 6));
            user.setRole(0);
            user.setStatus(0);
            save(user);
        }

        if (user.getStatus() == 1) {
            throw new RuntimeException("账号已被禁用");
        }

        String token = jwtUtil.createToken(user.getId(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", user);
        return data;
    }

    /**
     * 更新用户信息（昵称、头像、专业）
     */
    public void updateProfile(Long userId, String nickname, String avatar, String major) {
        User user = new User();
        user.setId(userId);
        if (nickname != null) user.setNickname(nickname);
        if (avatar != null) user.setAvatar(avatar);
        if (major != null) user.setMajor(major);
        updateById(user);
    }

    /**
     * 加入班级（通过邀请码）
     */
    public void joinClass(Long userId, Long classId) {
        User user = new User();
        user.setId(userId);
        user.setClassId(classId);
        updateById(user);
    }
}
