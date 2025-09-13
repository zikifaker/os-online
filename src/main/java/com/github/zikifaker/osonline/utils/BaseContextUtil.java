package com.github.zikifaker.osonline.utils;

public class BaseContextUtil {

    public static ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    public static void setCurrentUserId(Integer userId) {
        threadLocal.set(userId);
    }

    public static Integer getCurrentUserId() {
        return threadLocal.get();
    }

    public static void removeCurrentUserId() {
        threadLocal.remove();
    }
}
