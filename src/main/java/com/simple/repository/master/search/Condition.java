package com.simple.repository.master.search;

import com.simple.repository.util.SimpleDateUtils;
import com.simple.repository.util.SimpleStringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询条件对象
 *
 * @author laiqx
 * date 2023-04-23
 */
public class Condition {

    private String whereSql = "";

    public String getWhereSql() {
        return whereSql;
    }


    /**
     * 排序
     */
    private static final List<Sort> sorts = new ArrayList<>();

    /**
     * 新增排序规则
     *
     * @param fieldName 字段
     * @param type      排序类型
     * @return 返回当前查询对象
     */
    public Condition putSort(String fieldName, Sort.SortType type) {
        sorts.add(new Sort(fieldName, type));
        return this;
    }


    /**
     * 获取排序
     *
     * @return 查询条件
     */
    public String getSort() {
        if (sorts.isEmpty()) {
            return "";
        }
        StringBuilder sortStr = new StringBuilder(" order by ");
        for (Sort sort : sorts) {
            sortStr.append(sort.getFieldName()).append(" ").append(sort.getType()).append(",");
        }
        return sortStr.substring(0, sortStr.length() - 1);
    }

    /**
     * 小于
     *
     * @param fieldName 字段名称
     * @param value     条件值
     * @return 返回当前对象
     */
    public Condition lt(String fieldName, Object value) {
        if (SimpleStringUtils.isEmpty(fieldName) || null == value) {
            whereSql += " 1 = 1";
            return this;
        }
        String sql = " %s < %s";
        whereSql += String.format(sql, fieldName, formatValue(value));
        return this;
    }

    /**
     * 小于等于
     *
     * @param fieldName 字段名称
     * @param value     条件值
     * @return 返回当前对象
     */
    public Condition le(String fieldName, Object value) {
        if (SimpleStringUtils.isEmpty(fieldName) || null == value) {
            whereSql += " 1 = 1";
            return this;
        }
        String sql = " %s <= %s";
        whereSql += String.format(sql, fieldName, formatValue(value));
        return this;
    }

    /**
     * 大于
     *
     * @param fieldName 字段名称
     * @param value     条件值
     * @return 返回当前对象
     */
    public Condition gt(String fieldName, Object value) {
        if (SimpleStringUtils.isEmpty(fieldName) || null == value) {
            whereSql += " 1 = 1";
            return this;
        }
        String sql = " %s > %s";
        whereSql += String.format(sql, fieldName, formatValue(value));
        return this;
    }

    /**
     * 大于等于
     *
     * @param fieldName 字段名称
     * @param value     条件值
     * @return 返回当前对象
     */
    public Condition ge(String fieldName, Object value) {
        if (SimpleStringUtils.isEmpty(fieldName) || null == value) {
            whereSql += " 1 = 1";
            return this;
        }
        String sql = " %s >= %s";
        whereSql += String.format(sql, fieldName, formatValue(value));
        return this;
    }

    /**
     * 等于
     *
     * @param fieldName 字段名称
     * @param value     条件值
     * @return 返回当前对象
     */
    public Condition eq(String fieldName, Object value) {
        if (SimpleStringUtils.isEmpty(fieldName) || null == value) {
            whereSql += " 1 = 1";
            return this;
        }
        String sql = " %s = %s";
        whereSql += String.format(sql, fieldName, formatValue(value));
        return this;
    }

    /**
     * 不等于
     *
     * @param fieldName 字段名称
     * @param value     条件值
     * @return 返回当前对象
     */
    public Condition ne(String fieldName, Object value) {
        if (SimpleStringUtils.isEmpty(fieldName) || null == value) {
            whereSql += " 1 = 1";
            return this;
        }
        String sql = " %s != %s";
        whereSql += String.format(sql, fieldName, formatValue(value));
        return this;
    }

    /**
     * 模糊匹配
     *
     * @param fieldName 字段名
     * @param value     字段值
     * @return 返回当前对象
     */
    public Condition like(String fieldName, Object value) {
        if (SimpleStringUtils.isEmpty(fieldName) || null == value) {
            whereSql += " 1 = 1";
            return this;
        }
        whereSql += " " + fieldName + " like '%" + value + "%'";
        return this;
    }

