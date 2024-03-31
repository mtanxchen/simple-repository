package com.simple.repository.util;

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库数据类型工具
 *
 * @author laiqx
 * date 2023-02-06
 */
public class SimpleSqlDataType {

    /**
     * Mysql数据类型
     */
    public static final Map<String, SqlType> MYSQL_TYPE = new HashMap<>();

    static {
        MYSQL_TYPE.put("BIT", SqlType.BOOLEAN);
        MYSQL_TYPE.put("TINYINT", SqlType.INTEGER);
        MYSQL_TYPE.put("SMALLINT", SqlType.INTEGER);
        MYSQL_TYPE.put("MEDIUMINT", SqlType.INTEGER);
        MYSQL_TYPE.put("INT", SqlType.INTEGER);
        MYSQL_TYPE.put("BIGINT", SqlType.LONG);
        MYSQL_TYPE.put("FLOAT", SqlType.FLOAT);
        MYSQL_TYPE.put("DOUBLE", SqlType.DOUBLE);
        MYSQL_TYPE.put("DECIMAL", SqlType.BIG_DECIMAL);
        MYSQL_TYPE.put("DATE", SqlType.DATE);
        MYSQL_TYPE.put("DATETIME", SqlType.DATE);
        MYSQL_TYPE.put("TIMESTAMP", SqlType.TIMESTAMP);
        MYSQL_TYPE.put("TIME", SqlType.DATE);
        MYSQL_TYPE.put("CHAR", SqlType.STRING);
        MYSQL_TYPE.put("VARCHAR", SqlType.STRING);
        MYSQL_TYPE.put("TINYTEXT", SqlType.STRING);
        MYSQL_TYPE.put("TEXT", SqlType.STRING);
        MYSQL_TYPE.put("LONGTEXT", SqlType.STRING);
    }

    /**
     * 异常类型对象
     */
    public enum SqlType {
        STRING(String.class, "String"),
        INTEGER(Integer.class, "Integer"),
        LONG(Long.class, "Long"),
        FLOAT(Float.class, "Float"),
        DOUBLE(Double.class, "Double"),
        BOOLEAN(Boolean.class, "Boolean"),
        DATE("java.util.Date", Date.class, "Date"),
        TIMESTAMP("java.sql.Timestamp", Timestamp.class, "Timestamp"),
        BIG_DECIMAL("java.math.BigDecimal", BigDecimal.class, "BigDecimal"),
        ;

        SqlType(String packageName, Class<?> type, String typeName) {
            this.type = type;
            this.typeName = typeName;
            this.packageName = packageName;
        }

        SqlType(Class<?> type, String typeName) {
            this.type = type;
            this.typeName = typeName;
        }

        private Class<?> type;

        private String typeName;

        private String packageName;

        public Class<?> getType() {
            return type;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getPackageName() {
            return packageName;
        }
    }


    /**
     * 返回字段类型
     * <p>
     * 解析ResultSetMetaData中字段名与字段类型并置入map
     * </p>
     *
     * @param metaData 数据库字段信息
     * @return 字段类型map
     * @throws SQLException 异常抛出
     */
    public static Map<String, Class<?>> getColumnType(ResultSetMetaData metaData) throws SQLException {
        int columnCount = metaData.getColumnCount();
        Map<String, Class<?>> columnType = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            String name = metaData.getColumnLabel(i);
            String columnTypeName = metaData.getColumnTypeName(i);
            if (null == SimpleSqlDataType.MYSQL_TYPE.get(columnTypeName)) {
                columnType.put(name, String.class);
                continue;
            }
            columnType.put(name, SimpleSqlDataType.MYSQL_TYPE.get(columnTypeName).type);
        }
        return columnType;
    }

}
