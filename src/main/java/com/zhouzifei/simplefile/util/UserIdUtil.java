package com.zhouzifei.simplefile.util;

import com.zhouzifei.tool.consts.StorageTypeConst;

import java.util.Objects;

/**
 * 用户ID工具类
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
public class UserIdUtil {

    public static final Long ZERO_LONG = 0L;

    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    private static ThreadLocal<String> storageType = new ThreadLocal<>();

    /**
     * 设置当前登录的用户ID
     *
     * @param value
     */
    public static void setUserId(Long value) {
        threadLocal.set(value);
    }

    /**
     * 获取当前登录的用户ID
     *
     * @return
     */
    public static Long getUserId() {
        Long value = threadLocal.get();
        if (Objects.isNull(value)) {
            return ZERO_LONG;
        }
        return value;
    }
    /**
     * 设置当前登录的用户ID
     *
     * @param value
     */
    public static void setStorageType(String value) {
        storageType.set(value);
    }

    /**
     * 获取当前登录的用户ID
     *
     * @return
     */
    public static String getStorageType() {
        String value = storageType.get();
        if (Objects.isNull(value)) {
            return StorageTypeConst.LOCAL.getStorageType();
        }
        return value;
    }
}
