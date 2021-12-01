package com.zhouzifei.simplefile.util;

import java.util.Objects;

/**
 * 用户ID工具类
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
public class StorageUtil {

    public static final String ZERO_LONG = "local";

    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前登录的用户ID
     *
     * @param value
     */
    public static void set(String value) {
        threadLocal.set(value);
    }

    /**
     * 获取当前登录的用户ID
     *
     * @return
     */
    public static String get() {
        String value = threadLocal.get();
        if (Objects.isNull(value)) {
            return ZERO_LONG;
        }
        return value;
    }

}
