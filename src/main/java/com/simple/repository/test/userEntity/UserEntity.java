package com.simple.repository.test.userEntity;

import com.simple.repository.master.Entity;

public class UserEntity extends Entity<Integer> {

    /**
     * 用户名
     */
    public String name;

    public static final String NAME = "name";

    public Class<?> getIdentityClass(){
        return Integer.class;
    }

}