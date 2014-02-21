package com.zhangyue.hella.engine.dao;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.engine.db.entity.XjobState;
import com.zhangyue.hella.engine.entity.XjobStateView;

public class XjobStateDao extends BaseDao {

    public XjobState save(XjobState xjobState) throws DaoException {
        // jobPlanNodeID,currentExecutorKey,runInfo,runTime,executeTimes,finishedPercent,jobPlanNodeState
        Object[] params =
                { xjobState.getJobPlanNodeID(),
                 xjobState.getCurrentExecutorKey(), xjobState.getRunInfo(),
                 xjobState.getRunTime(), xjobState.getExecuteTimes(),
                 xjobState.getFinishedPercent(),
                 xjobState.getJobPlanNodeState() };
        int id = 0;
        try {
            id = super.insertForKeys(SqlTemplate.SQL_INSERT_XJOB_STATE, params);
            xjobState.setId(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }
        return xjobState;

    }

    public XjobState queryXjobState(int id) throws DaoException {
        String whereSql = SqlTemplate.SQL_QUERY_XJOB_STATE + " WHERE id=" + id;
        try {
            return super.findFirst(XjobState.class, whereSql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }

    }

    public XjobState update(XjobState xjobState) throws DaoException {
        String SQL_UPD_XJOB_STATE =
                " UPDATE " + XjobState.TABLE_NAME + " "
                        + "SET jobPlanNodeState='"
                        + xjobState.getJobPlanNodeState() + "', runInfo=?, "
                        + "runTime='" + xjobState.getRunTime()
                        + "',executeTimes=" + xjobState.getExecuteTimes()
                        + ", finishedPercent=" + xjobState.getFinishedPercent()
                        + " WHERE id=" + xjobState.getId();
        try {
            super.update(SQL_UPD_XJOB_STATE, xjobState.getRunInfo());
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }
        return xjobState;
    }

    public List<XjobState> queryXjobStateErrorByRunTime(String runTime)
        throws DaoException {
        StringBuffer SQL_QUERY_XJOB_STATE_ERROR =
                new StringBuffer(SqlTemplate.SQL_QUERY_XJOB_STATE_ERROR);
        if (StringUtils.isNotBlank(runTime)) {
            SQL_QUERY_XJOB_STATE_ERROR.append(" AND  runTime >=" + runTime + "");
        }
        try {
            return super.find(XjobState.class,
                SQL_QUERY_XJOB_STATE_ERROR.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }
    }

    public List<XjobState> queryXjobStateByCurrentExecutorKey(
        String currentExecutorKey) throws DaoException {
        if (StringUtils.isBlank(currentExecutorKey)) {
            return null;
        }
        StringBuffer SQL_QUERY_XJOB_STATE =
                new StringBuffer(SqlTemplate.SQL_QUERY_XJOB_STATE);
        SQL_QUERY_XJOB_STATE.append(" WHERE currentExecutorKey='"
                                    + currentExecutorKey + "'");
        try {
            return super.find(XjobState.class, SQL_QUERY_XJOB_STATE.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }
    }

    public List<XjobState> queryXjobStateByWhereSql(String whereSql)
        throws DaoException {
        if (StringUtils.isBlank(whereSql)) {
            return null;
        }
        StringBuffer SQL_QUERY_XJOB_STATE =
                new StringBuffer(SqlTemplate.SQL_QUERY_XJOB_STATE);
        SQL_QUERY_XJOB_STATE.append(whereSql);
        try {
            return super.find(XjobState.class, SQL_QUERY_XJOB_STATE.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }
    }

    public Page findPage(final Page page, final String whereSql)
        throws DaoException {
        String querySql =
                SqlTemplate.SQL_QUERY_S_N_P_V + whereSql
                        + " ORDER BY  s.id desc ";
        String countSql = getCountSql(querySql);
        long totalCount;
        try {
            totalCount = super.count(countSql);
            if (totalCount > 0) {
                page.setTotalCount(totalCount);
                List<XjobStateView> result =
                        super.findPage(XjobStateView.class, querySql,
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