    /**
     * 包含条件
     *
     * @param fieldName 字段名
     * @param values    字段值
     * @param <E>       值类型
     * @return 返回当前对象
     */
    public <E> Condition in(String fieldName, List<E> values) {
        if (SimpleStringUtils.isEmpty(fieldName) || null == values || values.isEmpty()) {
            whereSql += " 1 = 1";
            return this;
        }
        String sql = " %s in (%s)";
        String str;
        if (values.get(0).getClass().equals(String.class) || values.get(0).getClass().equals(Date.class)) {
            str = values.stream().map(o -> "'" + o.toString() + "'").collect(Collectors.joining(","));
        } else {
            str = values.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        whereSql += String.format(sql, fieldName, str);
        return this;
    }

    /**
     * 不包含
     *
     * @param fieldName 字段名
     * @param values    字段值
     * @param <T>       值类型
     * @return 返回当前条件对象
     */
    public <T> Condition notIn(String fieldName, List<T> values) {
        if (SimpleStringUtils.isEmpty(fieldName) || null == values || values.isEmpty()) {
            whereSql += " 1 = 1";
            return this;
        }
        String sql = " %s not in(%s)";
        String str;
        if (values.get(0).getClass().equals(String.class) || values.get(0).getClass().equals(Date.class)) {
            str = values.stream().map(o -> "'" + o.toString() + "'").collect(Collectors.joining(","));
        } else {
            str = values.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        whereSql += String.format(sql, fieldName, str);
        return this;
    }

    /**
     * 是否为空
     *
     * @param fieldName 字段名
     * @return 返回当前条件对象
     */
    public Condition isNull(String fieldName) {
        if (SimpleStringUtils.isEmpty(fieldName)) {
            whereSql += " 1 = 1";
            return this;
        }
        String sql = " %s is null";
        whereSql += String.format(sql, fieldName);
        return this;
    }

    /**
     * 是否非空
     *
     * @param fieldName 字段名
     * @return 返回当前条件对象
     */
    public Condition isNotNull(String fieldName) {
        if (SimpleStringUtils.isEmpty(fieldName)) {
            whereSql += " 1 = 1";
            return this;
        }
        String sql = " %s is not null";
        whereSql += String.format(sql, fieldName);
        return this;
    }

    /**
     * 与
     *
     * @return 返回当前条件对象
     */
    public Condition or() {
        whereSql += " or";
        return this;
    }

    /**
     * 或
     *
     * @return 返回当前条件对象
     */
    public Condition and() {
        whereSql += " and";
        return this;
    }

    /**
     * 括弧()
     *
     * @param condition 条件
     * @return 返回新增括弧后的条件对象
     */
    public Condition paren(Condition condition) {
        if (SimpleStringUtils.isEmpty(condition.whereSql)) {
            return this;
        }
        String sql = "(%s)";
        whereSql += String.format(sql, condition.whereSql);
        return this;
    }

    /**
     * 查询数据范围
     *
     * @param size 查询数量
     * @return 返回当前条件对象
     */
    public Condition limit(Integer size) {
        if (null == size) {
            return this;
        }
        whereSql += " limit " + size;
        return this;
    }

    /**
     * 查询数据范围
     *
     * @param startIndex 下标开始
     * @param endIndex   下标结束
     * @return 返回当前条件对象
     */
    public Condition limit(Integer startIndex, Integer endIndex) {
        if (null == startIndex || null == endIndex) {
            return this;
        }
        whereSql += " limit " + startIndex + "," + endIndex;
        return this;
    }

    /**
     * 范围条件查询
     *
     * @param fieldName 字段名
     * @param start     范围开始
     * @param end       范围结束
     * @return 返回当前条件对象
     */
    public Condition between(String fieldName, Object start, Object end) {
        if (SimpleStringUtils.isEmpty(fieldName) || null == start || null == end) {
            whereSql += " 1 = 1";
            return this;
        }
        String sql = " %s between %s and %s";
        whereSql += String.format(sql, fieldName, formatValue(start), formatValue(end));
        return this;
    }

    /**
     * 空条件
     *
     * @return 返回当前对象
     */
    public Condition isNotCondition() {
        whereSql += " 1 = 1 ";
        return this;
    }

    /**
     * 格式化值
     *
     * @param value 条件值
     * @return 返回格式化后值
     */
    private Object formatValue(Object value) {
        if (null == value) {
            return value;
        }
        if (value instanceof String) {
            value = SimpleStringUtils.replaceAll(value.toString(), "'", "\\'");
            return "'" + value + "'";
        } else if (value instanceof Date) {
            return "'" + SimpleDateUtils.dateToStr((Date) value, SimpleDateUtils.FormatType.DATE_FORMAT) + "'";
        }
        return value;
    }
}
