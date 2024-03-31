package com.simple.repository.test.userEntity;

import com.simple.repository.master.Entity;

public class UserEntity extends Entity<Long> {

    public Class<?> getIdentityClass(){
        return Long.class;
    }


    public static final String TABLE = "user";

}