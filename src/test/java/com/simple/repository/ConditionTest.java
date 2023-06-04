package com.simple.repository;

import com.simple.repository.master.search.Condition;
import com.simple.repository.test.TestRepository;
import com.simple.repository.test.entity.TestEntity;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConditionTest {

    @Test
    public void test() throws IOException {
        SimpleStart.run(new String[]{"--simple.env=dev"});
        Condition idCondition = new Condition().in(TestEntity.ID, Arrays.asList(1,2)).or().eq(TestEntity.ID,18);
        Condition condition = new Condition().eq(TestEntity.NAME, "张三").or().paren(idCondition);
        List<TestEntity> entities = new TestRepository().list(condition);
    }

}
