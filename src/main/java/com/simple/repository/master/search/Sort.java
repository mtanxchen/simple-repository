package com.simple.repository.master.search;


/**
 * 排序对象
 */
public class Sort {

    /**
     * 排序字段
     */
    private String fieldName;

    /**
     * 排序方式
     */
    private SortType type;

    public Sort(String fieldName, SortType type) {
        this.fieldName = fieldName;
        this.type = type;
    }

    /**
     * 排序枚举
     */
    public enum SortType {
        asc, desc
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public SortType getType() {
        return type;
    }

    public void setType(SortType type) {
        this.type = type;
    }
}
