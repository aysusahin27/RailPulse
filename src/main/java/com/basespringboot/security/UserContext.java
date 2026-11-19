package com.basespringboot.security;

import com.basespringboot.entity.UserEntity;

public class UserContext {

    private static final ThreadLocal<UserEntity> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUser(UserEntity user) {
        CURRENT_USER.set(user);
    }

    public static UserEntity getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
