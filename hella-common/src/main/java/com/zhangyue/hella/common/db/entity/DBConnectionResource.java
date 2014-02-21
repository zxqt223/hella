package com.zhangyue.hella.common.db.entity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 数据库连接占用的资源 包含 Connection, Statement, ResultSet，便于close
 * 
 * @author scott 2013-8-19 上午11:04:43
 * @version 1.0
 */
public class DBConnectionResource {

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    public DBConnectionResource(Connection connection, Statement statement, ResultSet resultSet){
        this.connection = connection;
        this.statement = statement;
        this.resultSet = resultSet;
    }

    public Connection getConnection() {
        return connection;
    }

    public Statement getStatement() {
        return statement;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }
}
