package com.zhangyue.hella.common.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.db.entity.DBConnectionResource;
import com.zhangyue.hella.common.exception.ConnPoolException;
import com.zhangyue.hella.common.exception.ConnectionException;

/**
 * @Descriptions The class ConnectionManager.java's implementation： 数据库工具
 * @author scott 2013-8-19 上午11:03:39
 * @version 1.0
 */
public class ConnectionManager {

    private static DBConnectionPool connectionPool = null;

    public static void initialize(Configuration schedConf) throws ConnPoolException {

        connectionPool =
                new DBConnectionPool(schedConf.get("jdbc.driver"), schedConf.get("jdbc.url"),
                    schedConf.get("jdbc.username"), schedConf.get("jdbc.password"));

        connectionPool.setInitialConnections(schedConf.getInt("jdbc.connection.pool.initial.connection", 10));
        connectionPool.setIncrementalConnections(schedConf.getInt("jdbc.connection.pool.incremental.connection", 2));
        connectionPool.setMaxConnections(schedConf.getInt("jdbc.connection.pool.max.connection", 20));

        try {
            connectionPool.createPool();
        } catch (Exception e) {
            throw new ConnPoolException(e.getMessage());
        }
    }

    /**
     * 获取数据库连接
     * 
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    /**
     * 关闭结果集
     */
    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) try {
            resultSet.close();
        } catch (SQLException e) {
        }// IGNORE}
    }

    /**
     * 关闭结果集和Statement对象
     */
    public static void closeResultSetAndStatement(ResultSet resultSet, Statement statement) {
        if (resultSet != null) try {
            resultSet.close();
        } catch (SQLException e) {
        }// IGNORE}
        if (null != statement) try {
            statement.close();
        } catch (SQLException e) {
        }// IGNORE
    }

    public static void closeAndReleaseDBConnectionResource(DBConnectionResource resource) {
        if (null == resource) {
            return;
        }
        if (null != resource.getResultSet()) try {
            resource.getResultSet().close();
        } catch (SQLException e) {
        }// IGNORE}
        if (null != resource.getStatement()) try {
            resource.getStatement().close();
        } catch (SQLException e) {
        }// IGNORE

        if (null != resource.getConnection()) try {
            resource.getConnection().close();
        } catch (SQLException e) {
        }// IGNORE

        releaseConnection(resource.getConnection());
    }

    /**
     * 关闭Statement对象
     */
    public static void closeStatement(Statement statement) {
        if (null != statement) try {
            statement.close();
        } catch (SQLException e) {
        }// IGNORE
    }

    /**
     * 执行查询SQL, 注意，执行完这个方法必须执行： <br>
     * 1. DataSourceManager.closeResultSetAndStatement( resultSet, stmt ); <br>
     * 2. DataSourceManager.returnBackConnectionToPool( conn );
     * 
     * @param selectSql 查询SQL语句
     */
    public static DBConnectionResource executeQuery(String querySql) throws ConnectionException {
        try {
            Connection conn = getConnection();
            if (null == conn) throw new Exception("No available connection");
            Statement stmt = conn.createStatement();
            return new DBConnectionResource(conn, stmt, stmt.executeQuery(querySql));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConnectionException("执行数据库查询[" + querySql + "]出错: " + e.getMessage(), e.getCause());
        }
    }

    /**
     * 执行插入SQL<br>
     * 此方法自己会释放资源，不需要调用方释放。
     */
    public static int executeInsert(String insertSql) throws ConnectionException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            if (null == conn) throw new Exception("No available connection");
            stmt = conn.createStatement();
            return stmt.executeUpdate(insertSql);
        } catch (Exception e) {
            throw new ConnectionException("Error when execute insert [" + insertSql + "],error: " + e.getMessage(),
                e.getCause());
        } finally {
            closeStatement(stmt);
            releaseConnection(conn);
        }
    }

    /**
     * 执行插入SQL,并获取最后一次插入主键值 此方法自己会释放资源，不需要调用方释放。
     */
    public static int executeInsertAndReturnGeneratedKeys(String insertSql) throws ConnectionException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            if (null == conn) throw new Exception("No available connection");
            stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (null != rs && rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (Exception e) {
            throw new ConnectionException("执行数据库插入[" + insertSql + "]出错: " + e.getMessage(), e.getCause());
        } finally {
            closeResultSetAndStatement(rs, stmt);
            releaseConnection(conn);
        }
    }

    /**
     * 更新数据库 update 此方法自己会释放资源，不需要调用方释放。
     */
    public static int executeUpdate(String updateSql) throws ConnectionException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            if (null == conn) throw new Exception("No available connection");
            stmt = conn.createStatement();
            return stmt.executeUpdate(updateSql);
        } catch (Exception e) {
            throw new ConnectionException("执行数据库更新[" + updateSql + "]出错: " + e.getMessage(), e.getCause());
        } finally {
            closeStatement(stmt);
            releaseConnection(conn);
        }
    }

    /**
     * 删除 此方法自己会释放资源，不需要调用方释放。
     */
    public static int executeDelete(String deleteSql) throws ConnectionException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            if (null == conn) throw new Exception("No available connection");
            stmt = conn.createStatement();
            return stmt.executeUpdate(deleteSql);
        } catch (Exception e) {
            throw new ConnectionException("执行数据库删除[" + deleteSql + "]出错: " + e.getMessage(), e.getCause());
        } finally {
            closeStatement(stmt);
            releaseConnection(conn);
        }
    }

    /**
     * 向数据库连接池中归还连接，即释放连接
     * 
     * @param conn
     */
    public static void releaseConnection(Connection conn) {
        connectionPool.releaseConnection(conn);
    }

    public static Statement createStatement(Connection conn) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stmt;
    }

    /**
     * create a Query for Result
     * 
     * @param stmt
     * @param sql
     * @return
     */
    public static ResultSet executeQuery(Statement stmt, String sql) {
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    /**
     * create a Prepared Statement
     * 
     * @param conn
     * @param sql
     * @return
     */
    public static PreparedStatement preparedStatement(Connection conn, String sql) {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pstmt;
    }

    public static PreparedStatement peraredSatement(Connection conn, String sql, int autoGeneratedKeys) {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pstmt;
    }

}
