package com.zhangyue.hella.engine.web.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.common.util.XjobExecutorType;
import com.zhangyue.hella.engine.cluster.IEngineClusterManager;
import com.zhangyue.hella.engine.dao.JobExecutionPlanDao;
import com.zhangyue.hella.engine.dao.Page;
import com.zhangyue.hella.engine.dao.XjobStateDao;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlanSubmissionContext;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;
import com.zhangyue.hella.engine.db.entity.XjobState;
import com.zhangyue.hella.engine.entity.JobCount;
import com.zhangyue.hella.engine.entity.JobPlanState;
import com.zhangyue.hella.engine.entity.NodeInfo;
import com.zhangyue.hella.engine.manager.IJobPlanManager;
import com.zhangyue.hella.engine.parser.IJobExecutionPlanParser;

public class JobPlanServiceImpl implements JobPlanService {

    private final static Logger LOG = Logger.getLogger(JobPlanServiceImpl.class);
    private final static String JOB_EXECUTION_PLAN_COUNT_SQL =
            "select j.clusterID as clusterID,j.state as state,count(j.id) as count  from   SCH_JOB_EXECUTION_PLAN  j  group by j.clusterID,j.state";

    private JobExecutionPlanDao jobDao;
    private XjobStateDao xjobStateDao;
    private IEngineClusterManager clusterManager;
    private IJobPlanManager jobPlanManager;
    private IJobExecutionPlanParser jobExecutionPlanParser;

    protected JobPlanServiceImpl(){
    }

    public void initialize(IEngineClusterManager clusterManager, IJobPlanManager jobPlanManager,
        IJobExecutionPlanParser jobExecutionPlanParser) {
        this.jobDao = new JobExecutionPlanDao();
        this.xjobStateDao = new XjobStateDao();
        this.clusterManager = clusterManager;
        this.jobPlanManager = jobPlanManager;
        this.jobExecutionPlanParser = jobExecutionPlanParser;
    }

    public Page queryJobPlans(Page page, String clusterID, String jepName, JobExecutionPlan.State state){
        StringBuffer jobPlanQuerySQL = new StringBuffer();
        if (StringUtils.isNotBlank(clusterID)) {
            jobPlanQuerySQL.append(" AND j.clusterID LIKE '%" + clusterID + "%' ");
        }
        if (StringUtils.isNotBlank(jepName)) {
            jobPlanQuerySQL.append(" AND j.jepName LIKE '%" + jepName + "%' ");
        }
        if (null != state) {
            jobPlanQuerySQL.append("  AND j.state ='" + state + "'");
        }
        try {
            page = jobDao.findPage(page, jobPlanQuerySQL.toString());
        } catch (DaoException e) {
           LOG.error("Fail to find page.SQL:"+jobPlanQuerySQL.toString(),e);
        }

        return page;
    }

    public JobExecutionPlan loadJobExecutionPlan(int jobExecutionPlanID) throws Exception {
        return jobDao.loadJobExecutionPlan(jobExecutionPlanID);
    }

    public boolean resumeJob(int jobExecutionPlanID) {
        return jobPlanManager.resumeJobExecutionPlan(jobExecutionPlanID);
    }

    public boolean pauseJob(int jobExecutionPlanID) {
        JobExecutionPlan jep = null;
        try {
            jep = loadJobExecutionPlan(jobExecutionPlanID);
        } catch (Exception e) {
            LOG.error("Fail to find job execution plan.jobExecutionPlanID:"+jobExecutionPlanID,e);
            return false;
        }
        return jep == null ? false : jobPlanManager.pauseJobExecutionPlan(jep);
    }

    @Override
    public List<String> getClusterIDs() {
        List<String> custerIDs = null;
        try {
            custerIDs = jobDao.getClusterIDList();
        } catch (DaoException e) {
           LOG.error("Fail to query cluster id list from database!",e);
        }
        return custerIDs;
    }

