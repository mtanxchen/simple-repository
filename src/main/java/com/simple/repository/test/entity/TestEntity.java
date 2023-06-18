package com.simple.repository.test.entity;

import com.simple.repository.master.Entity;
import java.util.Date;

public class TestEntity extends Entity<Integer> {

    /**
     * 名称
     */
    public String name;

    /**
     * 年龄
     */
    public Integer age;

    /**
     * 创建时间
     */
    public Date createTime;

    /**
     * 修改时间
     */
    public Date updateTime;

    public static final String NAME = "name";

    public static final String AGE = "age";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public Class<?> getIdentityClass(){
        return Integer.class;
    }

}