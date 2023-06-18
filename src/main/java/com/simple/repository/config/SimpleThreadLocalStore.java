package com.simple.repository.config;

import com.simple.repository.connect.SimpleSession;

import java.sql.SQLException;

/**
 * 会话存储
 * 用于存储当前线程的会话，每次数据库访问完结后需调用clear()释放资源
 */
public class SimpleThreadLocalStore {

    public static ThreadLocal<SimpleSession> session = new ThreadLocal<>();

    /**
     * 清除线程数据
     * <p>
     * 线程结束前调用，用于释放数据库连接会话等资源
     * </p>
     *
     * @throws SQLException
     */
    public static void clear() throws SQLException {
        session.get().clear();
        session.remove();
    }


}
