package com.simple.repository;

import com.simple.repository.config.SimpleConfig;
import com.simple.repository.connect.RedisSession;
import com.simple.repository.connect.SimpleDataSource;
import com.simple.repository.generate.SimpleGenerate;
import com.simple.repository.generate.SimpleSqlConstGenerate;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试类
 *
 * @author laiqx
 * @date 2023-04-23
 */
public class SimpleTest {

    private final static Logger log = LoggerFactory.getLogger(SimpleDataSource.class);

    /**
     * 代码生成测试
     */
    @Test
    public void GenerateTest() throws IOException {
        log.info("simple代码生成测试");
        SimpleConfig.setRunEnv(new String[]{"--simple.env=dev"});
        new SimpleGenerate().run();
        new SimpleSqlConstGenerate().run();
        log.info("simple代码生成测试完成");
    }

    /**
     * redis 测试
     */
    @Test
    public void redisTest() throws IOException {
        RedisSession.open();
        String key = "test:1231";
        RedisSession.set(key, "1232", 10);
        System.out.println("setValue:" + RedisSession.list(key, "1231"));
        System.out.println("getValue:" + RedisSession.get(key));
        RedisSession.incr(key);
        System.out.println("incr:" + RedisSession.get(key));
        System.out.println("setNx:" + RedisSession.setNx(key, "666", 10));
        System.out.println("setNx2:" + RedisSession.setNx(key + "122", "666", 10));
        RedisSession.remove(key);


        String hsetKey = "test:haseKey";
        RedisSession.setHash(hsetKey, "name", "张三");
        Map<String, String> map = new HashMap<>();
        map.put("age", "10");
        map.put("remark", "remark");
        RedisSession.setHash(hsetKey, map);
        System.out.println("hSet:" + RedisSession.getHash(hsetKey, "age"));
        RedisSession.removeHash(hsetKey, "age");
        System.out.println("removeHash:" + RedisSession.getHash(hsetKey, "age"));
        String setKey = "test:setKey";
        RedisSession.addSet(setKey, "5");
        RedisSession.addSet(setKey, "4");
        System.out.println("listSet:" + RedisSession.getSet(setKey));
        RedisSession.removeSet(setKey, "4");
        System.out.println("removeSet:" + RedisSession.getSet(setKey));
    }
//
//    /**
//     * 数据操作测试
//     */
//    @Test
//    public void dataTest() throws IOException {
//        SimpleStart.run(new String[]{"--simple.env=dev"});
//        TestRepository testRepository = new TestRepository();
//        TestEntity entity = new TestEntity();
//        entity.age = 18;
//        entity.name = "张三";
//        testRepository.add(entity);
//        log.info("测试新增数据完成;entity={}", SimpleJson.toJsonString(entity));
//        /* 测试批量新增 */
//        TestEntity entity2 = new TestEntity();
//        entity.age = 19;
//        entity.name = "李四";
//        Integer oldCount = testRepository.count(new Condition().isNotCondition());
//        testRepository.add(Arrays.asList(entity2, entity));
//        log.info("测试新增批量新增数据完成;原数据量={},当前数据量={}", oldCount, testRepository.count(new Condition().isNotCondition()));
//        // 测试id查询
//        log.info("测试id查询完成;entity={}", testRepository.get(entity.id));
//        // 测试条件查询
//        Condition condition = new Condition().eq("age", 19);
//        log.info("测试条件查询完成;condition={},entity={}", SimpleJson.toJsonString(condition), testRepository.get(condition));
//        // 测试批量查询
//        log.info("测试条件查询完成:listIds={}", SimpleJson.toJsonString(testRepository.list(Arrays.asList(1, 2))));
//        log.info("测试条件查询完成:listCondition={}", SimpleJson.toJsonString(testRepository.list(new Condition().in(TestEntity.ID, Arrays.asList(1, 2)))));
//        log.info("分页数据查询：pageList={}", SimpleJson.toJsonString(testRepository.list(new BaseSearch())));
//        // 更新测试
//        entity.age = 20;
//        testRepository.update(entity);
//        entity.id = 4;
//        entity2.id = 5;
//        entity2.age = 30;
//        testRepository.update(Arrays.asList(entity2, entity));
//        entity.name = "全部更新";
//        testRepository.update(entity, new Condition().isNotCondition());
////        // 全量更新(空值更新)
//        TestEntity allUpdateEntity = new TestEntity();
//        allUpdateEntity.id = 1;
//        allUpdateEntity.name = "你好";
//        testRepository.updateAll(allUpdateEntity);
//        // 批量更新
//        List<TestEntity> allUpdateEntities = new ArrayList<>();
//        for(int i =1;i<5;i++){
//            allUpdateEntity = new TestEntity();
//            allUpdateEntity.id = i+1;
//            allUpdateEntity.name = "你好"+i;
//            allUpdateEntities.add(allUpdateEntity);
//        }
//        testRepository.updateAll(allUpdateEntities);
//
//        // 条件更新
//        allUpdateEntity = new TestEntity();
//        allUpdateEntity.name ="百岁老人";
//        testRepository.updateAll(allUpdateEntity,new Condition().le(TestEntity.ID,10));
//
//
//        // sql索引查询
//        TestSearch testSearch = new TestSearch();
//        testSearch.ids = Arrays.asList("1", "2");
//        log.info("sql索引查询：indexSelect={}", SimpleJson.toJsonString(testRepository.select(TestIndex.TEST_SELECT, testSearch, TestEntity.class)));
//        TestSearch search = new TestSearch();
//        search.name = "张三";
//        testRepository.select(TestIndex.TEST_SELECT,search,TestEntity.class);
//
//
//        // 删除测试
//        testRepository.delete(1);
//        testRepository.delete(new Condition().in(TestEntity.ID, Arrays.asList(1, 2)));
//        testRepository.delete(Arrays.asList(1, 2, 3));
//
//
//    }
//
//    @Test
//    public void transactionTest() throws SQLException, IOException {
//        SimpleStart.run(new String[]{"--simple.env=dev"});
//        TestRepository testRepository = new TestRepository();
//        TestEntity entity = new TestEntity();
//        entity.age = 18;
//        entity.name = "张五";
//        testRepository.add(entity);
//
//        SimpleSession.openSession().openTransaction();
//        SimpleStart.run(new String[]{"--simple.env=dev"});
//        entity = new TestEntity();
//        entity.age = 18;
//        entity.name = "李六";
//        testRepository.add(entity);
//        log.info("测试新增数据完成;entity={}", SimpleJson.toJsonString(entity));
//    }

}
