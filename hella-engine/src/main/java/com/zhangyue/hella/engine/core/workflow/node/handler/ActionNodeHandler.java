package com.zhangyue.hella.engine.core.workflow.node.handler;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zhangyue.hella.common.protocol.JobProgress;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.common.util.JobPlanNodeType;
import com.zhangyue.hella.common.util.XjobExecutorType;
import com.zhangyue.hella.engine.core.workflow.WorkflowException;
import com.zhangyue.hella.engine.core.workflow.node.NodeContext;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;
import com.zhangyue.hella.engine.db.entity.XjobState;
import com.zhangyue.hella.engine.manager.IJobPlanManager;
import com.zhangyue.hella.engine.manager.IJobStateManager;

/**
 * 作业流中Action类型的节点处理器
 * 
 * @date 2014-1-3
 * @author scott
 */
public class ActionNodeHandler extends NodeHandler {

    private static final Logger LOG = Logger.getLogger(ActionNodeHandler.class);
    private JobPlanNode jobPlanNode;
    private IJobStateManager jobStateManager;
    private IJobPlanManager jobPlanManager;
    private NodeContext xjobContext;

    public ActionNodeHandler(JobPlanNode jobPlanNode, IJobStateManager jobStateManager, IJobPlanManager jobPlanManager){
        this.jobPlanNode = jobPlanNode;
        this.jobStateManager = jobStateManager;
        this.jobPlanManager = jobPlanManager;
    }

    @Override
    public boolean enter(NodeContext xjobContext) throws Exception{
        this.xjobContext = xjobContext;
        XjobState xjobState = new XjobState();
        xjobState.setJobPlanNodeID(jobPlanNode.getId());
        xjobState.setCurrentExecutorKey(xjobContext.getCurrentExecutorKey());
        xjobState.setJobPlanNodeName(jobPlanNode.getName());
        xjobState.setRunTime(DateUtil.dateFormaterBySeconds(new Date()));
        xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.INIT);
        try {
            xjobState = jobStateManager.addJobState(xjobState);
        } catch (Exception e) {
            LOG.error("Fail to add job state,but it doesn't effect job running.",e);
        }

        JobProgress jobProgress = new JobProgress();
        jobProgress.jobPlanNodeState = JobPlanNodeState.INIT.name();
        jobProgress.xjobStateID = xjobState.getId();

        xjobContext.setJobPlanNodeState(JobPlanNodeState.INIT);
        xjobContext.setCurrentJobProgress(jobProgress);

        jobPlanManager.executeXJob(xjobContext.getCurrentExecutorKey(), jobPlanNode, xjobState.getId(), XjobExecutorType.Auto,
            xjobContext.getArgs());
        xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.NOTIFY_SUCCESS);
        try {
            jobStateManager.updJobState(xjobState);
        } catch (Exception e) {
            LOG.error("Fail to update xjob state to database.job plan node state:"+JobPlanNodeState.NOTIFY_SUCCESS.name(), e);
        }
        return true;
    }

    @Override
    public String exit(NodeContext xjobContext) throws Exception {
        if (StringUtils.isBlank(xjobContext.getCurrentExecutorKey())) {
            throw new WorkflowException("this currentExecutorKey has not initialized ");
        }
        if (null == xjobContext.getJobProgress() || 0 == xjobContext.getJobProgress().xjobStateID) {
            throw new WorkflowException("this jobProgress has not initialized ");
        }

        JobPlanNodeState jobPlanNodeState = JobPlanNodeState.valueOf(xjobContext.getJobProgress().jobPlanNodeState);
        if (!jobPlanNodeState.isResultState()) {
            throw new WorkflowException("this jobPlanNodeState is not resultState ");
        }

        XjobState xjobState = jobStateManager.queryXjobState(xjobContext.getJobProgress().xjobStateID);
        if (null == xjobState) {
            throw new WorkflowException("this xjobState is null");
        }
        xjobState.setRunTime(xjobContext.getJobProgress().runTime);
        xjobState.setRunInfo(xjobContext.getJobProgress().runInfo);
        xjobState.setFinishedPercent(xjobContext.getJobProgress().progress);
        xjobState.setJobPlanNodeState(xjobContext.getJobProgress().jobPlanNodeState);
        try {
            jobStateManager.updJobState(xjobState);
        } catch (Exception e) {
            LOG.error("Fail to update xjob state.",e);
        }
        if (xjobState.getJobPlanNodeStateEnum() == JobPlanNodeState.RESULT_SUCCESS) {
            return jobPlanNode.getOkNode();
        }
        if (xjobState.getJobPlanNodeStateEnum() == JobPlanNodeState.RESULT_ERROR) {
            /* 是否重新执行 */
            if ((jobPlanNode.getErrorMaxRedoTimes() > 0)
                && (xjobState.getExecuteTimes() < jobPlanNode.getErrorMaxRedoTimes())) {
                redoXjob(xjobState);
            } else {
                return jobPlanNode.getErrorNode();
            }
        }

        return null;
    }

    private void redoXjob(XjobState xjobState){
        LOG.debug(jobPlanNode.getName() + " begin redo , it is has redo " + xjobState.getExecuteTimes() + " times");
        // 容错间隔
        if (jobPlanNode.getErrorRedoPeriod() > 0) {
            LOG.info(jobPlanNode.getName() + " error redo Period ,it sleep " + jobPlanNode.getErrorRedoPeriod()
                     + " min");
            try {
                Thread.sleep(jobPlanNode.getErrorRedoPeriod() * Constant.MIN_IN_RATE);
            } catch (InterruptedException e) {
            }
        }

        jobPlanManager.executeXJob(xjobContext.getCurrentExecutorKey(),jobPlanNode, xjobState.getId(), XjobExecutorType.Auto, null);
        xjobState.setExecuteTimes(xjobState.getExecuteTimes() + 1);
        xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.NOTIFY_SUCCESS);
        try {
            jobStateManager.updJobState(xjobState);
        } catch (Exception e) {
            LOG.error("Fail to update job plan node state:"+JobPlanNodeState.NOTIFY_SUCCESS.name(), e);
        }
    }

    @Override
    public JobPlanNodeType getJobPlanNodeType() {
        return JobPlanNodeType.action;
    }

}
