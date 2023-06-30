package com.simple.repository.master;


import com.simple.repository.config.SimpleConfig;
import com.simple.repository.connect.SimpleSession;
import com.simple.repository.master.cache.SimpleCacheManager;
import com.simple.repository.master.cache.SimpleInnerCacheManager;
import com.simple.repository.master.cache.SimpleRedisCacheManager;
import com.simple.repository.master.exception.SimpleException;
import com.simple.repository.master.search.BaseSearch;
import com.simple.repository.master.search.Condition;
import com.simple.repository.util.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 存储操作对象
 *
 * @param <T> 操作对象类型
 * @author laiqx
 * date 2023-02-08
 */
public class BaseRepository<T extends Entity> implements IBaseRepository<T> {

    /**
     * 当前存储对象使用的entity类型
     */
    private Class<T> entityClass;

    /**
     * 缓存管理
     */
    private SimpleCacheManager<T> cacheManager;

    /**
     * 开启数据缓存的表
     */
    private static List<String> cacheTables = new ArrayList<>();

    /**
     * 批量处理最大数
     * <p>
     * 单次批量新增或更新的最大数
     * </p>
     */
    private static final int MAX_BATCH_NUMBER = 10000;

    /**
     * 初始化对象时构造缓存相关容器
     * <p>
     * 根据配置使用内存管理器,本版本支持本地内存与redis
     * 注意:缓存的数据需提前配置
     * -- simple.cache-table(simple-repository.yml)
     * </p>
     */
    public BaseRepository() {
        try {
            cacheTables = SimpleConfig.initConfig().cacheTable;
            cacheManager = null != SimpleConfig.initConfig().redis && SimpleConfig.initConfig().redis.getEnable() ? new SimpleRedisCacheManager<>() : new SimpleInnerCacheManager<>();
        } catch (Exception e) {
            cacheManager = new SimpleInnerCacheManager<>();
        }
    }

    /**
     * 设置操作对象类型
     * <p>
     * 在repository初始化时调用
     * </p>
     *
     * @param entityClass 操作对象类型
     */
    public void setEntityClass(Class<T> entityClass) {
        // 字段类型
        this.entityClass = entityClass;
    }

    /**
     * 新增对象
     *
     * @param entity 对象
     */
    @Override
    public void add(T entity) {
        if (null == entity) {
            throw new SimpleException(SimpleException.Type.ENTITY_IS_NULL);
        }
        // 分析对象生成组合sql的字段和字段值
        String sql = "insert into %s (%s) values (%s)";
        Map<String, String> analysisMap = analysisPojoAndFormatValue(entity, null, false);
        String fields = String.join(",", analysisMap.keySet());
        String values = String.join(",", analysisMap.values());
        sql = String.format(sql, getTableName(), fields, values);
        // 执行保存
        List<Number> ids = SimpleSession.openSession().add(sql, entity.getIdentityClass());
        if (ids.isEmpty()) {
            return;
        }
        entity.id = ids.get(0);
    }

    /**
     * 批量新增
     *
     * @param entities 对象集合
     */
    @Override
    public void add(List<T> entities) {
        if (null == entities || entities.isEmpty()) {
            throw new SimpleException(SimpleException.Type.ENTITY_IS_NULL);
        }
        add(getTableName(), entities);
    }

    @Override
    public int update(T entity) {
        return update(entity, false);
    }

    @Override
    public void update(List<T> entities) {
        update(entities, false);
    }

    @Override
    public void update(T entity, Condition condition) {
        update(entity, condition, false);
    }

    /**
     * 更新（包含null）
     *
     * @param entity 更新对象
     * @return 返回更新数量
     */
    @Override
    public int updateAll(T entity) {
        return update(entity, true);
    }

    /**
     * 批量更新（包含null）
     *
     * @param entities 更新对象集
     */
    @Override
    public void updateAll(List<T> entities) {
        update(entities, true);
    }

    /**
     * 条件更新（包含null）
     *
     * @param entity    更新对象
     * @param condition 条件对象
     */
    @Override
    public void updateAll(T entity, Condition condition) {
        update(entity, condition, true);
    }

