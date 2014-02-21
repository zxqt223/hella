package com.zhangyue.hella.engine.core.workflow.node.handler;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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
import com.zhangyue.hella.engine.manager.IJobPlanManager;
import com.zhangyue.hella.engine.manager.IJobStateManager;

public class FailNodeHandler extends NodeHandler {

    private JobPlanNode jobPlanNode;
    private IJobStateManager jobStateManager;
    private IJobPlanManager jobPlanManager;

    public FailNodeHandler(JobPlanNode jobPlanNode, IJobStateManager jobStateManager, IJobPlanManager jobPlanManager){
        this.jobPlanNode = jobPlanNode;
        this.jobStateManager = jobStateManager;
        this.jobPlanManager = jobPlanManager;
    }

    public boolean enter(NodeContext xjobContext) throws Exception {
        XjobState xjobState = new XjobState();
        xjobState.setJobPlanNodeID(jobPlanNode.getId());
        xjobState.setCurrentExecutorKey(xjobContext.getCurrentExecutorKey());
        xjobState.setJobPlanNodeName(jobPlanNode.getName());
        xjobState.setRunTime(DateUtil.dateFormaterBySeconds(new Date()));
        xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.INIT);
        xjobState = jobStateManager.addJobState(xjobState);
        if (null != jobPlanNode.getXjobMeta()) {
            try {
                jobPlanManager.executeXJob(xjobContext.getCurrentExecutorKey(), jobPlanNode, xjobState.getId(),
                    XjobExecutorType.Auto, xjobContext.getArgs());
                xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.NOTIFY_SUCCESS);
            } catch (Exception e) {
                xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.NOTIFY_FAIL);
                e.printStackTrace();
            }
            jobStateManager.updJobState(xjobState);
        }

        JobProgress jobProgress = new JobProgress();
        jobProgress.jobPlanNodeState = xjobState.getJobPlanNodeStateEnum().name();
        jobProgress.xjobStateID = xjobState.getId();

        xjobContext.setJobPlanNodeState(xjobState.getJobPlanNodeStateEnum());
        xjobContext.setCurrentJobProgress(jobProgress);
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

        xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.RESULT_SUCCESS);

        try {
            jobStateManager.updJobState(xjobState);
            int jpid = xjobContext.getWorkflowInstance().getWorkflowManager().getJobExecutionPlan().getId();
            jobPlanManager.changeJobExecutionPlanCurrentXjobState(jpid, JobExecutionPlan.CurrentXjobState.error);

            List<XjobState> xjobStateList =
                    jobStateManager.queryXjobStateByCurrentExecutorKey(xjobContext.getCurrentExecutorKey());
            for (XjobState x : xjobStateList) {
                if (x.getJobPlanNodeStateEnum().isRunningState()) {
                    x.setRunInfo("由[" + x.getJobPlanNodeStateEnum().getStateName() + "]自动转为[失败]" + x.getRunInfo());
                    x.setRunTime(DateUtil.dateFormaterBySeconds(new Date()));
                    x.setJobPlanNodeStateEnum(JobPlanNodeState.RESULT_ERROR);
                    jobStateManager.updJobState(x);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void kill(NodeContext xjobContext) {
    }

    public void fail(NodeContext xjobContext) {
    }

    @Override
    public JobPlanNodeType getJobPlanNodeType() {
        return JobPlanNodeType.fail;
    }

}
