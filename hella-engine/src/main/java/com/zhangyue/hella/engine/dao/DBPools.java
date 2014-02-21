package com.zhangyue.hella.engine.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.db.DBConnectionPool;
import com.zhangyue.hella.common.exception.ConnPoolException;

/**
 * @Descriptions The class DbPools.java's implementation：数据库连接池 + 事务控制
 * @author scott
 * @date 2013-8-19 下午2:34:54
 * @version 1.0
 */
public class DBPools {

    private static final Logger LOG = LoggerFactory.getLogger(DBPools.class);

    private static ThreadLocal<Connection> currentConnThreadLocal =
            new ThreadLocal<Connection>();

    private static DBConnectionPool connectionPool = null;

    public static void init(Configuration schedConf)
        throws ConnPoolException {

        connectionPool =
                new DBConnectionPool(schedConf.get("jdbc.driver"),
                    schedConf.get("jdbc.url"), schedConf.get("jdbc.username"),
                    schedConf.get("jdbc.password"));

        connectionPool.setInitialConnections(schedConf.getInt(
            "jdbc.connection.pool.initial.connection", 10));
        connectionPool.setIncrementalConnections(schedConf.getInt(
            "jdbc.connection.pool.incremental.connection", 2));
        connectionPool.setMaxConnections(schedConf.getInt(
            "jdbc.connection.pool.max.connection", 20));
        connectionPool.setTestTable(schedConf.get(
            "jdbc.connection.pool.testtable", "SCH_EXECUTE_PLAN_VERSION"));

        try {
            connectionPool.createPool();
        } catch (Exception e) {
            throw new ConnPoolException(e.getMessage());
        }
    }

    public DBPools(){

    }

    /**
     * 外口调用方法，调用此方法即可获取一个数据库的连接池
     */
    public static Connection currentConnection() {
        Connection conn = currentConnThreadLocal.get();
        if (conn == null) {
            conn = getConnection();
            currentConnThreadLocal.set(conn);
        }
        if (!connectionPool.testConnection(conn)) {
            conn = getConnection();
            currentConnThreadLocal.set(conn);
        }
        return conn;
    }

    private static Connection getConnection() {
        if (connectionPool != null) {
            try {
                return connectionPool.getConnection();
            } catch (SQLException e) {
                LOG.error("Fail to get connection.",e);
            }
        }
        return null;
    }

    /**
     * 清空当前线程绑定的链接，线程池并不清空，保持长链接
     */
    public static void closeConnection() {
        connectionPool.releaseConnection(currentConnection());
        currentConnThreadLocal.set(null);
    }

    public static void closeConnectionPool() {
        try {
            connectionPool.closeConnectionPool();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void openTransaction() {
        try {
            Connection conn = currentConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(2);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void commit() {
        try {
            Connection conn = currentConnection();
            if (conn != null) conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void roolback() {
        try {
            Connection conn = currentConnection();
            if (conn != null) conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
