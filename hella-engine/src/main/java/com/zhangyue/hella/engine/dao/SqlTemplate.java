package com.zhangyue.hella.engine.dao;

import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;
import com.zhangyue.hella.engine.db.entity.JobPlanSubscribe;
import com.zhangyue.hella.engine.db.entity.NoConsumptionJobEvent;
import com.zhangyue.hella.engine.db.entity.SystemLog;
import com.zhangyue.hella.engine.db.entity.XjobMeta;
import com.zhangyue.hella.engine.db.entity.XjobState;

/**
 * @Descriptions The class SqlTemplate.java's implementation：系统所有SQL
 * @author scott
 * @date 2013-8-19 下午2:40:36
 * @version 1.0
 */
public class SqlTemplate {

    /** JobExecutionPlan */
    public static final String SQL_QUERY_JOBEXECUTIONPLAN =
            "SELECT id,jobPlanType,executePlanVersionID,clusterID,jepID,jepName,cronType,cronExpression,event,ignoreError,state,currentXjobDate,currentXjobState,currentNode,description,createDate"
                    + " FROM " + JobExecutionPlan.TABLE_NAME;

    public static final String SQL_QUERY_JOBEXECUTIONPLAN_VERSION =
            "SELECT j.id,j.jobPlanType,j.executePlanVersionID,j.clusterID,j.jepID,j.jepName,"
                    + "j.cronType,j.cronExpression,j.event,j.ignoreError,j.state,j.currentXjobDate,j.currentXjobState,j.currentNode,j.description,j.createDate "
                    + " FROM " + JobExecutionPlan.TABLE_NAME + " j WHERE 1=1 ";

    public static final String SQL_QUERY_JOBPLANSTATE =
            "SELECT clusterID,jepID,jepName,currentXjobDate,currentXjobState FROM "
                    + JobExecutionPlan.TABLE_NAME + " WHERE 1=1 AND state = '"
                    + JobExecutionPlan.State.able.name() + "' ";

    public static final String SQL_INSERT_JOBEXECUTIONPLAN =
            "INSERT INTO "
                    + JobExecutionPlan.TABLE_NAME
                    + " (jobPlanType,executePlanVersionID,clusterID,jepID,jepName,cronType,cronExpression,event,ignoreError,state,currentXjobDate,currentXjobState,currentNode,description,createDate) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static final String SQL_DEL_JOBEXECUTIONPLAN =
            "DELETE  FROM " + JobExecutionPlan.TABLE_NAME + " WHERE id=?";

    /** JobPlanNode */

    public static final String SQL_INSERT_JOBPLANNODE =
            "INSERT INTO "
                    + JobPlanNode.TABLE_NAME
                    + " (jobExecutionPlanID,type,name,executorClusterID,forkName,joinName,toNode,okNode,errorNode,delayType,delayTime,errorMaxRedoTimes,errorRedoPeriod) "
                    + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
    public static final String SQL_QUERY_JOBPLANNODE =
            "SELECT id,jobExecutionPlanID,type,name,executorClusterID,forkName,joinName,toNode,okNode,errorNode,delayType,delayTime,errorMaxRedoTimes,errorRedoPeriod "
                    + " FROM " + JobPlanNode.TABLE_NAME;

    public static final String SQL_DEL_JOBPLANNODE =
            "DELETE  FROM " + JobPlanNode.TABLE_NAME
                    + " WHERE jobExecutionPlanID=?";

    /** XjobMeta */

    public static final String SQL_INSERT_XJOB =
            "INSERT INTO "
                    + XjobMeta.TABLE_NAME
                    + " (jobExecutionPlanID,jobPlanNodeID,mode,executeUser,executionContent,jobClassName,args,description) "
                    + " VALUES(?,?,?,?,?,?,?,?)";

    public static final String SQL_QUERY_XJOB =
            "SELECT id,jobExecutionPlanID,jobPlanNodeID,mode,executeUser,executionContent,jobClassName,args,description FROM "
                    + XjobMeta.TABLE_NAME;

    public static final String SQL_DEL_XJOB = "DELETE  FROM "
                                              + XjobMeta.TABLE_NAME
                                              + " WHERE jobExecutionPlanID=?";

    /** XjobState */

