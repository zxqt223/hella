package com.zhangyue.hella.engine.dao;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.engine.db.entity.SystemLog;

public class SystemLogDao extends BaseDao {

    private static Logger log = LoggerFactory.getLogger(SystemLogDao.class);

    public void save(SystemLog systemLog) throws DaoException {
        if (null == systemLog) {
            return;
        }
        log.debug("SQL:" + SqlTemplate.SQL_INSERT_SYSTEM_LOG.toString());
        // operatorName,ip,logType,logContent,createDate
        try {
            super.insert(
                SqlTemplate.SQL_INSERT_SYSTEM_LOG,
                new Object[] { systemLog.getOperatorName(), systemLog.getIp(),
                              systemLog.getLogType(),
                              systemLog.getLogContent(),
                              systemLog.getCreateDate() });
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage());
        } finally {
            super.closeConnection();
        }
    }

    public Page findPage(final Page page, final String whereSql)
        throws DaoException {
        String querySql = SqlTemplate.SQL_QUERY_SYSTEM_LOG + " " + whereSql;
        String countSql = getCountSql(querySql);
        long totalCount;
        try {
            totalCount = super.count(countSql);
            if (totalCount > 0) {
                page.setTotalCount(totalCount);
                List<SystemLog> result =
                        super.findPage(SystemLog.class, querySql,
                            (page.getPageNo() - 1) * page.getPageSize(),
                            page.getPageSize());
                page.setResult(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage());
        } finally {
            super.closeConnection();
        }
        return page;
    }
}
