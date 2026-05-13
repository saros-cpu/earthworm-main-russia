package com.earthworm.config;

import com.earthworm.service.CourseService;

public class UserContext {
    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();

    public static void setUserId(String userId) {
        userIdHolder.set(userId);
    }

    public static void clear() {
        userIdHolder.remove();
    }

    public static String getUserId() {
        String uid = userIdHolder.get();
        if (uid != null) return uid;
        return CourseService.DEV_USER_ID;
    }
}