    public static final String SQL_INSERT_XJOB_STATE =
            "INSERT INTO "
                    + XjobState.TABLE_NAME
                    + " (jobPlanNodeID,currentExecutorKey,runInfo,runTime,executeTimes,finishedPercent,jobPlanNodeState) "
                    + "VALUES(?,?,?,?,?,?,?)";
    public static final String SQL_QUERY_XJOB_STATE =
            "SELECT id,jobPlanNodeID,currentExecutorKey,runInfo,runTime,executeTimes,finishedPercent,jobPlanNodeState FROM "
                    + XjobState.TABLE_NAME;

    public static final String SQL_QUERY_XJOB_STATE_ERROR =
            "SELECT t.id,t.jobPlanNodeID,t.currentExecutorKey,t.runInfo,t.runTime,t.executeTimes,"
                    + "t.finishedPercent,t.jobPlanNodeState FROM "
                    + XjobState.TABLE_NAME
                    + " t, "
                    + JobExecutionPlan.TABLE_NAME
                    + " j "
                    + "  WHERE j.ignoreError="
                    + true
                    + " AND (jobPlanNodeState='notifyFail' OR jobPlanNodeState='dispatchFail' OR jobPlanNodeState='resultError') ";

    /** SystemLog */

    public static final String SQL_INSERT_SYSTEM_LOG =
            "INSERT INTO " + SystemLog.TABLE_NAME
                    + " (operatorName,ip,logType,logContent,createDate) "
                    + "VALUES(?,?,?,?,?)";
    public static final String SQL_QUERY_SYSTEM_LOG =
            "SELECT id,operatorName,ip,logType,logContent,createDate FROM "
                    + SystemLog.TABLE_NAME + " WHERE 1=1 ";

    /** JobPlanSubscribe */

    public static final String SQL_INSERT_JOBPLAN_SUBSCRIBE =
            "INSERT INTO " + JobPlanSubscribe.TABLE_NAME
                    + " (clusterID,userEmail,userPhoneNumber,state) "
                    + "VALUES(?,?,?,?)";

    public static final String SQL_QUERY_JOBPLAN_SUBSCRIBE =
            "SELECT id,clusterID,userEmail,userPhoneNumber,state FROM "
                    + JobPlanSubscribe.TABLE_NAME;

    public static final String SQL_DEL_JOBPLAN_SUBSCRIBE =
            "DELETE FROM " + JobPlanSubscribe.TABLE_NAME;
    public static final String SQL_UPDATE_JOBPLAN_SUBSCRIBE =
            "UPDATE " + JobPlanSubscribe.TABLE_NAME + " SET state=? WHERE 1=1 ";

    /** 多表联合查询 */
    public static final String SQL_QUERY_S_N_P_V =
            "SELECT s.id,p.jobPlanType,p.clusterID,p.jepName,"
                    + "n.name as jobPlanNodeName,n.executorClusterID as executorClusterID,n.type as jobPlanNodeType,s.runInfo,s.runTime,s.executeTimes,s.finishedPercent,s.jobPlanNodeState,s.currentExecutorKey"
                    + " FROM "
                    + XjobState.TABLE_NAME
                    + " s,"
                    + JobPlanNode.TABLE_NAME
                    + " n,"
                    + JobExecutionPlan.TABLE_NAME
                    + " p "
                    + " WHERE s.jobPlanNodeID=n.id AND n.jobExecutionPlanID=p.id ";

    /** 统计sql */

    public static final String SQL_COUNT_XJOB = "SELECT COUNT(id) FROM "
                                                + XjobMeta.TABLE_NAME;

    public static final String SQL_DISTINCT_CLUSTERID =
            "SELECT DISTINCT clusterID  FROM " + JobExecutionPlan.TABLE_NAME;

    public static final String SQL_DISTINCT_JEPID_NAME =
            "SELECT DISTINCT jepID,jepName  FROM "
                    + JobExecutionPlan.TABLE_NAME;

    /** NoConsumptionJobEvent begin **/
    public static final String SQL_INSERT_NO_CONSUMPTION_JOB_EVENT =
            "INSERT INTO " + NoConsumptionJobEvent.TABLE_NAME
                    + " (name,createDate) " + "VALUES(?,?)";

    public static final String SQL_QUERY_NO_CONSUMPTION_JOB_EVENT =
            "SELECT id,name,createDate FROM "
                    + NoConsumptionJobEvent.TABLE_NAME + " WHERE 1=1 ";

    public static final String SQL_DEL_NO_CONSUMPTION_JOB_EVENT =
            "DELETE  FROM " + NoConsumptionJobEvent.TABLE_NAME;
    /** NoConsumptionJobEvent end **/
}
