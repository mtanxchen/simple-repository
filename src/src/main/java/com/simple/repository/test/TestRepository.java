package com.simple.repository.test;

import com.simple.repository.test.entity.TestEntity;
import com.simple.repository.master.BaseRepository;

public class TestRepository extends BaseRepository<TestEntity> {

    public TestRepository(){
        this.setEntityClass(TestEntity.class);
    }

}
