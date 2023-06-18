package com.simple.repository.generate;

import java.util.List;

/**
 * 数据库表模型信息
 * <p>
 *     用于创建entity对象使用
 * </p>
 *
 * @author laiqx
 * @date 2023-02-08
 */
public class ModelInfo {

    /**
     * 数据库名称
     */
    public String schema;

    /**
     * 包命
     */
    public String packageName;

    /**
     * 待生成entity的表名
     */
    public List<String> tables;

    public ModelInfo(String schema, String packageName, List<String> tables) {
        this.schema = schema;
        this.packageName = packageName;
        this.tables = tables;
    }
}
