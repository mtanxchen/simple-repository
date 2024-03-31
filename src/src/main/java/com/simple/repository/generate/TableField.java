package com.simple.repository.generate;

/**
 * 表字段
 *
 * @author laiqx
 * date 2023-02-08
 */
public class TableField {

    /**
     * 字段名
     */
    public String fieldName;

    /**
     * 字段类型
     */
    public String type;

    /**
     * 字段描述
     */
    public String comment;

    public TableField(String fieldName, String type, String comment) {
        this.fieldName = fieldName;
        this.type = type;
        this.comment = comment;
    }


}
