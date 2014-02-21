package com.zhangyue.hella.engine.dao;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.engine.db.entity.JobPlanSubscribe;

public class JobPlanSubscribeDao extends BaseDao {

    private static Logger log =
            LoggerFactory.getLogger(JobPlanSubscribeDao.class);

    public void save(JobPlanSubscribe jobPlanSubscribe) throws DaoException {
        // clusterID,userEmail,userPhoneNumber
        Object[] args =
                new Object[] { jobPlanSubscribe.getClusterID(),
                              jobPlanSubscribe.getUserEmail(),
                              jobPlanSubscribe.getUserPhoneNumber(),
                              jobPlanSubscribe.isState() };
        try {
            super.insert(SqlTemplate.SQL_INSERT_JOBPLAN_SUBSCRIBE, args);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }
    }

    public int delete(int[] ids) throws DaoException {
        if (null == ids || ids.length == 0) {
            return 0;
        }
        StringBuffer sql =
                new StringBuffer(SqlTemplate.SQL_DEL_JOBPLAN_SUBSCRIBE
                                 + " WHERE id IN(0");
        for (int id : ids) {
            sql.append("," + id);
        }
        sql.append(")");
        log.debug(sql.toString());

        try {
            return super.update(sql.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }

    }

    public int updateState(int[] ids, boolean state) throws DaoException {
        if (null == ids || ids.length == 0) {
            return 0;
        }
        StringBuffer sql =
                new StringBuffer(SqlTemplate.SQL_UPDATE_JOBPLAN_SUBSCRIBE
                                 + " AND  id IN(0");
        for (int id : ids) {
            sql.append("," + id);
        }
        sql.append(")");
        log.debug(sql.toString());
        try {
            return super.update(sql.toString(), state);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }

    }

    public List<JobPlanSubscribe> query(String querySql) throws DaoException {
        log.debug("SQL:" + querySql);
        try {
            return super.find(JobPlanSubscribe.class, querySql);
        } catch (SQLException e) {
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }
    }

    public JobPlanSubscribe get(int id) throws DaoException {
        if (0 == id) {
            return null;
        }
        StringBuffer SQL_QUERY_JOBPLAN =
                new StringBuffer(SqlTemplate.SQL_QUERY_JOBPLAN_SUBSCRIBE
                                 + " WHERE 1=1");
        SQL_QUERY_JOBPLAN.append(" AND ID = " + id + " ");
        log.debug("SQL:" + SQL_QUERY_JOBPLAN);
        try {
            return super.findFirst(JobPlanSubscribe.class,
                SQL_QUERY_JOBPLAN.toString());
        } catch (SQLException e) {
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }
    }

    public Page findPage(final Page page, final String whereSql)
        throws DaoException {
        String querySql =
                SqlTemplate.SQL_QUERY_JOBPLAN_SUBSCRIBE + whereSql
                        + " ORDER BY id desc ";
        String countSql = getCountSql(querySql);
        long totalCount;
        try {
            totalCount = super.count(countSql);
            if (totalCount > 0) {
                page.setTotalCount(totalCount);
                List<JobPlanSubscribe> result =
                        super.findPage(JobPlanSubscribe.class, querySql,
                            (page.getPageNo() - 1) * page.getPageSize(),
                            page.getPageSize());
                page.setResult(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }
        return page;
    }

}
