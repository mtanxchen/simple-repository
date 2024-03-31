package com.simple.repository.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sql工具
 *
 * @author laiqx
 * @version 1.0.1
 * date 2022-11-08
 */

public class SimpleSqlUtil {

    /**
     * sql语句空值矫正
     * <p>
     * 处理sql语句中没有匹配值的文本
     * 例如： select * from test where name = &{name} and age > 18; name 为空
     * 矫正后 select * from test where 1=1 and age > 18;
     * </p>
     *
     * @param sql sql语句
     * @param valueMap 值map,key:字段名，value:字段值
     * @return 返回矫正后的sql语句
     */
    public static String sqlRevise(String sql, Map<String, ?> valueMap) {
        Map<String, String> paramMap = sqlPreAnalysis(sql);
        String replaceText = " 1 = 1 ";
        for (String field : paramMap.keySet()) {
            if (null == valueMap.get(field) || SimpleStringUtils.isEmpty(valueMap.get(field).toString())) {
                sql = SimpleStringUtils.replaceAll(sql, paramMap.get(field), replaceText);
            }
        }
        // sql特殊处理
        sql = sqlSetActionHandler(sql);
        return sql;
    }

    /**
     * set语句处理
     *
     * @param sql 待处理的sql语句
     */
    private static String sqlSetActionHandler(String sql) {
        sql = SimpleStringUtils.replaceAll(sql, "  ", " ");
        sql = SimpleStringUtils.replaceAll(sql, ", 1 = 1", "");
        sql = SimpleStringUtils.replaceAll(sql, "set 1 = 1 ,", "");
        return sql;
    }

    /**
     * sql预解析
     * <p>
     * 切割sql语句，查找出待替换的sql段和参数名称
     * 例如：select * from test where name = &{name} and age > ${age};
     * 解析结果 map -> (1) key="name":value="name = ${name}"
     * (2) key="age":value="age > ${age}"
     * </p>
     *
     * @param sql sql语句
     * @return 返回目前sql待替换的属性标识
     */
    public static Map<String, String> sqlPreAnalysis(String sql) {
        if (SimpleStringUtils.isEmpty(sql)) {
            return new HashMap<>();
        }
        sql = sql.toLowerCase();
        /* 根据&{或%}获取切割点 */
        String replace = " 1 = 1 ";
        Map<String, String> map = new HashMap<>();
        while (true) {
            int leftIndex = sql.indexOf("${");
            leftIndex = leftIndex > 0 ? leftIndex : sql.indexOf("&{");
            if (leftIndex <= 0) {
                return map;
            }
            // 获取替换的属性名
            String leftSql = sql.substring(0, leftIndex);
            int rightIndex = leftIndex + sql.substring(leftIndex).indexOf("}") + 1;
            String name = sql.substring(leftIndex + 2, rightIndex - 1);
            // 获取替换的sql段
            leftIndex = maxLeftIndex(leftSql);
            String invalidText = sql.substring(leftIndex, rightIndex);
            map.put(name, invalidText.trim());
            sql = sql.replace(invalidText, replace);
        }
    }

    /**
     * 最大的左sql标签位置
     *
     * @param leftSql 截取后左边的sql语句
     * @return 最大的左sql标签位置
     */
    public static int maxLeftIndex(String leftSql) {
        Integer index = null;
        for (String tag : tags) {
            int tabIndex = leftSql.lastIndexOf(tag) + tag.length();
            index = null == index || tabIndex > index ? tabIndex : index;
        }
        return index;
    }


    /**
     * 格式化sql值
     *
     * @param value 条件值
     * @return 返回格式化后值
     */
    public static String formatValue(Object value) {
        if (null == value || SimpleStringUtils.isEmpty(value.toString())) {
            return "null";
        }
        if (value instanceof String) {
            value = SimpleStringUtils.replaceAll(value.toString(), "'", "\\'");
            return "'" + value + "'";
        } else if (value instanceof Date) {
            return "'" + SimpleDateUtils.dateToStr((Date) value, SimpleDateUtils.FormatType.DATE_FORMAT) + "'";
        } else if (value instanceof Collection) {
            List<String> list = new ArrayList<>();
            for (Object o : (Collection<?>) value) {
                String str = formatValue(o);
                if(SimpleStringUtils.isNotEmpty(str)){
                    list.add(str);
                }
            }
            value = String.join(",", list);
            value = "(" + value + ")";
        }
        return value.toString();
    }

    private static final List<String> tags = Arrays.asList(" set ", " where ", " on ", " and ", " or ", "(", " having ", ",");

}
