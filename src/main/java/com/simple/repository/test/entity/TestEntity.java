package com.simple.repository.test.entity;

import com.simple.repository.master.Entity;
import java.sql.Timestamp;

public class TestEntity extends Entity<Integer> {

    /**
     * 
     */
    public String name;

    /**
     * 
     */
    public Integer age;

    /**
     * 
     */
    public Timestamp createTime;

    /**
     * 
     */
    public Timestamp updateTime;

    public Class<?> getIdentityClass(){
        return Integer.class;
    }


    public static final String NAME = "name";

    public static final String AGE = "age";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String TABLE = "test";

}