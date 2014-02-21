package com.zhangyue.hella.engine.web.service;

import java.io.IOException;
import java.util.List;

import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.engine.cluster.IEngineClusterManager;
import com.zhangyue.hella.engine.dao.Page;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.entity.JobCount;
import com.zhangyue.hella.engine.entity.JobPlanState;
import com.zhangyue.hella.engine.entity.NodeInfo;
import com.zhangyue.hella.engine.manager.IJobPlanManager;
import com.zhangyue.hella.engine.parser.IJobExecutionPlanParser;

/**
 * 作业计划服务接口
 * 
 * @date 2013-8-19 下午3:07:21
 * @author scott
 */
public interface JobPlanService {

    public void initialize(IEngineClusterManager cluserManager, IJobPlanManager jobPlanManager, IJobExecutionPlanParser jobExecutionPlanParser);
    
    public Page queryJobPlans(Page page, String clusterID, String jepName,
        JobExecutionPlan.State state);

    public JobExecutionPlan loadJobExecutionPlan(int jobExecutionPlanID)
        throws Exception;

    public boolean resumeJob(int vsID);

    public boolean pauseJob(int vsID);

    public List<JobCount> countJobInfo() throws Exception;

    public boolean executeQJob(int jobExecutionPlanID) throws Exception;

    public boolean executeJobPlanNode(int jobPlanNodeID, String args)
        throws Exception;

    public List<String> getClusterIDs();

    public List<JobPlanState> queryJobPlanStates(String clusterID,
        String[] jepIDs) throws Exception;
    
    public String queryJobPlanState(int jepID) throws Exception;
    
    public boolean validateXMLJobPlan(String jobplanpath) throws Exception;

    public boolean submitJobPlan(String clusterID, String jobplanpath)
        throws Exception;

    public boolean delJobExecutionPlan(int jobExecutionPlanID) throws Exception;

    public void changeJobExecutionPlanCronExpression(int jobExecutionPlanID,
        CronType cronType, String cronExpression) throws Exception;
    
    public List<NodeInfo> getExecutorClusterInfoList() throws IOException;
    
    public String getEngineNodeState();

}
