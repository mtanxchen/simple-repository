package com.simple.repository.connect;

import com.simple.repository.config.SimpleConfig;
import com.simple.repository.util.SimpleStringUtils;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 数据库连接管理
 * <p>
 * 数据库连接管理分为长连池、短连池、回收池三组
 * 长连接是整个程序生命周期中共享的连接,能有效的减少数据库连接次数降低资源损耗,主要作用于自动提交事务的操作。
 * 短连接是短期存活的连接,主要作用于非自动提交事务性的操作,事务开启时创建，事务手动提交后关闭。
 * 回收池用于保存待关闭的连接再有序关闭连接。
 * </p>
 *
 * @author laiqx
 * @date 2023-02-05
 */
public class SimpleDataSource {

    /**
     * 链接池
     */
    private static final List<SimpleConnection> CONNECTION_POOL = new CopyOnWriteArrayList<>();


    /**
     * 数据库连接url
     */
    private String url;

    /**
     * 数据库连接用户名
     */
    private String username;

    /**
     * 数据库连接密码
     */
    private String password;

    /**
     * 数据库使用驱动名
     */
    private String driver;

    /**
     * 数据源对象
     */
    private static SimpleDataSource simpleDataSource = null;

    private SimpleDataSource() {
    }

    /**
     * 加载数据源
     *
     * @param dataSource 数据库连接配置
     * @return 返回数据源
     */
    public static synchronized SimpleDataSource initSimpleDataSource(SimpleConfig.DataSource dataSource) throws SQLException, ClassNotFoundException {
        if (null == simpleDataSource) {
            simpleDataSource = new SimpleDataSource();
            simpleDataSource.url = dataSource.getUrl();
            simpleDataSource.username = dataSource.getUsername();
            simpleDataSource.password = dataSource.getPassword();
            simpleDataSource.driver = SimpleStringUtils.isEmpty(dataSource.getDriver()) ? "com.mysql.cj.jdbc.Driver" : dataSource.getDriver();
            simpleDataSource.initConnectPool(DEFAULT_CONNECT_POOL_SIZE);
        }
        return simpleDataSource;
    }


    /**
     * 获取活跃的数据库连接
     * 1.如果数据库连接池连接数小于最小连接数量则会新建连接
     * 2.如果连接已经超时则关闭连接后重新获取
     *
     * @return 返回数据库连接
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public synchronized SimpleConnection activeConnect() throws SQLException, ClassNotFoundException {
        SimpleConnection connection = CONNECTION_POOL.remove(0);
        if (MIN_CONNECT_POOL_SIZE > CONNECTION_POOL.size()) {
            initConnectPool(1);
        }
        // 过期则重新获取
        if (connection.overdue()) {
            connection.close();
            return activeConnect();
        }
        return connection;
    }

    /**
     * 归还连接
     * <p>
     * 判断是否超时，如果超时则关闭连接，否则放回连接池
     * </p>
     */
    public void giveBack(SimpleConnection connection) throws SQLException {
        if (connection.overdue() || MAX_CONNECT_POOL_SIZE < CONNECTION_POOL.size()) {
            connection.close();
            return;
        }
        CONNECTION_POOL.add(connection);
    }

    /**
     * 初始化连接池
     */
    private void initConnectPool(int initSize) throws SQLException, ClassNotFoundException {
        for (int i = 0; i < initSize; i++) {
            CONNECTION_POOL.add(connect());
        }
    }

    /**
     * 数据库连接
     */
    private SimpleConnection connect() throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        return new SimpleConnection(DriverManager.getConnection(url, username, password));
    }

    /**
     * 短连接数
     */
    public static Integer DEFAULT_CONNECT_POOL_SIZE = 128;


    /**
     * 最大连接数
     */
    public static Integer MAX_CONNECT_POOL_SIZE = 2048;

    /**
     * 最小连接数
     */
    public static Integer MIN_CONNECT_POOL_SIZE = 16;

}
