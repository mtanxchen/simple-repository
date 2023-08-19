package com.simple.repository;

import com.simple.repository.master.search.Condition;
import com.simple.repository.master.search.Sort;
import com.simple.repository.test.TestRepository;
import com.simple.repository.test.entity.TestEntity;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 条件对象测试
 *
 * @author laiqx
 * @date 2023-05-29
 */
public class ConditionTest {

    private final static Logger log = LoggerFactory.getLogger(ConditionTest.class);

    @Test
    public void test() throws IOException {
        SimpleStart.run(new String[]{"--simple.env=dev"});
        Condition idCondition = new Condition().in(TestEntity.ID, Arrays.asList(1, 2)).or().eq(TestEntity.ID, 18);
        Condition condition = new Condition().eq(TestEntity.NAME, "张三").or().paren(idCondition);
        List<TestEntity> entities = new TestRepository().list(condition);
        log.info("条件对象测试: 测试条件查询结果;entities={}", entities);
    }

    @Test
    public void sort() throws IOException {
        SimpleStart.run(new String[]{"--simple.env=dev"});
        Condition idCondition = new Condition().in(TestEntity.ID, Arrays.asList(1, 2)).or().eq(TestEntity.ID, 18);
        Condition condition = new Condition().eq(TestEntity.NAME, "张三").or().paren(idCondition)
                .putSort(TestEntity.NAME, Sort.SortType.asc).putSort(TestEntity.AGE, Sort.SortType.desc);
        new TestRepository().list(condition);
        new TestRepository().delete(new Condition().eq(TestEntity.ID,null));
    }


}
