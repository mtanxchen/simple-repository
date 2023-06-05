package com.simple.repository.master;


import com.simple.repository.master.search.BaseSearch;
import com.simple.repository.master.search.Condition;

import java.util.List;
import java.util.Map;

public interface IBaseRepository<T> {

    /**
     * 新增
     *
     * @param entity 对象
     */
    void add(T entity);

    /**
     * 批量新增
     *
     * @param entities 对象集合
     */
    void add(List<T> entities);

    /**
     * 更新
     *
     * @param entity
     * @return
     */
    int update(T entity);

    /**
     * 批量更新
     * @param entities 对象集
     */
    void update(List<T> entities);

    /**
     * 条件更新
     *
     * @param condition 条件对象
     */
    void update(T entity, Condition condition);


    /**
     * 更新所有(包含null)
     *
     * @param entity
     * @return
     */
    int updateAll(T entity);

    /**
     * 批量更新所有(包含null)
     * @param entities 对象集
     */
    void updateAll(List<T> entities);

    /**
     * 条件更新所有(包含null)
     *
     * @param condition 条件对象
     */
    void updateAll(T entity, Condition condition);


    /**
     * 删除
     *
     * @param id 主键
     */
    void delete(Number id);

    /**
     * 批量删除
     *
     * @param ids 主键集合
     */
    void delete(List<? extends Number> ids);

    /**
     * 条件删除
     */
    void delete(Condition condition);

    /**
     * 查询
     *
     * @param id
     * @return
     */
    T get(Number id);

    /**
     * 条件查询对象
     *
     * @param condition
     * @return 返回对象
     */
    T get(Condition condition);

    /**
     * 查询列表
     *
     * @return
     */
    List<T> list(BaseSearch search);


    /**
     * 根据id集查询列表
     *
     * @return 返回对象列表
     */
    <E extends Number> List<T> list(List<E> ids);

    /**
     * 条件查询对象集合
     *
     * @param condition
     * @return 返回对象
     */
    List<T> list(Condition condition);

    /**
     * 统计数量
     *
     * @param condition
     * @return 返回数量
     */
    Integer count(Condition condition);

    /**
     * 执行sql游标语句
     *
     * @param sqlIndex    sql游标
     * @param search      查询对象
     * @param resultClass 返回的类型
     * @param <E>         返回的类型
     * @return
     */
    <E> List<E> select(String sqlIndex, BaseSearch search, Class<E> resultClass);

    /**
     * 执行sql游标语句
     *
     * @param sqlIndex    sql游标
     * @param search      查询对象
     * @param resultClass 返回的类型
     * @param <E>         返回的类型
     * @return
     */
    <E> List<E> select(String sqlIndex, Map<String, Object> search, Class<E> resultClass);


    /**
     * 执行sql
     *
     * @param sqlIndex 游标
     * @param params   参数对象
     * @return
     */
    <E extends Entity<?>> Integer execute(String sqlIndex, E params);

    /**
     * 执行sql
     *
     * @param sqlIndex 游标
     * @param params   参数
     * @return
     */
    Integer execute(String sqlIndex, Map<String, Object> params);

    /**
     * 返回缓存
     *
     * @param id 对象id
     */
    T getCache(Number id);

    /**
     * 获取缓存集合
     *
     * @param ids 对象id集合
     * @return 返回对象集合
     */
    <E extends Number> Map<E, T> getCache(List<E> ids);

    /**
     * 删除缓存
     *
     * @param id 对象id
     */
    void delCache(Number id);

    /**
     * 删除所有缓存
     */
    void delAllCache();
}