    /**
     * 根据id输出
     * <p>
     * 如果id为空则不执行
     * </p>
     *
     * @param id 主键
     */
    @Override
    public void delete(Number id) {
        if (null == id) {
            return;
        }
        String table = getTableName();
        String sql = "delete from %s where id = %s;";
        sql = String.format(sql, table, id);
        int num = SimpleSession.openSession().update(sql);
        if (num <= 0) {
            return;
        }
        delCache(id);
    }

    /**
     * 批量删除
     *
     * @param ids 主键集合
     */
    @Override
    public void delete(List<? extends Number> ids) {
        if (null == ids || ids.isEmpty()) {
            return;
        }
        delete(new Condition().in(Entity.ID, ids));
    }

    /**
     * 条件删除
     *
     * @param condition 根据条件删除数据
     */
    @Override
    public void delete(Condition condition) {
        if (null == condition || SimpleStringUtils.isEmpty(condition.getWhereSql())) {
            throw new SimpleException(SimpleException.Type.CONDITION_IS_NULL);
        }
        /* 删除数据 */
        String sql = "delete from %s where %s;";
        sql = String.format(sql, getTableName(), getWhereSql(condition));
        SimpleSession.openSession().update(sql);
        delAllCache();
    }

    @Override
    public T get(Number id) {
        if (null == id) {
            throw new RuntimeException(SimpleException.Type.ENTITY_ID_IS_NULL.getMsg());
        }
        String sql = "select * from %s where id = %s;";
        // 生成表名
        String table = getTableName();
        // 分析对象生成组合sql的字段和字段值
        sql = String.format(sql, table, id);
        List<Map<String, Object>> list = SimpleSession.openSession().query(sql);
        List<T> results = SimpleCollectionUtil.listMapToObject(list, entityClass);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public T get(Condition condition) {
        String table = getTableName();
        String where = getWhereSql(condition);
        String sql = "select * from %s where %s;";
        sql = String.format(sql, table, where);
        List<Map<String, Object>> list = SimpleSession.openSession().query(sql);
        List<T> results = SimpleCollectionUtil.listMapToObject(list, entityClass);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 查询数据列表
     *
     * @param search 查询对象
     * @return 返回查询结果
     */
    @Override
    public List<T> list(BaseSearch search) {
        if (null == search) {
            throw new SimpleException(SimpleException.Type.CONDITION_IS_NULL);
        }
        String table = getTableName();
        String where = getWhereSql(search.getCondition());
        String sort = search.getSort();
        String sql = "select * from %s where %s %s limit %s,%s;";
        sql = String.format(sql, table, where, sort, search.getStartIndex(), search.getEndIndex());
        // 不分页查询
        if (!search.getPage()) {
            sql = "select * from %s where %s %s;";
            sql = String.format(sql, table, where, sort);
        }
        List<Map<String, Object>> list = SimpleSession.openSession().query(sql);
        return SimpleCollectionUtil.listMapToObject(list, entityClass);
    }

    /**
     * 根据id集查询列表
     *
     * @param ids id集
     * @return 返回数据列
     */
    @Override
    public <E extends Number> List<T> list(List<E> ids) {
        if (null == ids || ids.isEmpty()) {
            throw new SimpleException(SimpleException.Type.CONDITION_IS_NULL);
        }
        return list(new Condition().in(Entity.ID, ids));
    }

    @Override
    public List<T> list(Condition condition) {
        if (null == condition) {
            throw new SimpleException(SimpleException.Type.CONDITION_IS_NULL);
        }
        String table = getTableName();
        String where = getWhereSql(condition);
        String sql = "select * from %s where %s;";
        sql = String.format(sql, table, where);
        List<Map<String, Object>> list = SimpleSession.openSession().query(sql);
        return SimpleCollectionUtil.listMapToObject(list, entityClass);
    }

    /**
     * 统计数量
     *
     * @param condition 查询条件
     * @return 返回统计的数据
     */
    @Override
    public Integer count(Condition condition) {
        if (null == condition) {
            throw new SimpleException(SimpleException.Type.CONDITION_IS_NULL);
        }
        String table = getTableName();
        String where = getWhereSql(condition);
        String sql = "select count(1) from %s where %s;";
        sql = String.format(sql, table, where);
        return SimpleSession.openSession().count(sql);
    }

    /**
     * 游标方式查询数据
     *
     * @param sqlIndex    sql游标
     * @param search      查询对象
     * @param resultClass 返回的类型
     * @param <E>         返回结果对象类型
     * @return 返回查询结果
     */
    @Override
    public <E> List<E> select(String sqlIndex, BaseSearch search, Class<E> resultClass) {
        if (SimpleStringUtils.isEmpty(sqlIndex) || null == search || null == resultClass) {
            throw new SimpleException(String.format("查询参数不能为空!sqlIndex=%s;search=%s;resultClass=%s", sqlIndex, SimpleJson.toJsonString(search), resultClass));
        }
        String sql = createIndexSql(sqlIndex, search);
        List<Map<String, Object>> list = SimpleSession.openSession().query(sql);
        return SimpleCollectionUtil.listMapToObject(list, resultClass);
    }

    /**
     * 游标方式查询数据
     *
     * @param sqlIndex    sql游标
     * @param map         查询对象
     * @param resultClass 返回的类型
     * @param <E>         返回结果对象类型
     * @return 返回查询结果
     */
    @Override
    public <E> List<E> select(String sqlIndex, Map<String, Object> map, Class<E> resultClass) {
        if (SimpleStringUtils.isEmpty(sqlIndex) || null == map || map.isEmpty() || null == resultClass) {
            throw new SimpleException(String.format("查询参数不能为空!sqlIndex=%s;map=%s;resultClass=%s", sqlIndex, SimpleJson.toJsonString(map), resultClass));
        }
        Map<String, Object> newMap = new HashMap<>();
        for (String key : map.keySet()) {
            newMap.put(SimpleStringUtils.humpToUnderline(key), map.get(key));
        }
        String sql = createIndexSql(sqlIndex, newMap);
        if (SimpleStringUtils.isEmpty(sql)) {
            throw new SimpleException(SimpleException.Type.SQL_IS_NULL);
        }
        List<Map<String, Object>> list = SimpleSession.openSession().query(sql);
        return SimpleCollectionUtil.listMapToObject(list, resultClass);
    }

    /**
     * 执行sql语句
     * <p>
     * 主要执行变更语句
     * </p>
     *
     * @param sqlIndex 游标
     * @param params   参数
     * @return 返回变更数量
     */
    @Override
    public <E extends Entity<?>> Integer execute(String sqlIndex, E params) {
        if (SimpleStringUtils.isEmpty(sqlIndex)) {
            throw new SimpleException("sqlIndex不能为空");
        }
        String sql = createIndexSql(sqlIndex, params);
        int i = SimpleSession.openSession().update(sql);
        if (i > 0) {
            delAllCache();
        }
        return i;
    }

    /**
     * 执行sql语句
     * <p>
     * 主要执行变更语句
     * </p>
     *
     * @param sqlIndex 游标
     * @param params   参数
     * @return 返回变更数量
     */
    @Override
    public Integer execute(String sqlIndex, Map<String, Object> params) {
        String sql = createIndexSql(sqlIndex, params);
        if (SimpleStringUtils.isEmpty(sql)) {
            throw new SimpleException(SimpleException.Type.SQL_IS_NULL);
        }
        int i = SimpleSession.openSession().update(sql);
        if (i > 0) {
            delAllCache();
        }
        return i;
    }


    /**
     * 查询缓存
     * <p>
     * 如果缓存中没有则查询数据库后再加入缓存中
     * </p>
     *
     * @param id 对象id
     * @return 返回缓存对象
     */
    @Override
    public T getCache(Number id) {
        if (null == id) {
            return null;
        } else if (!isCacheEntity()) {
            // 对象未设置缓存则查询数据库
            return get(id);
        }
        T entity = cacheManager.get(entityClass, id);
        if (null == entity) {
            entity = get(id);
            cacheManager.save(entity);
        }
        return entity;
    }

    /**
     * 批量查询缓存数据
     *
     * @param ids 对象id集合
     * @param <E> 返回的对象类型
     * @return 返回数据集合
     */
    @Override
    public <E extends Number> Map<E, T> getCache(List<E> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        /* 非缓存则直接查表 */
        Map<E, T> reusltMap = new HashMap<>();
        if (!isCacheEntity()) {
            List<T> entitys = list(new Condition().in(Entity.ID, ids));
            entitys.forEach(o -> reusltMap.put((E) o.id, o));
            return reusltMap;
        }
        /* 缓存查询 */
        List<E> nowCacheIds = new ArrayList<>();
        ids.forEach(id -> {
            T entity = cacheManager.get(entityClass, id);
            if (null == entity) {
                nowCacheIds.add(id);
            } else {
                reusltMap.put(id, entity);
            }
        });
        /* 如果没有未缓存的数据则返回结果 */
        if (nowCacheIds.isEmpty()) {
            return reusltMap;
        }
        /* 将对象保存到redis中 */
        List<T> entities = list(nowCacheIds);
        entities.forEach(entity -> {
            cacheManager.save(entity);
            reusltMap.put((E) entity.id, entity);
        });
        return reusltMap;
    }

    @Override
    public void delCache(Number id) {
        if (id == null) {
            return;
        }
        cacheManager.remove(entityClass, id);
    }

    /**
     * 删除所有缓存
     */
    @Override
    public void delAllCache() {
        cacheManager.remove(entityClass);
    }

    /**
     * 批量新增表数据
     *
     * @param tableName 指定表名
     * @param entities  数据集
     */
    private void add(String tableName, List<T> entities) {
        /* 如果大于最大批处理数，择进行分隔执行 */
        if (entities.size() > MAX_BATCH_NUMBER) {
            List<T> newEntities = entities.subList(MAX_BATCH_NUMBER, entities.size());
            entities = entities.subList(0, MAX_BATCH_NUMBER);
            add(tableName, entities);
            add(tableName, newEntities);
            return;
        }
        /* 创建批量插入语句 */
        Map<String, String> analysisMap = analysisListPojo(entities, Collections.singletonList("id"));
        String fields = analysisMap.get("fields");
        String values = analysisMap.get("values");
        if (SimpleStringUtils.isEmpty(values)) {
            throw new RuntimeException("插入的对象为空");
        }
        // 分析对象生成组合sql的字段和字段值
        String sql = "insert into %s %s values %s;";
        sql = String.format(sql, tableName, fields, values);
        // 执行保存
        SimpleSession.openSession().add(sql, Long.class);
    }


    /**
     * 更新对象
     *
     * @param entity     待更新的对象
     * @param updateNull 是否更新空值
     * @return 返回更新数
     */
    private int update(T entity, boolean updateNull) {
        if (null == entity || null == entity.id) {
            throw new SimpleException(SimpleException.Type.ENTITY_ID_IS_NULL);
        }
        /* sql语句构建 */
        String sql = createUpdateSql(entity, updateNull);
        int num = SimpleSession.openSession().update(sql);
        delCache(entity.id);
        return num;
    }

    /**
     * 条件更新
     *
     * @param entity     更新的对象
     * @param condition  条件对象
     * @param updateNull 是否更新空值
     */
    private void update(T entity, Condition condition, boolean updateNull) {
        if (null == entity || null == condition) {
            throw new SimpleException(SimpleException.Type.ENTITY_CONDITION_IS_NULL);
        }
        /* sql语句构建 */
        String sql = "update %s set %s where %s";
        // 分析对象生成组合sql的字段和字段值
        String table = getTableName();
        Map<String, String> analysisMap = analysisPojoAndFormatValue(entity, Collections.singletonList("id"), updateNull);
        StringBuilder stringBuilder = new StringBuilder();
        for (String filed : analysisMap.keySet()) {
            stringBuilder.append(filed).append("=").append(analysisMap.get(filed)).append(",");
        }
        String updateSql = stringBuilder.substring(0, stringBuilder.length() - 1);
        /* 查询语句 */
        String where = getWhereSql(condition);
        sql = String.format(sql, table, updateSql, where);
        // 执行保存
        SimpleSession.openSession().update(sql);
        delAllCache();
    }


    /**
     * 批量更新数据
     * <p>
     * 1.更新数据超过50行则使用临时表更新
     * 2.如果小于50行则使用update语句更新
     * </p>
     *
     * @param entities   待更新数据集
     * @param updateNull 是否更新空值
     */
    private void update(List<T> entities, boolean updateNull) {
        if (null == entities || entities.isEmpty()) {
            throw new SimpleException(SimpleException.Type.ENTITY_IS_NULL);
        }
        entities = distinct(entities);
        if (entities.size() >= 50) {
            tempTableUpdate(entities, updateNull);
        } else {
            List<String> batchSql = createBatchUpdateSql(entities, updateNull);
            SimpleSession.openSession().update(batchSql);
        }
        delAllCache();
    }

    /**
     * 创建游标sql
     *
     * @param sqlIndex 游标
     * @param params   参数
     * @return 返回sql语句
     */
    private String createIndexSql(String sqlIndex, Object params) {
        String sql = SqlIndexCache.getCacheSql(sqlIndex).toLowerCase();
        // 如果sql模板中没有sql语句则报错
        if (SimpleStringUtils.isEmpty(sql)) {
            throw new SimpleException(SimpleException.Type.SQL_IS_NULL);
        }
        /* 合并sql的值 */
        Map<String, Object> map = analysisPojo(params, null, false);
        checkRequiredFiled(sqlIndex, map);
        sql = SimpleSqlUtil.sqlRevise(sql, map);
        for (String field : map.keySet()) {
            sql = replaceIndexSql(sql, field, map.get(field));
        }
        return sql;
    }

    /**
     * 创建游标sql
     *
     * @param sqlIndex 游标
     * @param map      查询对象
     * @return 返回sql语句
     */
    private String createIndexSql(String sqlIndex, Map<String, ?> map) {
        String sql = SqlIndexCache.getCacheSql(sqlIndex).toLowerCase();
        // 如果sql模板中没有sql语句则报错
        if (SimpleStringUtils.isEmpty(sql)) {
            throw new SimpleException(SimpleException.Type.SQL_IS_NULL);
        }
        // 非空检查
        checkRequiredFiled(sqlIndex, map);
        // 合并sql
        sql = SimpleSqlUtil.sqlRevise(sql, map);
        for (String field : map.keySet()) {
            sql = replaceIndexSql(sql, field, map.get(field));
        }
        return sql;
    }

    /**
     * 替换sql索引语句的值
     *
     * @param sql   sql语句
     * @param field 字段名
     * @param value 字段值
     * @return 返回替换后的sql
     */
    private static String replaceIndexSql(String sql, String field, Object value) {
        String numName = "${" + field + "}";
        String strName = "&{" + field + "}";
        sql = SimpleStringUtils.replaceAll(sql, strName, SimpleSqlUtil.formatValue(value));
        if (value instanceof String) {
            sql = SimpleStringUtils.replaceAll(sql, numName, value.toString());
        } else {
            sql = SimpleStringUtils.replaceAll(sql, numName, SimpleSqlUtil.formatValue(value));
        }
        return sql;
    }

    /**
     * 非空检查
     *
     * @param sqlIndex sql索引
     * @param map      传值列表
     */
    private void checkRequiredFiled(String sqlIndex, Map<String, ?> map) {
        List<String> requiredFiledList = SqlIndexCache.getRequiredFiled(sqlIndex);
        for (String requiredFiled : requiredFiledList) {
            if (null == map.get(requiredFiled)) {
                throw new SimpleException(requiredFiled + "传参不能为空");
            }
        }
    }


    /**
     * 解析对象的属性与值(值格式化)
     * <p>
     * 1.调用analysisPojo解析出待格式化值的属性map
     * 2.对map值进行格式化
     * </p>
     *
     * @param entity       查询对象
     * @param nullAnalysis 是否空值解析
     * @return 返回解析后属性名与属性值，map由fields与value组成
     */
    private Map<String, String> analysisPojoAndFormatValue(Object entity, List<String> filterFields, boolean nullAnalysis) {
        Map<String, Object> map = analysisPojo(entity, filterFields, nullAnalysis);
        Map<String, String> result = new HashMap<>();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            result.put(key, SimpleSqlUtil.formatValue(value));
        }
        return result;
    }

    /**
     * 解析对象的属性与值
     * <p>
     * 1.属性格式：属性名由驼峰转下划线；
     * 2.解析的属性与值由nullAnalysis控制，
     * nullAnalysis为true,解析结果包含空值
     * nullAnalysis为falst,解析结果不包含空值
     * </p>
     *
     * @param entity       查询对象
     * @param nullAnalysis 是否空值解析
     * @return 返回解析后属性名与属性值，map由fields与value组成
     */
    private Map<String, Object> analysisPojo(Object entity, List<String> filterFields, boolean nullAnalysis) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Field> fieldMap = SimpleBeanUtils.getFields(entity);
        for (String fieldName : fieldMap.keySet()) {
            Field field = fieldMap.get(fieldName);
            String name = SimpleStringUtils.humpToUnderline(fieldName);
            try {
                Object value = field.get(entity);
                if (null != filterFields && filterFields.contains(name) || !nullAnalysis && null == value) {
                    continue;
                }
                map.put(name, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    /**
     * 解析对象的属性与值
     * <p>
     * 属性格式：属性名由驼峰转下划线；
     * </p>
     *
     * @param entities 对象集合
     * @return 返回解析后属性名与属性值，map由fields与value组成
     */
    private Map<String, String> analysisListPojo(List<T> entities, List<String> filterParams) {
        Map<String, Field> fieldMap = SimpleBeanUtils.getFields(entities.get(0));
        // 获取字段值
        List<String> values = new ArrayList<>();
        List<String> fieldList = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            T entity = entities.get(i);
            List<String> value = new ArrayList<>();
            for (String fieldName : fieldMap.keySet()) {
                String name = SimpleStringUtils.humpToUnderline(fieldName);
                if (filterParams.contains(name)) {
                    continue;
                } else if (i == 0) {
                    fieldList.add(name);
                }
                try {
                    value.add(SimpleSqlUtil.formatValue(fieldMap.get(fieldName).get(entity)));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            values.add("(" + String.join(",", value) + ")");
        }
        Map<String, String> analysisMap = new HashMap<>();
        analysisMap.put("fields", "(" + String.join(",", fieldList) + ")");
        analysisMap.put("values", String.join(",", values));
        return analysisMap;
    }


    /**
     * 解析对象的字段
     * <p>
     * 1.解析entity对象包含的字段列表
     * 2.如果filterParams中存在则过滤掉
     * 3.如果nullAnalysis=true:值为null的字段也要解析并返回;
     * nullAnalysis=false:值为null的字段需过滤;
     * </p>
     *
     * @param entity       待解析对象
     * @param filterParams 过滤的字段列表
     * @param nullAnalysis 是否包含空值
     * @return 返回字段列表
     */
    private List<String> analysisFields(T entity, List<String> filterParams, boolean nullAnalysis) {
        /* 对象转为下划线后将字段名与值装载进analysisMap */
        List<String> fields = new ArrayList<>();
        Map<String, Field> fieldMap = SimpleBeanUtils.getFields(entity);
        for (String fieldName : fieldMap.keySet()) {
            try {
                Object value = fieldMap.get(fieldName).get(entity);
                fieldName = SimpleStringUtils.humpToUnderline(fieldName);
                if (filterParams.contains(fieldName) || (!nullAnalysis && value == null)) {
                    continue;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            fields.add(fieldName);
        }
        return fields;
    }


    /**
     * 获取表名
     *
     * @return 返回表名
     */
    private String getTableName() {
        String table = entityClass.getSimpleName().replace("Entity", "");
        table = SimpleStringUtils.humpToUnderline(table);
        return table;
    }

    /**
     * 判断是否缓存
     */
    private boolean isCacheEntity() {
        return cacheTables.contains(getTableName());
    }


    /**
     * 创建批量更新语句
     *
     * @param entities 待处理数据集
     * @return 返回拼接完成的更新sql语句集
     */
    private List<String> createBatchUpdateSql(List<T> entities, boolean nullAnalysis) {
        List<String> batchSql = new ArrayList<>();
        for (T entity : entities) {
            batchSql.add(createUpdateSql(entity, nullAnalysis));
        }
        return batchSql;
    }


    /**
     * 创建更新语句
     *
     * @param entity     对象
     * @param updateNull 是否更新空值
     * @return 返回语句
     */
    private String createUpdateSql(T entity, boolean updateNull) {
        // 分析对象生成组合sql的字段和字段值
        Map<String, String> analysisMap = analysisPojoAndFormatValue(entity, Collections.singletonList("id"), updateNull);
        StringBuilder content = new StringBuilder();
        int i = 0;
        for (String field : analysisMap.keySet()) {
            if (i > 0) {
                content.append(",");
            }
            content.append(field).append("=").append(analysisMap.get(field));
            i++;
        }
        String tableName = getTableName();
        String sql = "update %s set %s where %s.id = " + entity.id;
        return String.format(sql, tableName, content, tableName);
    }


    /**
     * 数据集去重
     *
     * @param entities 待去重数据
     * @return 返回去重后的数据
     */
    private List<T> distinct(List<T> entities) {
        Map<Number, T> map = new HashMap<>();
        for (T entity : entities) {
            if (null == entity.id) {
                continue;
            }
            map.put(entity.id, entity);
        }
        return new ArrayList<>(map.values());
    }

    /**
     * 临时表更新
     * <p>
     * 1.对数据集根据id去重
     * 2.创建目标表数据结构一致的临时表
     * 3.待更新的数据插入临时表中
     * 4.使用表链接更新修改目标表数据
     * </p>
     *
     * @param entities   修改数据集
     * @param updateNull 是否空值更新
     */
    private void tempTableUpdate(List<T> entities, boolean updateNull) {
        /* 建立临时表 */
        String targetTable = getTableName();
        String tempTable = createTempTable(targetTable);
        // 填充临时表数据
        add(tempTable, entities);
        /* 更新数据:从临时表同步到实体表 */
        List<String> fields = analysisFields(entities.get(0), Collections.singletonList("id"), updateNull);
        String sql = "update %s left join %s on %s.id = %s.id set  %s where %s.id is not null;";
        String setContent = createSetContent(targetTable, tempTable, fields, updateNull);
        sql = String.format(sql, targetTable, tempTable, tempTable, targetTable, setContent, tempTable);
        SimpleSession.openSession().update(sql);
        /* 删除临时表 */
        sql = "drop table " + tempTable;
        SimpleSession.openSession().update(sql);
    }

    /**
     * 创建临时表
     *
     * @param sourceTable 结构来源表
     * @return 返回临时表名
     */
    private String createTempTable(String sourceTable) {
        String sql = "create temporary table %s  like %s;";
        String tempTable = sourceTable + System.currentTimeMillis();
        sql = String.format(sql, tempTable, sourceTable);
        SimpleSession.openSession().update(sql);
        return tempTable;
    }

    /**
     * 创建set语句
     *
     * @param tableName     目标表
     * @param tempTableName 临时表
     * @param fields        字段集合
     * @param updateNull    是否空更新
     * @return 返回set语句集
     */
    private String createSetContent(String tableName, String tempTableName, List<String> fields, boolean updateNull) {
        String sqlTemplate = updateNull ? tableName + ".%s = " + tempTableName + ".%s" :
                tableName + ".%s = IFNULL(" + tempTableName + ".%s," + tableName + ".%s)";
        StringBuilder setContent = new StringBuilder(String.format(sqlTemplate, "id", "id", "id"));
        for (String filed : fields) {
            setContent.append(",").append(String.format(sqlTemplate, filed, filed, filed));
        }
        return setContent.toString();
    }

    /**
     * 获取条件查询语句
     *
     * @param condition 条件查询对象
     * @return 返回条件查询语句
     */
    private String getWhereSql(Condition condition) {
        if (null != condition && SimpleStringUtils.isNotEmpty(condition.getWhereSql())) {
            return condition.getWhereSql();
        }
        return " 1 = 1 ";
    }
}
