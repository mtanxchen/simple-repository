package com.simple.repository.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * 容器类工具
 */
public class SimpleCollectionUtil {

    /**
     * list 转 map
     *
     * @param fieldName 字段名
     * @param list      列表
     * @param keyClass  key类型
     * @param <K>       返回map中key的类型
     * @param <V>       返回map中value的类型
     * @return 返回map对象
     */
    public static <K, V> Map<K, V> listToMap(List<V> list, String fieldName, Class<K> keyClass) {
        if (null == list || list.isEmpty() || SimpleStringUtils.isEmpty(fieldName) || null == keyClass) {
            return new HashMap<>();
        }
        fieldName = SimpleStringUtils.underlineToHump(fieldName, false);
        Field field = SimpleBeanUtils.getFields(list.get(0)).get(fieldName);
        if (null == field) {
            throw new RuntimeException("field not found:" + fieldName);
        }
        Map<K, V> map = new HashMap<>();
        for (V obj : list) {
            try {
                if (null == field.get(obj)) {
                    continue;
                }
                map.put(keyClass.cast(field.get(obj)), obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    /**
     * list 指定参数转 map
     *
     * @param list     对象集
     * @param keyName  字段名
     * @param keyClass key类型
     * @param valName  值名称
     * @param valClass 值类型
     * @param <K>      字段名类型
     * @param <V>      字段值类型
     * @param <E>      来源类类型
     * @return 返回转换后的map
     */
    public static <K, V, E> Map<K, V> listToParamMap(List<E> list, String keyName, Class<K> keyClass, String valName, Class<V> valClass) {
        if (null == list || list.isEmpty() || SimpleStringUtils.isEmpty(keyName) || null == keyClass
                || null == valName || null == valClass) {
            return new HashMap<>();
        }
        // 获取key和value的字段
        keyName = SimpleStringUtils.underlineToHump(keyName, false);
        valName = SimpleStringUtils.underlineToHump(valName, false);
        Map<String, Field> fieldMap = SimpleBeanUtils.getFields(list.get(0));
        Field keyField = fieldMap.get(keyName);
        Field valField = fieldMap.get(valName);
        if (null == keyField || null == valField) {
            throw new RuntimeException("field not found:" + keyName + "|" + valName);
        }
        // 遍历list获取数据
        Map<K, V> map = new HashMap<>();
        for (Object obj : list) {
            try {
                if (null == keyField.get(obj)) {
                    continue;
                }
                map.put(keyClass.cast(keyField.get(obj)), valClass.cast(valField.get(obj)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    /**
     * 获取list对象中的参数
     *
     * @param list       对象集合
     * @param paramName  参数名
     * @param paramClass 参数类型
     * @param <P>        参数类型
     * @param <T>        对象类型
     * @return 返回list对象中的某个参数列表
     */
    public static <P, T> List<P> listParam(List<T> list, String paramName, Class<P> paramClass) {
        if (null == list || list.isEmpty() || SimpleStringUtils.isEmpty(paramName) || null == paramClass) {
            return new ArrayList<>();
        }
        // 获取对象中对应的字段
        paramName = SimpleStringUtils.underlineToHump(paramName, false);
        Field field = SimpleBeanUtils.getFields(list.get(0)).get(paramName);
        if (null == field) {
            throw new RuntimeException("field not found:" + paramName);
        }
        // 返回字段值
        List<P> newList = new ArrayList<>();
        for (T obj : list) {
            try {
                if (null == field.get(obj)) {
                    continue;
                }
                newList.add(paramClass.cast(field.get(obj)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return newList;
    }

    /**
     * 获取Set对象中的参数
     *
     * @param list       对象集合
     * @param paramName  参数名
     * @param paramClass 参数类型
     * @param <P>        参数类型
     * @param <T>        对象类型
     * @return 返回转换后的set
     */
    public static <P, T> Set<P> listParamToSet(List<T> list, String paramName, Class<P> paramClass) {
        if (null == list || list.isEmpty() || SimpleStringUtils.isEmpty(paramName) || null == paramClass) {
            return new HashSet<>();
        }
        paramName = SimpleStringUtils.underlineToHump(paramName, false);
        Field field = SimpleBeanUtils.getFields(list.get(0)).get(paramName);
        Set<P> set = new HashSet<>();
        for (T obj : list) {
            try {
                if (null == field.get(obj)) {
                    continue;
                }
                set.add(paramClass.cast(field.get(obj)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return set;
    }

    /**
     * listMap 转 java对象列表
     *
     * @param listMap    数据集
     * @param paramClass 参数类型
     * @param <T>        返回的对象类型
     * @return 返回对象列表
     */
    public static <T> List<T> listMapToObject(List<Map<String, Object>> listMap, Class<T> paramClass) {
        Map<String, Field> fieldMap = SimpleBeanUtils.getFields(paramClass);
        List<T> result = new ArrayList<>();
        for (Map<String, Object> map : listMap) {
            try {
                T entity = paramClass.newInstance();
                for (String fieldName : fieldMap.keySet()) {
                    Field field = fieldMap.get(fieldName);
                    if (null == field) {
                        continue;
                    }
                    Object value = map.get(SimpleStringUtils.humpToUnderline(fieldName));
                    if (value instanceof BigDecimal) {
                        if (field.getType().equals(Integer.class)) {
                            value = ((BigDecimal) value).intValue();
                        } else if (field.getType().equals(Long.class)) {
                            value = ((BigDecimal) value).longValue();
                        } else if (field.getType().equals(Double.class)) {
                            value = ((BigDecimal) value).doubleValue();
                        } else if (field.getType().equals(Float.class)) {
                            value = ((BigDecimal) value).floatValue();
                        } else if (field.getType().equals(String.class)) {
                            value = value.toString();
                        }
                    }
                    field.set(entity, value);
                }
                result.add(entity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * map 排序
     *
     * @param map    map
     * @param isDesc 是否倒序
     * @param <K>    key类型
     * @param <V>    value类型
     * @return 返回排序后的map
     */
    public static <K, V> Map<K, V> mapSort(Map<K, V> map, boolean isDesc) {
        if (null == map || map.isEmpty()) {
            return map;
        }
        Map<K, V> newMap = new LinkedHashMap<>();
        Comparator<K> comparator = (o1, o2) -> {
            if (null == o1 || null == o2) {
                return 0;
            }
            int i = isDesc ? o2.toString().compareTo(o1.toString()) : o1.toString().compareTo(o2.toString());
            if (i > 0) {
                return 1;
            } else if (i < 0) {
                return -1;
            }
            return 0;
        };
        map.keySet().stream().sorted(comparator).forEach(o -> newMap.put(o, map.get(o)));
        return newMap;
    }

    /**
     * list 根据属性值分组
     *
     * @param list     容器
     * @param keyName  属性名
     * @param keyClass 属性类型
     * @param <K>      属性类型
     * @param <V>      对象类型
     * @return 返回分组后map
     */
    public static <K, V> Map<K, List<V>> listGroupMap(List<V> list, String keyName, Class<K> keyClass) {
        if (null != list && !list.isEmpty() && !SimpleStringUtils.isEmpty(keyName) && null != keyClass) {
            keyName = SimpleStringUtils.underlineToHump(keyName, false);
            Map<String, Field> fieldMap = SimpleBeanUtils.getFields(list.get(0));
            Field keyField = fieldMap.get(keyName);
            if (null != keyField) {
                Map<K, List<V>> map = new HashMap<>();
                for (V obj : list) {
                    try {
                        K key = keyClass.cast(keyField.get(obj));
                        List<V> values = null == map.get(key) ? new ArrayList<>() : map.get(key);
                        values.add(obj);
                        map.put(key, values);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                return map;
            } else {
                throw new RuntimeException("field not found:" + keyName);
            }
        } else {
            return new HashMap<>();
        }
    }

    /**
     * list 根据属性值分组
     *
     * @param list     容器
     * @param keyName  属性名
     * @param keyClass 属性类型
     * @param valName  属性值名
     * @param valClass 属性值类型
     * @param <K>      对象类型
     * @param <V>      key类型
     * @param <E>      value类型
     * @return 返回根据根据属性值分组的参数
     */
    public static <K, V, E> Map<K, List<V>> listValueGroupMap(List<E> list, String keyName, Class<K> keyClass, String valName, Class<V> valClass) {
        if (null != list && !list.isEmpty() && !SimpleStringUtils.isEmpty(keyName) && null != keyClass && null != valName && null != valClass) {
            keyName = SimpleStringUtils.underlineToHump(keyName, false);
            valName = SimpleStringUtils.underlineToHump(valName, false);
            Map<String, Field> fieldMap = SimpleBeanUtils.getFields(list.get(0));
            Field keyField = fieldMap.get(keyName);
            Field valField = fieldMap.get(valName);
            if (null != keyField && null != valField) {
                Map<K, List<V>> map = new HashMap<>();
                for (E obj : list) {
                    try {
                        K key = keyClass.cast(keyField.get(obj));
                        V value = valClass.cast(valField.get(obj));
                        List<V> values = null == map.get(key) ? new ArrayList<>() : map.get(key);
                        values.add(value);
                        map.put(key, values);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                return map;
            } else {
                throw new RuntimeException("field not found:" + keyName + "|" + valName);
            }
        } else {
            return new HashMap<>();
        }
    }

}
