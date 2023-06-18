package com.simple.repository.master.cache;

import com.simple.repository.connect.RedisSession;
import com.simple.repository.master.Entity;
import com.simple.repository.util.SimpleJson;
import com.simple.repository.util.SimpleStringUtils;

import java.io.IOException;

/**
 * 缓存管理
 * <p>
 *  该版本使用redis进行缓存管理
 * </p>
 *
 * @author laiqx
 * @date 2023-01-16
 */
public class SimpleRedisCacheManager<T extends Entity> implements SimpleCacheManager<T> {
    public SimpleRedisCacheManager() throws IOException {
        RedisSession.open();
    }

    /**
     * 保存
     * @param t 保存对象
     * @return 是否保存成功
     */
    public boolean save(T t) {
        if (null == t.id) {
            return false;
        }
        String key = SIMPLE_DATA_PATH + t.getClass().getName() + ":" + t.id;
        return RedisSession.set(key, SimpleJson.toJsonString(t), DEFAULT_EXPIRES_SEC);
    }

    /**
     * 查询对象
     * @param tClass 对象类
     * @param id 对象主键id
     * @return 返回对象
     */
    @Override
    public T get(Class<T> tClass, Number id) {
        String key = SIMPLE_DATA_PATH + tClass.getName() + ":" + id;
        String json = RedisSession.get(key);
        if (SimpleStringUtils.isEmpty(json)) {
            return null;
        }
        return SimpleJson.toObject(json, tClass);
    }

    @Override
    public void remove(Class<T> tClass, Number id) {
        String key = SIMPLE_DATA_PATH + tClass.getName() + ":" + id;
        RedisSession.remove(key);
    }

    @Override
    public void remove(Class<T> tClass) {
        String key = SIMPLE_DATA_PATH + tClass.getName();
        RedisSession.remove(key);
    }

    /**
     * entity 缓存保存路径
     */
    private static final String SIMPLE_DATA_PATH = "simple:repository:";

    /**
     * 默认过期时间
     */
    private static final long DEFAULT_EXPIRES_SEC = 24 * 60 * 60L;
}
