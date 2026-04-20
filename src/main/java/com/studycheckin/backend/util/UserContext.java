package com.studycheckin.backend.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class UserContext {

    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<Integer> roleHolder = new ThreadLocal<>();

    public static void set(Long userId, Integer role) {
        userIdHolder.set(userId);
        roleHolder.set(role);
    }

    public static Long getUserId() {
        return userIdHolder.get();
    }

    public static Integer getRole() {
        return roleHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
        roleHolder.remove();
    }
}
