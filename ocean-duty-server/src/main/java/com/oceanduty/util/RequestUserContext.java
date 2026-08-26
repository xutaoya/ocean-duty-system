package com.oceanduty.util;

import com.oceanduty.common.domain.RequestUser;

/**
 * 登录用户 ThreadLocal 上下文
 */
public final class RequestUserContext {

    private static final ThreadLocal<RequestUser> HOLDER = new ThreadLocal<>();

    private RequestUserContext() {
    }

    public static void set(RequestUser user) {
        HOLDER.set(user);
    }

    public static RequestUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
