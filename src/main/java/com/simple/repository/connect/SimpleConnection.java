package com.simple.repository.connect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库连接
 * @author laiqx
 * @date 2023-05-09
 */
public class SimpleConnection {

    /**
     * sql连接
     */
    private Connection connection;

    /**
     * 连接时间戳
     */
    private Long timestamp;

    /**
     * 连接过期时间 2小时（单位：毫秒）
     */
    private static final Long overdue_times = 2 * 60 * 60 * 1000L;

    public SimpleConnection(){
    }

    public SimpleConnection(Connection connection){
        this.connection = connection;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建Statement
     * @return Statement
     * @throws SQLException
     */
    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    /**
     * 提交事务
     * @throws SQLException
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * 回滚
     * @throws SQLException
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }


    /**
     * 判断是否超时
     * @return
     */
    public boolean overdue(){
        return System.currentTimeMillis() - timestamp > overdue_times;
    }

    /**
     * 关闭连接
     * @throws SQLException
     */
    public void close() throws SQLException {
        if(null != connection){
            connection.close();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
