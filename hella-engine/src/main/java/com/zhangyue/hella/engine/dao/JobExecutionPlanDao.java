package com.zhangyue.hella.engine.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;
import com.zhangyue.hella.engine.db.entity.XjobMeta;
import com.zhangyue.hella.engine.entity.JobPlanState;

/**
 * @Descriptions The class JobExecutionPlanDao.java's implementation：封装表
 *               JobExecutionPlan XjobEntity XjobState CRUD
 * @author scott
 * @date 2013-8-19 下午2:35:56
 * @version 1.0
 */
public class JobExecutionPlanDao extends BaseDao {

    private static Logger log =
            LoggerFactory.getLogger(JobExecutionPlanDao.class);

    public JobExecutionPlan save(JobExecutionPlan jep, boolean isCloseCon)
        throws DaoException {
        // executePlanVersionID,clusterID,jepID,jepName,cronType,cronExpression,ignoreError,state,currentNode,createDate
        Object[] params =
                { jep.getJobPlanType(), jep.getExecutePlanVersionID(),
                 jep.getClusterID(), jep.getJepID(), jep.getJepName(),
                 jep.getCronType(), jep.getCronExpression(), jep.getEvent(),
                 jep.isIgnoreError(), jep.getState(), jep.getCurrentXjobDate(),
                 jep.getCurrentXjobState(), jep.getCurrentNode(),
                 jep.getDescription(), jep.getCreateDate() };
        int id = 0;
        try {
            id =
                    super.insertForKeys(
                        SqlTemplate.SQL_INSERT_JOBEXECUTIONPLAN, params);
            jep.setId(id);
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            if (isCloseCon) {
                super.closeConnection();
            }
        }
        return jep;
    }

    public boolean isJobPlansExist(List<String> ids) throws SQLException{
        StringBuilder sql= new StringBuilder("SELECT COUNT(1) FROM "+JobExecutionPlan.TABLE_NAME+" t WHERE t.jepID in ( ");
        int size=ids.size();
        int i=0;
        while(i<size){
            if(i>0){
                sql.append(",");
            }
            sql.append(ids.get(i++));
        }
        sql.append(" )");
        long count=super.count(sql.toString());
        if(count>0){
            return true;
        }
        return false;
    }
    public void delJobExecutionPlan(int id, boolean isCloseCon)
        throws DaoException {
        Object[] params = { id };
        try {
            super.update(SqlTemplate.SQL_DEL_JOBEXECUTIONPLAN, params);
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            if (isCloseCon) {
                super.closeConnection();
            }
        }
    }

