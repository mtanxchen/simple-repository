package com.simple.repository.master;


import java.sql.Timestamp;

public class Entity<T extends Number> {

    public T id;

    public Timestamp createTime;

    public Timestamp updateTime;

    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public Class<?> getIdentityClass(){
        return null;
    }
}
