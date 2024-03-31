package com.simple.repository.master.cache;

import com.simple.repository.master.Entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存管理
 * <p>
 * 本地缓存
 * </p>
 *
 * @author laiqx
 * date 2023-01-16
 */
public class SimpleInnerCacheManager<T extends Entity> implements SimpleCacheManager<T> {
    public Map<Number, T> cacheManager = new ConcurrentHashMap<>();

    public boolean save(T t) {
        if (null == t.id) {
            return false;
        }
        cacheManager.put(t.id, t);
        if (CACHE_SIZE > cacheManager.size()) {
            cacheManager.remove(0, 10000);
        }
        return true;
    }

    public T get(Class<T> tClass, Number id) {
        T t = cacheManager.remove(id);
        if (null != t) {
            cacheManager.put(id, t);
        }
        return t;
    }

    @Override
    public void remove(Class<T> tClass, Number id) {
        cacheManager.remove(id);
    }

    @Override
    public void remove(Class<T> tClass) {
        cacheManager = new ConcurrentHashMap<>();
    }

    private static final Integer CACHE_SIZE = 10000;
}