    @Override
    public List<JobCount> countJobInfo() throws Exception {
        List<String> custerIDs = this.getClusterIDs();
        if (null == custerIDs) {
            return null;
        }
        List<JobCount> jobCountList = new ArrayList<JobCount>();
        List<Map<String, Object>> jobExecutionplanRs = null;
        List<Map<String, Object>> jobstateRs = null;
        
        String jobStateQuerySQL =
                "SELECT jn.executorClusterID as executorClusterID,xs.jobPlanNodeState as jobPlanNodeState, count(1) as count FROM "
                        + XjobState.TABLE_NAME + " xs," + JobPlanNode.TABLE_NAME + " jn  "
                        + "where xs.jobPlanNodeID=jn.id and jn.type='action' AND xs.runTime>"
                        + DateUtil.getDateHourAgo(24 * 3) + " group by jn.executorClusterID,xs.jobPlanNodeState";

        jobExecutionplanRs = xjobStateDao.find(JOB_EXECUTION_PLAN_COUNT_SQL, null);
        jobstateRs = xjobStateDao.find(jobStateQuerySQL, null);

        for (String custerIDStr : custerIDs) {
            if (StringUtils.isBlank(custerIDStr)) {
                continue;
            }
            JobCount jobCount = new JobCount();
            jobCount.setClusterID(custerIDStr);
            jobCountList.add(jobCount);

            /** 处理执行计划完成结果信息*/
            if (null != jobExecutionplanRs) {
                for (Map<String, Object> map : jobExecutionplanRs) {
                    String clusterID = String.valueOf(map.get("clusterID"));
                    if (!custerIDStr.equals(clusterID)) {
                        continue;
                    }
                    JobExecutionPlan.State jobExecutionPlanState =
                            JobExecutionPlan.State.valueOf(map.get("state").toString());
                    long count = Long.valueOf(map.get("count").toString());
                    if (jobExecutionPlanState == JobExecutionPlan.State.able) {
                        jobCount.setAbleJobPlanNum(count);
                    }
                    if (jobExecutionPlanState == JobExecutionPlan.State.disable) {
                        jobCount.setDisableJobPlanNum(count);
                    }
                }
            }
            /** 处理作业状态信息*/
            if (null != jobstateRs) {
                for (Map<String, Object> map : jobstateRs) {
                    String clusterID = String.valueOf(map.get("executorClusterID"));
                    if (!custerIDStr.equals(clusterID)) {
                        continue;
                    }
                    JobPlanNodeState jobPlanNodeState =
                            JobPlanNodeState.valueOf(map.get("jobPlanNodeState").toString());
                    long count = Long.valueOf(map.get("count").toString());
                    if (null == jobPlanNodeState) {
                        continue;
                    }
                    if (jobPlanNodeState.isFailState()) {
                        jobCount.setFailXjobsNum(jobCount.getFailXjobsNum() + count);
                    }
                    if (jobPlanNodeState == JobPlanNodeState.RESULT_SUCCESS) {
                        jobCount.setSuccessXjobsNum(count);
                    }
                    if (jobPlanNodeState.isRunningState()) {
                        jobCount.setRunningXjobsNum(jobCount.getRunningXjobsNum() + count);
                    }
                }
            }
        }
        return jobCountList;
    }

    @Override
    public boolean executeQJob(int jobExecutionPlanID) {
        try {
            jobPlanManager.startJobPlan(jobExecutionPlanID);
        } catch (Exception e) {
            LOG.error("Fail to execute qjob.",e);
            return false;
        }
        return true;
    }

    @Override
    public boolean executeJobPlanNode(int jobPlanNodeID, String args) {
        if (jobPlanNodeID <= 0) {
            return false;
        }
        try {
            return jobPlanManager.executeJobPlanNode(jobPlanNodeID, XjobExecutorType.Once, args);
        } catch (Exception e) {
            LOG.error("Fail to execute job plan node.",e);
            return false;
        }
    }

    @Override
    public List<JobPlanState> queryJobPlanStates(String clusterID, String[] jepIDs) throws Exception {
        return jobPlanManager.queryJobPlanStates(clusterID, jepIDs);
    }

    @Override
    public String queryJobPlanState(int jepID) throws Exception {
        return jobPlanManager.queryJobPlanState(jepID);
    }

    @Override
    public boolean validateXMLJobPlan(String jobplanpath) throws Exception {
        return jobExecutionPlanParser.validateXMLByXSD(jobplanpath);
    }

    @Override
    public boolean submitJobPlan(String clusterID, String jobplanpath) throws Exception {
        JobExecutionPlanSubmissionContext jepSubmissionContext =
                jobExecutionPlanParser.getJobExecutionPlanSubmissionContext(clusterID, jobplanpath);
        if (jepSubmissionContext == null) {
            LOG.warn("Can't analysis job plan :" + jobplanpath + " to memory object.");
            return false;
        }
        List<String> ids = new ArrayList<String>();
        for (JobExecutionPlan jep : jepSubmissionContext.getJobExecutionPlans()) {
            ids.add(jep.getJepID());
        }
        if (jobPlanManager.isJobPlansExist(ids)) {
            LOG.warn("There exist job plan id at scheduler engine in this jobplan file : " + jobplanpath);
            return false;
        }
        return jobPlanManager.registerJobExecutionPlanSubmissionContext(jepSubmissionContext);
    }

    @Override
    public boolean delJobExecutionPlan(int jobExecutionPlanID) throws Exception {
        return jobPlanManager.deleteJobExecutionPlan(jobExecutionPlanID);
    }

    @Override
    public void changeJobExecutionPlanCronExpression(int jobExecutionPlanID, CronType cronType, String cronExpression)
        throws Exception {
        jobPlanManager.changeJobExecutionPlanCronExpression(jobExecutionPlanID, cronType, cronExpression);
    }

    @Override
    public List<NodeInfo> getExecutorClusterInfoList() throws IOException {
        return clusterManager.getExecutorClusterInfoList();
    }

    public String getEngineNodeState() {
        return clusterManager.isMasterNode() ? SysService.MASTER_NODE_STATE : SysService.SLAVE_NODE_STATE;
    }
}
