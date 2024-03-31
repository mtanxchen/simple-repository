package com.simple.repository;

import com.simple.repository.config.SimpleConfig;
import com.simple.repository.connect.RedisSession;
import com.simple.repository.connect.SimpleDataSource;
import com.simple.repository.connect.SimpleSession;
import com.simple.repository.generate.SimpleGenerate;
import com.simple.repository.generate.SimpleSqlConstGenerate;
import com.simple.repository.test.TestRepository;
import com.simple.repository.test.entity.TestEntity;
import com.simple.repository.util.SimpleJson;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
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
     * redis连接池测试
     */
    @Test
    public void redisPoolTest(){
        String key = "test:1231";
        for(int i = 0;i<10;i++){
            RedisSession.set(key, "1232", 10);
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * redis 测试
     */
    @Test
    public void redisTest() throws IOException {
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


    @Test
    public void transactionTest() throws SQLException, IOException {
        SimpleStart.run(new String[]{"--simple.env=dev"});
        TestRepository testRepository = new TestRepository();
        TestEntity entity = new TestEntity();
        entity.age = 18;
        entity.name = "张五";
        testRepository.add(entity);

        SimpleSession.openSession().openTransaction();
        SimpleStart.run(new String[]{"--simple.env=dev"});
        entity = new TestEntity();
        entity.age = 18;
        entity.name = "李六";
        testRepository.add(entity);
        log.info("测试新增数据完成;entity={}", SimpleJson.toJsonString(entity));
    }

}
