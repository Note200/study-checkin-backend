package com.studycheckin.backend.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        String msg = e.getMessage();
        if ("无权限".equals(msg)) {
            return Result.fail(403, "无权限，请确认管理员身份");
        }
        return Result.fail(msg != null ? msg : "服务器异常");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.fail("服务器异常：" + e.getMessage());
    }
}
