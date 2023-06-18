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
     * @return
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
                if(null == field.get(obj)){
                    continue;
                }
                map.put((K) field.get(obj), obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    /**
     * list 指定参数转 map
     *
     * @param list     列表
     * @param keyName  字段名
     * @param keyClass key类型
     * @param valName  值名称
     * @param valClass value 类型
     * @param <K>      返回map中key的类型
     * @param <V>      返回map中value的类型
     * @return 返回map
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
                map.put((K) keyField.get(obj), (V) valField.get(obj));
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
                newList.add((P) field.get(obj));
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
     * @return
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
                set.add((P) field.get(obj));
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
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> mapSort(Map<K, V> map, boolean isDesc) {
        if (null == map || map.isEmpty()) {
            return map;
        }
        Map<K, V> newMap = new LinkedHashMap<>();
        Comparator<K> comparator = new Comparator<K>() {
            @Override
            public int compare(Object o1, Object o2) {
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
            }
        };
        map.keySet().stream().sorted(comparator).forEach(o -> {
            newMap.put(o, map.get(o));
        });
        return newMap;
    }

}
