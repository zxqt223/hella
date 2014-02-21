package com.zhangyue.hella.engine.core.workflow.node.handler;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zhangyue.hella.common.protocol.JobProgress;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.common.util.JobPlanNodeType;
import com.zhangyue.hella.engine.core.workflow.WorkflowException;
import com.zhangyue.hella.engine.core.workflow.node.NodeContext;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;
import com.zhangyue.hella.engine.db.entity.XjobState;
import com.zhangyue.hella.engine.manager.IJobPlanManager;
import com.zhangyue.hella.engine.manager.IJobStateManager;

public class StartNodeHandler extends NodeHandler {

    private static Logger LOG = Logger.getLogger(StartNodeHandler.class);
    private JobPlanNode jobPlanNode;
    private IJobStateManager jobStateManager;
    private IJobPlanManager jobPlanManager;

    public StartNodeHandler(JobPlanNode jobPlanNode, IJobStateManager jobStateManager, IJobPlanManager jobPlanManager){
        this.jobPlanNode = jobPlanNode;
        this.jobStateManager = jobStateManager;
        this.jobPlanManager = jobPlanManager;
    }

    public boolean enter(NodeContext xjobContext) throws WorkflowException {
        if (StringUtils.isBlank(xjobContext.getCurrentExecutorKey())) {
            throw new WorkflowException("this currentExecutorKey has not initialized ");
        }

        try {
            XjobState xjobState = new XjobState();
            xjobState.setJobPlanNodeID(jobPlanNode.getId());
            xjobState.setCurrentExecutorKey(xjobContext.getCurrentExecutorKey());
            xjobState.setJobPlanNodeName(jobPlanNode.getName());
            xjobState.setRunTime(DateUtil.dateFormaterBySeconds(new Date()));
            xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.INIT);
            xjobState = jobStateManager.addJobState(xjobState);

            JobProgress jobProgress = new JobProgress();
            jobProgress.jobPlanNodeState = JobPlanNodeState.INIT.name();
            jobProgress.xjobStateID = xjobState.getId();

            xjobContext.setJobPlanNodeState(JobPlanNodeState.INIT);
            xjobContext.setCurrentJobProgress(jobProgress);

            return true;
        } catch (Exception e) {
            LOG.error("Fail to add xjob state.",e);
        }
        return false;
    }

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

        if (StringUtils.isNotBlank(this.jobPlanNode.getToNode())) {
            xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.RESULT_SUCCESS);
        } else {
            xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.RESULT_ERROR);
        }
        try {
            jobStateManager.updJobState(xjobState);
            int jpid = xjobContext.getWorkflowInstance().getWorkflowManager().getJobExecutionPlan().getId();
            jobPlanManager.changeJobExecutionPlanCurrentXjobState(jpid, JobExecutionPlan.CurrentXjobState.doing);
        } catch (Exception e) {
            LOG.error("Fail to update xjob state.",e);
        }

        return this.jobPlanNode.getToNode();
    }

    public void kill(NodeContext xjobContext) {

    }

    public void fail(NodeContext xjobContext) {

    }

    @Override
    public JobPlanNodeType getJobPlanNodeType() {
        return JobPlanNodeType.start;
    }

}
