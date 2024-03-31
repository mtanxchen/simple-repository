package com.simple.repository.connect;

import com.simple.repository.config.SimpleConfig;
import com.simple.repository.config.SimpleThreadLocalStore;
import com.simple.repository.master.exception.SimpleException;
import com.simple.repository.util.SimpleSqlDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库连接会话
 * <p>
 * 提供基础的增、删、改、查操作
 * 注意：为了降低资源消耗，将会在线程第一次开启会话时通过ThreadLocal保存,
 * 并且在该线程中共享会话资源(connection、statement)。
 * 因此请根据业务需要，手动调用SimpleThreadLocalStore.clear()释放资源
 * </p>
 *
 * @author laiqx
 * date 2023-02-05
 */
public class SimpleSession {

    private final static Logger log = LoggerFactory.getLogger(SimpleSession.class);
    private final SimpleDataSource dataSource;

    private SimpleConnection connection;

    private Statement statement;

    /**
     * 创建数据库会话
     * <p>
     * 创建会话时初始化该会话连接
     * </p>
     */
    private SimpleSession() throws IOException, SQLException, ClassNotFoundException {
        this.dataSource = SimpleDataSource.initSimpleDataSource(SimpleConfig.initConfig().dataSource);
        connection = this.dataSource.activeConnect();
        statement = connection.createStatement();
    }

    /**
     * 打开数据库会话
     *
     * @return 返回数据会话
     */
    public static SimpleSession openSession() {
        try {
            if (null == SimpleThreadLocalStore.session.get()) {
                SimpleThreadLocalStore.session.set(new SimpleSession());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SimpleException("开启会话失败:" + e.getMessage());
        }
        return SimpleThreadLocalStore.session.get();
    }

    /**
     * 开启事务
     * 如果当前连接未关闭自动提交，则重新获取
     * 注意：自动提交的连接默认为长链
     *
     * @throws SQLException 关闭自动提交事务异常
     */
    public void openTransaction() throws SQLException {
        connection.getConnection().setAutoCommit(false);
    }

    /**
     * 统计数据
     *
     * @param sql 查询sql
     * @return 返回统计的数
     */
    public int count(String sql) {
        ResultSet result = null;
        try {
            result = statement.executeQuery(sql);
            log(sql);
            while (result.next()) {
                return result.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new SimpleException("执行语句异常:" + e.getMessage());
        } finally {
            closeResultSet(result);
        }
    }

    /**
     * 查询数据
     *
     * @param sql 查询语句
     * @return 返回查询的数据
     */
    public List<Map<String, Object>> query(String sql) {
        ResultSet result = null;
        try {
            result = statement.executeQuery(sql);
            log(sql);
            // 解析数据库返回的数据并置入map返回
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Class<?>> columnType = SimpleSqlDataType.getColumnType(result.getMetaData());
            while (result.next()) {
                Map<String, Object> map = new HashMap<>();
                for (String paramName : columnType.keySet()) {
                    map.put(paramName.toLowerCase(), result.getObject(paramName, columnType.get(paramName)));
                }
                list.add(map);
            }
            result.close();
            return list;
        } catch (SQLException e) {
            throw new SimpleException("执行语句异常:" + e.getMessage());
        } finally {
            closeResultSet(result);
        }
    }

    /**
     * 更新数据
     *
     * @param sql sql语句
     * @return 返回变更数量
     */
    public int update(String sql) {
        try {
            log(sql);
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new SimpleException(e);
        }
    }

    /**
     * 批量更新
     *
     * @param sqlList 插入的sql语句
     */
    public void update(List<String> sqlList) {
        try {
            for (String sql : sqlList) {
                log(sql);
                statement.addBatch(sql);
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new SimpleException(e);
        }
    }

    /**
     * 新增对象
     *
     * @param sql    sql语句
     * @param tClass 新增对象的类型
     * @return 返回id集合
     */
    public List<Number> add(String sql, Class<?> tClass) {
        ResultSet result = null;
        try {
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            result = statement.getGeneratedKeys();
            List<Number> ids = new ArrayList<>();
            while (result.next()) {
                //生成的主键只有一个值，所以获得第一列的值即可
                if (tClass.equals(Integer.class)) {
                    ids.add(result.getInt(1));
                } else {
                    ids.add(result.getLong(1));
                }
            }
            result.close();
            log(sql);
            return ids;
        } catch (SQLException e) {
            throw new SimpleException("执行语句异常:" + e.getMessage());
        } finally {
            closeResultSet(result);
        }
    }

    /**
     * 事务提交
     *
     * @throws SQLException 事务提交失败异常
     */
    public void commit() throws SQLException {
        connection.commit();
        dataSource.giveBack(connection);
        SimpleThreadLocalStore.clear();
    }

    /**
     * 事务回滚
     *
     * @throws SQLException 事务回滚失败异常
     */
    public void rollback() throws SQLException {
        connection.rollback();
        SimpleThreadLocalStore.clear();
    }

    /**
     * 使用完连接(提交事务后)归还
     *
     * @throws SQLException 数据连接关闭异常
     */
    public void clear() throws SQLException {
        if (null != statement && !statement.isClosed()) {
            statement.close();
        }
        dataSource.giveBack(connection);
    }

    /**
     * 日志输出
     *
     * @param sql 语句
     */
    public static void log(String sql) {
        try {
            if (SimpleConfig.initConfig().log) {
                log.info(sql);
            }
        } catch (Exception e) {
            throw new SimpleException(e);
        }
    }

    /**
     * 关闭ResultSet
     *
     * @param result ResultSet
     */
    private void closeResultSet(ResultSet result) {
        if (null != result) {
            try {
                result.close();
            } catch (SQLException e) {
                log.error("关闭ResultSet异常:" + e.getMessage());
            }
        }
    }
}
