package com.zhangyue.hella.engine.dao;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Descriptions The class BaseDao.java's implementation：抽象DAO类，所有的DAO类都有该类派生出来
 * @author scott
 * @date 2013-8-19 下午2:33:53
 * @version 1.0
 */
public abstract class BaseDao {

    private static Logger LOG = LoggerFactory.getLogger(BaseDao.class);
    private QueryRunner queryRunner;
    private List<Class<?>> primitiveClasses = new ArrayList<Class<?>>() {

        /**
         * 
         */
        private static final long serialVersionUID = 1438864275318928550L;

        {
            add(Long.class);
            add(Integer.class);
            add(String.class);
            add(java.util.Date.class);
            add(java.sql.Date.class);
            add(java.sql.Timestamp.class);
        }
    };
    /** 返回单一列时用到的handler */
    private final static ColumnListHandler columnListHandler = new ColumnListHandler() {

        @Override
        protected Object handleRow(ResultSet rs) throws SQLException {
            Object obj = super.handleRow(rs);
            if (obj instanceof BigInteger) return ((BigInteger) obj).longValue();
            return obj;
        }

    };

    /** 判断是否为原始类型 */
    private boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || primitiveClasses.contains(cls);
    }

    /**
     * 获取当前线程Connection
     * 
     * @return
     */
    public Connection getConnection() {
        return com.zhangyue.hella.engine.dao.DBPools.currentConnection();
    }

    public static void closeConnection() {
        com.zhangyue.hella.engine.dao.DBPools.closeConnection();
    }

    public String getCountSql(final String sql) {
        String fromSql = sql;
        fromSql = "FROM " + StringUtils.substringAfter(fromSql, "FROM");
        fromSql = StringUtils.substringBefore(fromSql, "ORDER BY");
        String countSql = "SELECT COUNT(*) " + fromSql;
        return countSql;
    }

    /**
     * @param sql 插入sql语句
     * @param params 插入参数
     * @return 返回影响行数
     * @throws SQLException
     */
    public int insert(String sql, Object[] params) throws SQLException {
        LOG.debug(" sql:" + sql);
        queryRunner = new QueryRunner();
        int affectedRows = 0;
        if (params == null) {
            affectedRows = queryRunner.update(getConnection(), sql);
        } else {
            affectedRows = queryRunner.update(getConnection(), sql, params);
        }
        return affectedRows;
    }

    /**
     * 插入数据库，返回自动增长的主键
     * 
     * @param sql - 执行的sql语句
     * @return 主键 注意；此方法没关闭资源
     * @throws SQLException
     */
    public int insertForKeys(String sql, Object[] params) throws SQLException {
        LOG.debug(" sql:" + sql);
        int key = 0;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ParameterMetaData pmd = stmt.getParameterMetaData();
            if (params.length < pmd.getParameterCount()) {
                throw new SQLException("参数错误:" + pmd.getParameterCount());
            }
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                key = rs.getInt(1);
            }
        } finally {
            if (rs != null) { // 关闭记录集
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) { // 关闭声明
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return key;
    }

    private ScalarHandler scalarHandler = new ScalarHandler() {

        @Override
        public Object handle(ResultSet rs) throws SQLException {
            Object obj = super.handle(rs);
            if (obj instanceof BigInteger) return ((BigInteger) obj).longValue();
            return obj;
        }
    };

    public long count(String sql, Object... params) throws SQLException {
        LOG.debug(" sql:" + sql);
        Number num = 0;
        queryRunner = new QueryRunner();
        if (params == null) {
            num = (Number) queryRunner.query(getConnection(), sql, scalarHandler);
        } else {
            num = (Number) queryRunner.query(getConnection(), sql, scalarHandler, params);
        }
        return (num != null) ? num.longValue() : -1;
    }

    /**
     * 执行sql语句
     * 
     * @param sql sql语句
     * @return 受影响的行数
     * @throws SQLException
     */
    public int update(String sql) throws SQLException {
        return update(sql, null);
    }

    /**
     * 单条修改记录
     * 
     * @param sql sql语句
     * @param param 参数
     * @return 受影响的行数
     * @throws SQLException
     */
    public int update(String sql, Object param) throws SQLException {
        return update(sql, new Object[] { param });
    }

    /**
     * 单条修改记录
     * 
     * @param sql sql语句
     * @param params 参数数组
     * @return 受影响的行数
     * @throws SQLException
     */
    public int update(String sql, Object[] params) throws SQLException {
        LOG.debug(" sql:" + sql);
        queryRunner = new QueryRunner();
        int affectedRows = 0;
        if (params == null) {
            affectedRows = queryRunner.update(getConnection(), sql);
        } else {
            affectedRows = queryRunner.update(getConnection(), sql, params);
        }
        return affectedRows;
    }

    /**
     * 批量修改记录
     * 
     * @param sql sql语句
     * @param params 二维参数数组
     * @return 受影响的行数的数组
     * @throws SQLException
     */
    public int[] batchUpdate(String sql, Object[][] params) throws SQLException {
        LOG.debug(" sql:" + sql);
        queryRunner = new QueryRunner();
        int[] affectedRows = new int[0];
        affectedRows = queryRunner.batch(getConnection(), sql, params);
        return affectedRows;
    }

    /**
     * 执行查询，将每行的结果保存到一个Map对象中，然后将所有Map对象保存到List中
     * 
     * @param sql sql语句
     * @return 查询结果
     * @throws SQLException
     */
    public List<Map<String, Object>> find(String sql) throws SQLException {
        return find(sql, null);
    }

    /**
     * 执行查询，将每行的结果保存到一个Map对象中，然后将所有Map对象保存到List中
     * 
     * @param sql sql语句
     * @param param 参数
     * @return 查询结果
     * @throws SQLException
     */
    public List<Map<String, Object>> find(String sql, Object param) throws SQLException {
        return find(sql, new Object[] { param });
    }

    /**
     * 执行查询，将每行的结果保存到一个Map对象中，然后将所有Map对象保存到List中
     * 
     * @param sql sql语句
     * @param params 参数数组
     * @return 查询结果
     * @throws SQLException
     */
    public List<Map<String, Object>> findPage(String sql, int page, int count, Object... params) throws SQLException {
        sql = sql + " LIMIT ?,?";
        LOG.debug(" sql:" + sql);
        queryRunner = new QueryRunner();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (params == null) {
            list =
                    (List<Map<String, Object>>) queryRunner.query(getConnection(), sql, new MapListHandler(),
                        new Integer[] { page, count });
        } else {
            list =
                    (List<Map<String, Object>>) queryRunner.query(getConnection(), sql, new MapListHandler(),
                        ArrayUtils.addAll(params, new Integer[] { page, count }));
        }
        return list;
    }

    /**
     * 执行查询，将每行的结果保存到一个Map对象中，然后将所有Map对象保存到List中
     * 
     * @param sql sql语句
     * @param params 参数数组
     * @return 查询结果
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> find(String sql, Object[] params) throws SQLException {
        LOG.debug(" sql:" + sql);
        queryRunner = new QueryRunner();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (params == null) {
            list = (List<Map<String, Object>>) queryRunner.query(getConnection(), sql, new MapListHandler());
        } else {
            list = (List<Map<String, Object>>) queryRunner.query(getConnection(), sql, new MapListHandler(), params);
        }
        return list;
    }

    /**
     * 执行查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中
     * 
     * @param entityClass 类名
     * @param sql sql语句
     * @return 查询结果
     * @throws SQLException
     */
    public <T> List<T> find(Class<T> entityClass, String sql) throws SQLException {
        return find(entityClass, sql, null);
    }

    /**
     * 执行查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中
     * 
     * @param entityClass 类名
     * @param sql sql语句
     * @param param 参数
     * @return 查询结果
     * @throws SQLException
     */
    public <T> List<T> find(Class<T> entityClass, String sql, Object param) throws SQLException {
        return find(entityClass, sql, new Object[] { param });
    }

    /**
     * 执行查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中
     * 
     * @param entityClass 类名
     * @param sql sql语句
     * @param params 参数数组
     * @return 查询结果
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> find(Class<T> entityClass, String sql, Object[] params) throws SQLException {
        LOG.debug(" sql:" + sql);
        queryRunner = new QueryRunner();
        List<T> list = new ArrayList<T>();
        if (params == null) {
            list = (List<T>) queryRunner.query(getConnection(), sql, new BeanListHandler(entityClass));
        } else {
            list = (List<T>) queryRunner.query(getConnection(), sql, new BeanListHandler(entityClass), params);
        }
        return list;
    }

    /**
     * 查询出结果集中的第一条记录，并封装成对象
     * 
     * @param entityClass 类名
     * @param sql sql语句
     * @return 对象
     * @throws SQLException
     */
    public <T> T findFirst(Class<T> entityClass, String sql) throws SQLException {
        return findFirst(entityClass, sql, null);
    }

    /**
     * 查询出结果集中的第一条记录，并封装成对象
     * 
     * @param entityClass 类名
     * @param sql sql语句
     * @param param 参数
     * @return 对象
     * @throws SQLException
     */
    public <T> T findFirst(Class<T> entityClass, String sql, Object param) throws SQLException {
        return findFirst(entityClass, sql, new Object[] { param });
    }

    /**
     * 查询出结果集中的第一条记录，并封装成对象
     * 
     * @param entityClass 类名
     * @param sql sql语句
     * @param params 参数数组
     * @return 对象
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public <T> T findFirst(Class<T> entityClass, String sql, Object[] params) throws SQLException {
        LOG.debug(" sql:" + sql);
        queryRunner = new QueryRunner();
        Object object = null;
        if (params == null) {
            object = queryRunner.query(getConnection(), sql, new BeanHandler(entityClass));
        } else {
            object = queryRunner.query(getConnection(), sql, new BeanHandler(entityClass), params);
        }
        return (T) object;
    }

    /**
     * 查询出结果集中的第一条记录，并封装成Map对象
     * 
     * @param sql sql语句
     * @return 封装为Map的对象
     * @throws SQLException
     */
    public Map<String, Object> findFirst(String sql) throws SQLException {
        return findFirst(sql, null);
    }

    /**
     * 查询出结果集中的第一条记录，并封装成Map对象
     * 
     * @param sql sql语句
     * @param param 参数
     * @return 封装为Map的对象
     * @throws SQLException
     */
    public Map<String, Object> findFirst(String sql, Object param) throws SQLException {
        return findFirst(sql, new Object[] { param });
    }

    /**
     * 查询出结果集中的第一条记录，并封装成Map对象
     * 
     * @param sql sql语句
     * @param params 参数数组
     * @return 封装为Map的对象
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> findFirst(String sql, Object[] params) throws SQLException {
        LOG.debug(" sql:" + sql);
        queryRunner = new QueryRunner();
        Map<String, Object> map = null;
        if (params == null) {
            map = (Map<String, Object>) queryRunner.query(getConnection(), sql, new MapHandler());
        } else {
            map = (Map<String, Object>) queryRunner.query(getConnection(), sql, new MapHandler(), params);
        }

        return map;
    }

    /**
     * 查询某一条记录，并将指定列的数据转换为Object
     * 
     * @param sql sql语句
     * @param columnName 列名
     * @return 结果对象
     * @throws SQLException
     */
    public Object findBy(String sql, String params) throws SQLException {
        return findBy(sql, params, null);
    }

    /**
     * 查询某一条记录，并将指定列的数据转换为Object
     * 
     * @param sql sql语句
     * @param columnName 列名
     * @param param 参数
     * @return 结果对象
     * @throws SQLException
     */
    public Object findBy(String sql, String columnName, Object param) throws SQLException {
        return findBy(sql, columnName, new Object[] { param });
    }

    /**
     * 查询某一条记录，并将指定列的数据转换为Object
     * 
     * @param sql sql语句
     * @param columnName 列名
     * @param params 参数数组
     * @return 结果对象
     * @throws SQLException
     */
    public Object findBy(String sql, String columnName, Object[] params) throws SQLException {
        LOG.debug(" sql:" + sql);
        queryRunner = new QueryRunner();
        Object object = null;
        if (params == null) {
            object = queryRunner.query(getConnection(), sql, new ScalarHandler(columnName));
        } else {
            object = queryRunner.query(getConnection(), sql, new ScalarHandler(columnName), params);
        }
        return object;
    }

    /**
     * 查询某一条记录，并将指定列的数据转换为Object
     * 
     * @param sql sql语句
     * @param columnIndex 列索引
     * @return 结果对象
     * @throws SQLException
     */
    public Object findBy(String sql, int columnIndex) throws SQLException {
        return findBy(sql, columnIndex, null);
    }

    /**
     * 查询某一条记录，并将指定列的数据转换为Object
     * 
     * @param sql sql语句
     * @param columnIndex 列索引
     * @param param 参数
     * @return 结果对象
     * @throws SQLException
     */
    public Object findBy(String sql, int columnIndex, Object param) throws SQLException {
        return findBy(sql, columnIndex, new Object[] { param });
    }

    /**
     * 查询某一条记录，并将指定列的数据转换为Object
     * 
     * @param sql sql语句
     * @param columnIndex 列索引
     * @param params 参数数组
     * @return 结果对象
     * @throws SQLException
     */
    public Object findBy(String sql, int columnIndex, Object[] params) throws SQLException {
        LOG.debug(" sql:" + sql);
        queryRunner = new QueryRunner();
        Object object = null;
        if (params == null) {
            object = queryRunner.query(getConnection(), sql, new ScalarHandler(columnIndex));
        } else {
            object = queryRunner.query(getConnection(), sql, new ScalarHandler(columnIndex), params);
        }
        return object;
    }

    /**
     * @param <T>分页查询
     * @param beanClass
     * @param sql
     * @param page
     * @param count
     * @param params
     * @return
     * @throws SQLException
     */
    public <T> List<T> findPage(Class<T> beanClass, String sql, int page, int pageSize, Object... params)
        throws SQLException {
        if (page <= 1) {
            page = 0;
        }
        return query(beanClass, sql + " LIMIT ?,?", ArrayUtils.addAll(params, new Integer[] { page, pageSize }));
    }

    public <T> List<T> query(Class<T> beanClass, String sql, Object... params) throws SQLException {
        LOG.debug(" sql:" + sql);
        queryRunner = new QueryRunner();
        return (List<T>) queryRunner.query(getConnection(), sql,
            isPrimitive(beanClass) ? columnListHandler : new BeanListHandler(beanClass), params);
    }
}
