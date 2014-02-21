package com.zhangyue.hella.engine.core.workflow.node.handler;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zhangyue.hella.common.protocol.JobProgress;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.common.util.JobPlanNodeType;
import com.zhangyue.hella.common.util.XjobExecutorType;
import com.zhangyue.hella.engine.core.workflow.WorkflowException;
import com.zhangyue.hella.engine.core.workflow.node.NodeContext;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;
import com.zhangyue.hella.engine.db.entity.XjobState;
import com.zhangyue.hella.engine.manager.IJobEventManager;
import com.zhangyue.hella.engine.manager.IJobPlanManager;
import com.zhangyue.hella.engine.manager.IJobStateManager;

public class EndNodeHandler extends NodeHandler {

    private static Logger LOG = Logger.getLogger(EndNodeHandler.class);
    private JobPlanNode jobPlanNode;
    private IJobStateManager jobStateManager;
    private IJobPlanManager jobPlanManager;
    private IJobEventManager jobEventManager;

    public EndNodeHandler(JobPlanNode jobPlanNode, IJobStateManager jobStateManager, IJobPlanManager jobPlanManager,
                          IJobEventManager jobEventManager){
        this.jobPlanNode = jobPlanNode;
        this.jobStateManager = jobStateManager;
        this.jobPlanManager = jobPlanManager;
        this.jobEventManager = jobEventManager;
    }

    @Override
    public boolean enter(NodeContext xjobContext) throws Exception{
        XjobState xjobState = new XjobState();
        xjobState.setJobPlanNodeID(jobPlanNode.getId());
        xjobState.setCurrentExecutorKey(xjobContext.getCurrentExecutorKey());
        xjobState.setJobPlanNodeName(jobPlanNode.getName());
        xjobState.setRunTime(DateUtil.dateFormaterBySeconds(new Date()));
        xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.INIT);

        if (null != jobPlanNode.getXjobMeta()) {
            jobPlanManager.executeXJob(xjobContext.getCurrentExecutorKey(), jobPlanNode, xjobState.getId(),
                XjobExecutorType.Auto, xjobContext.getArgs());
            xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.NOTIFY_SUCCESS);
        }
        try {
            xjobState = jobStateManager.addJobState(xjobState);
        } catch (Exception e) {
            LOG.error("Fail to update job state.", e);
        }

        JobProgress jobProgress = new JobProgress();
        jobProgress.jobPlanNodeState = xjobState.getJobPlanNodeStateEnum().name();
        jobProgress.xjobStateID = xjobState.getId();

        xjobContext.setJobPlanNodeState(xjobState.getJobPlanNodeStateEnum());
        xjobContext.setCurrentJobProgress(jobProgress);

        return false;
    }

    @Override
    public List<String> multiExit(NodeContext xjobContext) throws WorkflowException {
        return null;
    }

    @Override
    public String exit(NodeContext xjobContext) throws Exception {
        if (StringUtils.isBlank(xjobContext.getCurrentExecutorKey())) {
            throw new WorkflowException("this currentExecutorKey has not initialized ");
        }
        if (null == xjobContext.getJobProgress() || 0 == xjobContext.getJobProgress().xjobStateID) {
            throw new WorkflowException("this jobProgress has not initialized ");
        }

        XjobState xjobState = jobStateManager.queryXjobState(xjobContext.getJobProgress().xjobStateID);
        if (null == xjobState) {
            throw new WorkflowException("this xjobState is null");
        }
        xjobState.setRunTime(DateUtil.dateFormaterBySeconds(new Date()));
        xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.RESULT_SUCCESS);
        try {
            jobStateManager.updJobState(xjobState);
            int jpid = xjobContext.getWorkflowInstance().getWorkflowManager().getJobExecutionPlan().getId();
            jobPlanManager.changeJobExecutionPlanCurrentXjobState(jpid, JobExecutionPlan.CurrentXjobState.success);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 结束时 判断是否发出事件
        JobExecutionPlan jobExecutionPlan = jobPlanManager.queryJobPlan(jobPlanNode.getJobExecutionPlanID());
        if (null == jobExecutionPlan || StringUtils.isBlank(jobExecutionPlan.getEvent())) {
            return null;
        }
        if (StringUtils.isNotBlank(jobExecutionPlan.getEvent())) {
            jobEventManager.produceJobEvent(jobExecutionPlan.getEvent());
        }
        return null;
    }

    @Override
    public JobPlanNodeType getJobPlanNodeType() {
        return JobPlanNodeType.end;
    }

}
