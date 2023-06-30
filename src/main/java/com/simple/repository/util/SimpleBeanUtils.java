package com.simple.repository.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字符处理工具
 *
 * @author laiqx
 * @version 1.0.1
 * date 2022-11-14
 */
public class SimpleBeanUtils {

    /**
     * 对象拷贝
     * @param source 来源
     * @param target 目标对象
     * @return 返回目标对象
     * @param <E> 目标对象类型
     */
    public static <E> E copyObject(Object source, E target) {
        if (null == source || null == target) {
            return null;
        }
        /* 获取来源对象的值 */
        Map<String, Field> sourceMap = getFields(source);
        Map<String, Field> targetMap = getFields(target);
        for (String name : targetMap.keySet()) {
            Field targetField = targetMap.get(name);
            Field sourceField = sourceMap.get(name);
            if (null == sourceField || Modifier.isFinal(targetField.getModifiers())
                    || Modifier.isStatic(targetField.getModifiers())) {
                continue;
            }
            try {
                targetField.setAccessible(true);
                sourceField.setAccessible(true);
                targetField.set(target, sourceField.get(source));
            } catch (Exception e) {
            }
        }
        return target;
    }

    /**
     * 对象拷贝
     * @param source 来源
     * @param tClass 目标类型
     * @return 返回目标对象
     * @param <E> 目标对象类型
     */
    public static <E> E copyObject(Object source, Class<E> tClass) {
        if (null == source || null == tClass) {
            return null;
        }
        try {
            E target = tClass.newInstance();
            copyObject(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对象列表拷贝
     * @param sources 来源集
     * @param tClass 目标类型
     * @return 返回目标对象列表
     * @param <E> 目标对象类型
     */
    public static <E> List<E> copyListObject(List<?> sources, Class<E> tClass) {
        if (null == sources || null == tClass || sources.isEmpty()) {
            return null;
        }
        /* 获取来源对象的值 */
        List<E> result = new ArrayList<>();
        for (Object source : sources) {
            E target = copyObject(source, tClass);
            result.add(target);
        }
        return result;
    }


    /**
     * 查询出对象的属性MAP
     *
     * @param object 操作对象
     * @return 返回对象属性MAP
     */
    public static Map<String, Field> getFields(Object object) {
        return getFields(object.getClass());
    }

    /**
     * 查询出对象的属性MAP
     *
     * @param tClass 操作对象的类型
     * @return 返回对象属性MAP
     */
    public static Map<String, Field> getFields(Class<?> tClass) {
        Map<String, Field> map = new HashMap<>();
        for (Field field : tClass.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            map.put(field.getName(), field);
        }
        for (Field field : tClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            map.put(field.getName(), field);
        }
        return map;
    }

}
