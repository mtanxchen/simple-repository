package com.simple.repository.config;

import com.simple.repository.connect.SimpleSession;
import com.simple.repository.master.exception.SimpleException;

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
     */
    public static void clear() {
        if (null == session.get()) {
            return;
        }
        try {
            session.get().clear();
        } catch (SQLException e) {
            throw new SimpleException("关闭连接会话失败");
        }
        session.remove();
    }


}
