package com.simple.repository.master.cache;

/**
 * 缓存管理
 *
 * @author laiqx
 * @date 2023-02-10
 */
public interface SimpleCacheManager<T> {

    boolean save(T t);

    T get(Class<T> tClass, Number id);

    void remove(Class<T> tClass, Number id);

    void remove(Class<T> tClass);
}