    public JobPlanNode save(JobPlanNode jobPlanNode, boolean isCloseCon)
        throws DaoException {
        // jobExecutionPlanID,type,name,toNode,okNode,errorNode,delayType,delayTime,errorMaxRedoTimes
        Object[] params =
                {
                 jobPlanNode.getJobExecutionPlanID(),
                 jobPlanNode.getType(),
                 jobPlanNode.getName(),
                 jobPlanNode.getExecutorClusterID(),
                 jobPlanNode.getForkName(),
                 jobPlanNode.getJoinName(),
                 jobPlanNode.getToNode(),
                 jobPlanNode.getOkNode(),
                 jobPlanNode.getErrorNode(),
                 jobPlanNode.getDelayType() == null ? null : jobPlanNode.getDelayType(),
                 jobPlanNode.getDelayTime(),
                 jobPlanNode.getErrorMaxRedoTimes(),
                 jobPlanNode.getErrorRedoPeriod() };
        int id = 0;
        try {
            id =
                    super.insertForKeys(SqlTemplate.SQL_INSERT_JOBPLANNODE,
                        params);
            jobPlanNode.setId(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            if (isCloseCon) {
                super.closeConnection();
            }
        }
        return jobPlanNode;
    }

    public void delJobPlanNode(int jobExecutionPlanID, boolean isCloseCon)
        throws DaoException {
        Object[] params = { jobExecutionPlanID };
        try {
            super.update(SqlTemplate.SQL_DEL_JOBPLANNODE, params);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            if (isCloseCon) {
                super.closeConnection();
            }
        }
    }

    public XjobMeta save(XjobMeta xjobMeta, boolean isCloseCon)
        throws DaoException {
        // jobExecutionPlanID,jobPlanNodeID,executeCluster,mode,executionContent,jobClassName,args,description
        Object[] params =
                { xjobMeta.getJobExecutionPlanID(),
                 xjobMeta.getJobPlanNodeID(), xjobMeta.getMode(),
                 xjobMeta.getExecuteUser(), xjobMeta.getExecutionContent(),
                 xjobMeta.getJobClassName(), xjobMeta.getArgs(),
                 xjobMeta.getDescription() };
        int id = 0;
        try {
            id = super.insertForKeys(SqlTemplate.SQL_INSERT_XJOB, params);
            xjobMeta.setId(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            if (isCloseCon) {
                super.closeConnection();
            }
        }
        return xjobMeta;
    }

    public void delXjobMeta(int jobExecutionPlanID, boolean isCloseCon)
        throws DaoException {
        Object[] params = { jobExecutionPlanID };
        try {
            super.update(SqlTemplate.SQL_DEL_XJOB, params);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            if (isCloseCon) {
                super.closeConnection();
            }
        }
    }

    public List<JobPlanState> queryJobPlanStates(String clusterID,
        String[] jepIDs) throws DaoException {
        // SELECT clusterID,jepID,jepName,currentXjobDate,currentXjobState FROM
        // SCH_JOB_EXECUTION_PLAN WHERE 1=1 AND state = 'able' jepID IN(0, '1')
        try {
            if (StringUtils.isBlank(clusterID) || null == jepIDs) {
                return null;
            }
            StringBuffer sql =
                    new StringBuffer(SqlTemplate.SQL_QUERY_JOBPLANSTATE
                                     + " and clusterID='" + clusterID
                                     + "'  and jepID IN(0");
            for (String id : jepIDs) {
                sql.append(", '" + id + "'");
            }
            sql.append(")");
            log.debug(sql.toString());

            return this.query(JobPlanState.class, sql.toString());
        } catch (Exception e) {
            throw new DaoException(e.getMessage(), e.getCause());
        }
    }

    public JobExecutionPlan queryJobPlan(int id) throws DaoException {
        if (0 != id) {
            List<JobExecutionPlan> queryJobPlans =
                    this.queryJobPlan(id, null, null, null, null, null, null,
                        null, true);
            if (null != queryJobPlans && queryJobPlans.size() > 0) {
                return queryJobPlans.get(0);
            }
        }
        return null;
    }

    public List<JobExecutionPlan> queryJobPlanByClusterIDAndState(
        String clusterID, JobExecutionPlan.State state, boolean isCloseCon)
        throws DaoException {
        return this.queryJobPlan(0, clusterID, null, null, state, null, null,
            null, isCloseCon);
    }

    public List<JobExecutionPlan> queryJobPlan(String clusterID, String jepID)
        throws DaoException {
        List<JobExecutionPlan> queryJobPlans =
                this.queryJobPlan(0, clusterID, jepID, null, null, null, null,
                    null, true);
        return queryJobPlans;
    }

    public List<JobExecutionPlan> queryJobPlan(String clusterID, String jepID,
        JobExecutionPlan.State state) throws DaoException {
        List<JobExecutionPlan> queryJobPlans =
                this.queryJobPlan(0, clusterID, jepID, null, state, null, null,
                    null, true);
        return queryJobPlans;
    }

    public JobExecutionPlan queryJobPlan(String clusterID, String jepID,
        boolean isCloseCon) throws DaoException {
        List<JobExecutionPlan> queryJobPlans =
                this.queryJobPlan(0, clusterID, jepID, null, null, null, null,
                    null, isCloseCon);
        if (null != queryJobPlans && queryJobPlans.size() > 0) {
            return queryJobPlans.get(0);
        }
        return null;
    }

    public List<JobExecutionPlan> queryJobPlanByCronType(
        JobExecutionPlan.State state, CronType cronType, String cronExpression)
        throws DaoException {
        return this.queryJobPlan(0, null, null, null, state, cronType,
            cronExpression, null, true);
    }

    private List<JobExecutionPlan> queryJobPlan(int id, String clusterID,
        String jepID, String jepName, JobExecutionPlan.State state,
        CronType cronType, String cronExpression, String event,
        boolean isCloseCon) throws DaoException {

        StringBuffer SQL_QUERY_JOBPLAN =
                new StringBuffer(SqlTemplate.SQL_QUERY_JOBEXECUTIONPLAN_VERSION);
        if (0 != id) {
            SQL_QUERY_JOBPLAN.append(" AND j.id = " + id + "");
        }
        if (StringUtils.isNotBlank(clusterID)) {
            SQL_QUERY_JOBPLAN.append(" AND j.clusterID = '" + clusterID + "'");
        }
        if (StringUtils.isNotBlank(jepID)) {
            SQL_QUERY_JOBPLAN.append(" AND j.jepID = '" + jepID + "'");
        }
        if (StringUtils.isNotBlank(jepName)) {
            SQL_QUERY_JOBPLAN.append(" AND j.jepName = '" + jepName + "'");
        }
        if (null != state) {
            SQL_QUERY_JOBPLAN.append(" AND j.state = '" + state.name() + "'");
        }
        if (null != cronType) {
            SQL_QUERY_JOBPLAN.append(" AND j.cronType = '" + cronType.name()
                                     + "'");
        }
        if (StringUtils.isNotBlank(cronExpression)) {
            SQL_QUERY_JOBPLAN.append(" AND j.cronExpression = '"
                                     + cronExpression + "'");
        }
        if (StringUtils.isNotBlank(event)) {
            SQL_QUERY_JOBPLAN.append(" AND j.event = '" + event + "'");
        }

        SQL_QUERY_JOBPLAN.append(" ORDER BY id DESC ");

        try {
            return super.find(JobExecutionPlan.class,
                SQL_QUERY_JOBPLAN.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            if (isCloseCon) {
                super.closeConnection();
            }
        }
    }

    public void updateJobPlanState(int id, String clusterID,
        JobExecutionPlan.State state, boolean isCloseCon) throws DaoException {
        StringBuffer sql =
                new StringBuffer("UPDATE " + JobExecutionPlan.TABLE_NAME
                                 + " SET state='" + state + "'  WHERE 1=1 ");
        if (0 != id) {
            sql.append(" AND id = " + id + " ");
        }

        if (StringUtils.isNotBlank(clusterID)) {
            sql.append(" AND clusterID = '" + clusterID + "' ");
        }
        log.debug("SQL:" + sql.toString());
        try {
            super.update(sql.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            if (isCloseCon) {
                super.closeConnection();
            }
        }
    }

    public JobExecutionPlan updateJobPlanCurrentXjobState(int id,
        JobExecutionPlan.CurrentXjobState currentXjobState,
        String currentXjobDate, boolean isCloseCon) throws DaoException {
        if (0 == id) {
            return null;
        }
        StringBuffer sql =
                new StringBuffer("UPDATE " + JobExecutionPlan.TABLE_NAME);
        sql.append(" SET currentXjobState='" + currentXjobState.name() + "', ");
        sql.append(" currentXjobDate='" + currentXjobDate + "'");
        sql.append(" WHERE 1=1");
        sql.append(" AND id = " + id + " ");
        log.debug("SQL:" + sql.toString());
        try {
            super.update(sql.toString());
            return this.queryJobPlan(id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            if (isCloseCon) {
                super.closeConnection();
            }
        }
    }

    public void updateJobPlanCronExpression(int id,
        CronType cronType, String cronExpression, boolean isCloseCon)
        throws DaoException {
        StringBuffer sql =
                new StringBuffer("UPDATE " + JobExecutionPlan.TABLE_NAME);
        sql.append(" SET cronType='" + cronType.name() + "', ");
        sql.append(" cronExpression='" + cronExpression + "'");
        sql.append(" WHERE 1=1");
        sql.append(" AND id = " + id + " ");
        log.debug("SQL:" + sql.toString());
        try {
            super.update(sql.toString());
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            if (isCloseCon) {
                super.closeConnection();
            }
        }
    }

    public JobExecutionPlan loadJobExecutionPlan(int jobExecutionPlanID)
        throws DaoException {
        JobExecutionPlan jobExecutionPlan =
                this.queryJobPlan(jobExecutionPlanID);
        if (null != jobExecutionPlan) {
            List<JobPlanNode> jobPlanNodeList =
                    this.queryJobPlanNodeByJPID(jobExecutionPlan.getId());
            List<XjobMeta> xjobMetaList =
                    this.queryXjobMetaByJPID(jobExecutionPlan.getId());
            jobExecutionPlan.addJobPlanNodes(jobPlanNodeList);
            for (JobPlanNode jobPlanNode : jobPlanNodeList) {
                for (XjobMeta xjobMeta : xjobMetaList) {
                    if (jobPlanNode.getId() == xjobMeta.getJobPlanNodeID()) {
                        jobPlanNode.setXjobMeta(xjobMeta);
                    }
                }
            }
        }
        return jobExecutionPlan;
    }

    public List<JobPlanNode> queryJobPlanNodeByJPID(int jobExecutionPlanID)
        throws DaoException {
        StringBuffer SQL_QUERY_JOBPLANNODE =
                new StringBuffer(SqlTemplate.SQL_QUERY_JOBPLANNODE);
        if (0 != jobExecutionPlanID) {
            SQL_QUERY_JOBPLANNODE.append(" WHERE jobExecutionPlanID="
                                         + jobExecutionPlanID);
            try {
                return super.find(JobPlanNode.class,
                    SQL_QUERY_JOBPLANNODE.toString());
            } catch (SQLException e) {
                e.printStackTrace();
                throw new DaoException(e.getMessage(), e.getCause());
            } finally {
                super.closeConnection();
            }
        }
        return null;
    }

    public JobPlanNode queryJobPlanNodeByJPIDAndNodeName(
        int jobExecutionPlanID, String nodeName, boolean isCloseCon)
        throws DaoException {
        StringBuffer SQL_QUERY_JOBPLANNODE =
                new StringBuffer(SqlTemplate.SQL_QUERY_JOBPLANNODE);
        if (0 != jobExecutionPlanID && StringUtils.isNotBlank(nodeName)) {
            SQL_QUERY_JOBPLANNODE.append(" WHERE jobExecutionPlanID="
                                         + jobExecutionPlanID);
            SQL_QUERY_JOBPLANNODE.append(" AND  name='" + nodeName + "'");
            try {
                return super.findFirst(JobPlanNode.class,
                    SQL_QUERY_JOBPLANNODE.toString());
            } catch (SQLException e) {
                e.printStackTrace();
                throw new DaoException(e.getMessage(), e.getCause());
            } finally {
                if (isCloseCon) {
                    super.closeConnection();
                }
            }
        }
        return null;
    }

    public JobPlanNode queryJobPlanNodeByID(int jobPlanNodeID)
        throws DaoException {
        StringBuffer SQL_QUERY_JOBPLANNODE =
                new StringBuffer(SqlTemplate.SQL_QUERY_JOBPLANNODE);
        if (0 != jobPlanNodeID) {
            SQL_QUERY_JOBPLANNODE.append(" WHERE id=" + jobPlanNodeID);
            try {
                return super.findFirst(JobPlanNode.class,
                    SQL_QUERY_JOBPLANNODE.toString());
            } catch (SQLException e) {
                e.printStackTrace();
                throw new DaoException(e.getMessage(), e.getCause());
            } finally {
                super.closeConnection();
            }
        }
        return null;
    }

    public List<XjobMeta> queryXjobMetaByJPID(int jobExecutionPlanID)
        throws DaoException {
        StringBuffer SQL_QUERY_XJOB =
                new StringBuffer(SqlTemplate.SQL_QUERY_XJOB);
        if (0 != jobExecutionPlanID) {
            SQL_QUERY_XJOB.append(" WHERE jobExecutionPlanID = "
                                  + jobExecutionPlanID + "");
            try {
                return super.find(XjobMeta.class, SQL_QUERY_XJOB.toString());
            } catch (SQLException e) {
                e.printStackTrace();
                throw new DaoException(e.getMessage(), e.getCause());
            } finally {
                super.closeConnection();
            }
        }
        return null;
    }

    public XjobMeta queryXjobMetaByJNID(int jobPlanNodeID) throws DaoException {
        StringBuffer SQL_QUERY_XJOB =
                new StringBuffer(SqlTemplate.SQL_QUERY_XJOB);
        if (0 != jobPlanNodeID) {
            SQL_QUERY_XJOB.append(" WHERE jobPlanNodeID = " + jobPlanNodeID
                                  + "");
            try {
                return super.findFirst(XjobMeta.class,
                    SQL_QUERY_XJOB.toString());
            } catch (SQLException e) {
                e.printStackTrace();
                throw new DaoException(e.getMessage(), e.getCause());
            } finally {
                super.closeConnection();
            }
        }
        return null;
    }

    public Page findPage(final Page page, final String whereSql)
        throws DaoException {
        String querySql =
                "select j.id,j.jobPlanType,j.executePlanVersionID,j.clusterID,j.jepID,j.jepName,j.cronType,j.cronExpression,j.event,"
                        + "j.ignoreError,j.state,j.currentXjobDate,j.currentXjobState,j.currentNode,j.description,j.createDate  "
                        + " from "
                        + JobExecutionPlan.TABLE_NAME
                        + " j where 1=1 ";

        if (StringUtils.isNotBlank(whereSql)) {
            querySql = querySql + whereSql;
        }
        querySql = querySql + " ORDER BY j.clusterID ASC,j.id DESC";

        String countSql =
                "select count(id) " + " from " + JobExecutionPlan.TABLE_NAME
                        + " j where 1=1  ";

        if (StringUtils.isNotBlank(whereSql)) {
            countSql = countSql + whereSql;
        }

        long totalCount;
        try {
            totalCount = super.count(countSql);
            if (totalCount > 0) {
                page.setTotalCount(totalCount);
                List<JobExecutionPlan> result =
                        super.findPage(JobExecutionPlan.class, querySql,
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

    public long count(String sql) throws DaoException {
        try {
            return super.count(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage());
        } finally {
            super.closeConnection();
        }
    }

    public List<String> getClusterIDList() throws DaoException {
        try {
            List<String> clusterIDList = new ArrayList<String>();
            List<Map<String, Object>> map =
                    super.find(SqlTemplate.SQL_DISTINCT_CLUSTERID);
            if (null != map && map.size() > 0) {
                for (Map<String, Object> m : map) {
                    for (String key : m.keySet()) {
                        clusterIDList.add(String.valueOf(m.get(key)));
                    }
                }
            }
            return clusterIDList;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage());
        } finally {
            super.closeConnection();
        }
    }
}
