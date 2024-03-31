package com.simple.repository.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

public class SimpleJson {

    /**
     * 转下划线JSON对象
     *
     * @param obj 待转换对象
     * @return 返回JSON对象
     */
    public static JSONObject toUnderlineJson(Object obj) {
        SerializeConfig config = new SerializeConfig();
        config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        return JSONObject.parseObject(JSON.toJSONString(obj, config, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteDateUseDateFormat));
    }

    /**
     * 列表转下划线JSON数组
     *
     * @param obj 待转换对象
     * @return JSON数组
     */
    public static JSONArray toUnderlineJsonArray(List obj) {
        SerializeConfig config = new SerializeConfig();
        config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        return JSONArray.parseArray(JSON.toJSONString(obj, config, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteDateUseDateFormat));
    }


    /**
     * 转JSON格式String
     * @param obj 待转换对象
     * @return 返回JSON字符串
     */
    public static String toJsonString(Object obj) {
        if (null == obj) {
            return null;
        }
        return JSONObject.toJSONString(obj);
    }

    /**
     * String 转对象
     * @param content 内容
     * @param classType 对象类型
     * @return 返回转换后的对象
     * @param <T> 目标对象类型
     */
    public static <T> T toObject(String content, Class<T> classType) {
        return JSONObject.parseObject(content, classType);
    }

    /**
     * 返回JSON数组
     *
     * @param list 列表
     * @return JSON数组
     */
    public static JSONArray toJsonArray(List list) {
        return JSONArray.parseArray(JSON.toJSONString(list));
    }
}
