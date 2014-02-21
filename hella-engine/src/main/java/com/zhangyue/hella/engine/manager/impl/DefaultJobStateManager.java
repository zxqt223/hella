package com.zhangyue.hella.engine.manager.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.common.protocol.JobEvent;
import com.zhangyue.hella.common.protocol.JobProgress;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.common.util.XjobExecutorType;
import com.zhangyue.hella.engine.core.workflow.WorkflowContext;
import com.zhangyue.hella.engine.core.workflow.IWorkflowInstance;
import com.zhangyue.hella.engine.core.workflow.node.JobPlanNodeContext;
import com.zhangyue.hella.engine.core.workflow.node.NodeContext;
import com.zhangyue.hella.engine.dao.JobExecutionPlanDao;
import com.zhangyue.hella.engine.dao.XjobStateDao;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;
import com.zhangyue.hella.engine.db.entity.XjobState;
import com.zhangyue.hella.engine.db.entity.JobPlanNode.DelayType;
import com.zhangyue.hella.engine.dispatcher.IEventDispatcher;
import com.zhangyue.hella.engine.manager.IJobPlanManager;
import com.zhangyue.hella.engine.manager.IJobPlanSubscribeManager;
import com.zhangyue.hella.engine.manager.IJobStateManager;
import com.zhangyue.hella.engine.util.EngineConstant;

/**
 * @Descriptions The class DefaultJobStateManagerImpl.java's
 *               implementation：Xjob状态管理
 * @author scott
 * @date 2013-8-19 下午2:51:54
 * @version 1.0
 */
public class DefaultJobStateManager implements IJobStateManager {

    private static Logger LOG = Logger.getLogger(DefaultJobStateManager.class);
    private XjobStateDao xjobStateDao = new XjobStateDao();
    private IJobPlanManager jobPlanManager;
    private IJobPlanSubscribeManager jobPlanSubscribeManager;
    private JobExecutionPlanDao jobDao = new JobExecutionPlanDao();
    private IEventDispatcher dispatcher;
    private int xjobDefaultTimeout;

    public DefaultJobStateManager(Configuration conf, IJobPlanManager jobPlanManager,
                                  IJobPlanSubscribeManager jobPlanSubscribeManager, IEventDispatcher dispatcher){
        this.jobPlanManager = jobPlanManager;
        this.jobPlanSubscribeManager = jobPlanSubscribeManager;
        this.dispatcher = dispatcher;
        this.xjobDefaultTimeout = conf.getInt(EngineConstant.XJOB_TIMEOUT, EngineConstant.DEFAULT_XJOB_TIMEOUT);
    }

    private void handlejobProgressOnce(JobProgress jobProgress) throws DaoException {
        XjobState xjobState = new XjobState();
        xjobState.setId(jobProgress.getXjobStateID());
        xjobState.setFinishedPercent(jobProgress.progress);
        xjobState.setJobPlanNodeState(jobProgress.jobPlanNodeState);
        xjobState.setRunInfo(jobProgress.runInfo);
        xjobState.setRunTime(jobProgress.runTime);
        xjobStateDao.update(xjobState);
    }

    public void handlejobProgress(String executorClusterID, JobProgress jobProgress) throws DaoException {
        if (null == jobProgress) {
            return;
        }
        String jobNodeName = WorkflowContext.getInstance().getJobNodeNameByEventID(jobProgress.eventID);
        WorkflowContext workflowContext = WorkflowContext.getInstance();
        XjobExecutorType xjobExecutorType = XjobExecutorType.valueOf(jobProgress.executorType);
        JobPlanNodeState jobProgressNodeState = JobPlanNodeState.valueOf(jobProgress.jobPlanNodeState);
        jobProgress.setXjobStateID(workflowContext.getXjobStateIDByEventID(jobProgress.eventID));
        if (jobProgressNodeState == JobPlanNodeState.DISPATCH_SUCCESS || xjobExecutorType == XjobExecutorType.Once) {
            workflowContext.removeJobNodeRuningInfo(jobProgress.eventID);
            handlejobProgressOnce(jobProgress);
            return;
        }

        IWorkflowInstance workflowInstance = workflowContext.getWorkflowByEventID(jobProgress.eventID);
        if (null == workflowInstance) {
            killJobHandle(executorClusterID, jobProgress.eventID);
            LOG.info("kill Job, this executor ClusterID:" + executorClusterID + ",eventID:" + jobProgress.eventID);
            return;
        }

        if (!jobProgressNodeState.isResultState()) {  //如果流程没执行完则不执行下面的逻辑
            return;
        }
        // 完成本节点 向下流转
        NodeContext context = new JobPlanNodeContext(workflowInstance, jobProgress);
        workflowInstance.completeJobPlanNode(jobNodeName, context);

        // 错误则预警
        if (jobProgressNodeState.isFailState()) {
            this.handleFailJobProgress(workflowInstance.getWorkflowManager().getJobExecutionPlan());
            try {
                jobPlanSubscribeManager.sendSubscribe(executorClusterID, jobNodeName,
                    DateUtil.parseEnDate(jobProgress.runTime), jobProgress.runInfo, JOB_PROCESS_ERROR);
            } catch (Exception e) {
                LOG.error("send Subscribe error!", e);
            }
        }

        // 超时但是完成的 发送成功报告
        if (workflowInstance.isTimeOut() && workflowInstance.isHasAlarm()) {
            try {
                jobPlanSubscribeManager.sendSubscribe(executorClusterID, jobNodeName,
                    DateUtil.parseEnDate(jobProgress.runTime), jobProgress.runInfo, JOB_PROCESS_TIMEOUT_BUT_SUCCESS);
            } catch (Exception e) {
                LOG.error("send Subscribe error!", e);
            }
        }
        //清除掉缓存的节点运行时信息
        workflowContext.removeJobNodeRuningInfo(jobProgress.eventID);
    }

    private void handleFailJobProgress(JobExecutionPlan jep) throws DaoException {
        // 1.是否停止
        if (!jep.isIgnoreError()) {
            jobPlanManager.pauseJobExecutionPlan(jep);
        }

    }

    public void autoTimeOutHandlejobProgress() throws DaoException {
        Set<String> workflowInstanceRunKeySet = WorkflowContext.getInstance().getWorkflowRunKeys();
        if (null != workflowInstanceRunKeySet) {
            for (String key : workflowInstanceRunKeySet) {
                IWorkflowInstance workflowInstance = WorkflowContext.getInstance().getWorkflow(key);
                if (null == workflowInstance) {
                    return;
                }
                List<XjobState> xjobStateList = xjobStateDao.queryXjobStateByCurrentExecutorKey(key);
                if (null == xjobStateList || xjobStateList.isEmpty()) {
                    return;
                }

                for (XjobState xjobState : xjobStateList) {
                    if (xjobState.getJobPlanNodeStateEnum().isRunningState()) {
                        // 进行进度处理
                        JobPlanNode jobPlanNode = jobDao.queryJobPlanNodeByID(xjobState.getJobPlanNodeID());
                        if (null == jobPlanNode) {
                            continue;
                        }

                        JobProgress jobProgress = new JobProgress();
                        jobProgress.xjobStateID = xjobState.getId();
                        jobProgress.runInfo = "";
                        jobProgress.runTime = xjobState.getRunTime();
                        jobProgress.progress = 0;
                        jobProgress.executorType = XjobExecutorType.Auto.toString();

                        this.timeOutHandle(workflowInstance, jobPlanNode, jobProgress);

                        if (workflowInstance.isTimeOut() && !workflowInstance.isHasAlarm()) {
                            try {
                                jobPlanSubscribeManager.sendSubscribe(jobPlanNode.getExecutorClusterID(),
                                    jobPlanNode.getName(), DateUtil.parseEnDate(jobProgress.runTime),
                                    jobProgress.runInfo, JOB_PROCESS_TIMEOUT);
                                workflowInstance.setHasAlarm(true);
                            } catch (Exception e) {
                                LOG.error("send Subscribe error!", e);
                            }
                        }

                    }
                }
            }
        }

    }

    private void timeOutHandle(IWorkflowInstance workflowInstance, JobPlanNode jobPlanNode, JobProgress jobProgress) {
        Date oldRunTime;
        int delayTime;
        if (StringUtils.isBlank(jobProgress.runTime)) {
            return;
        }
        try {
            oldRunTime = DateUtil.dateFormaterBySeconds(jobProgress.runTime);
        } catch (ParseException e) {
            LOG.error("Fail to format date!", e);
            return;
        }
        long timeOutMin = (new Date().getTime() - oldRunTime.getTime()) / Constant.MIN_IN_RATE;
        /**
         * 业务逻辑:1.优先考虑配置超时; 2.系统默认超时
         */
        delayTime = jobPlanNode.getDelayTime() > 0 ? jobPlanNode.getDelayTime() : xjobDefaultTimeout;
        // 未超时
        if (timeOutMin < delayTime) {
            return;
        }
        workflowInstance.setTimeOut(true);
        int killTime = (EngineConstant.JOB_KILL_TIMES - 1) * delayTime;
        jobProgress.runInfo += " this job time out , it will be killed after " + killTime + " min ";

        if (timeOutMin > EngineConstant.JOB_KILL_TIMES * delayTime) {
            // 完成该结点 移除 如果有进度汇报 则kill
            LOG.info("WorkflowKey is " + workflowInstance.getWorkflowKey() + ", this job time out and complete it");
            jobProgress.jobPlanNodeState = JobPlanNodeState.RESULT_ERROR.name();
            NodeContext context = new JobPlanNodeContext(workflowInstance, jobProgress);
            workflowInstance.completeJobPlanNode(jobPlanNode.getName(), context);
            // 硬超时则停止job
            if (DelayType.hard == jobPlanNode.getDelayTypeEnum()) {
                jobPlanManager.pauseJobExecutionPlan(workflowInstance.getWorkflowManager().getJobExecutionPlan());
            }

        }

    }

    private void killJobHandle(String executorClusterID, String eventID) {
        JobEvent jobEvent = new JobEvent();
        jobEvent.executorClusterID = executorClusterID;
        jobEvent.eventType = Constant.KILL_JOB_EVENT;
        jobEvent.eventID = eventID;
        jobEvent.xjobExecutorType = "";
        jobEvent.mode = "";
        jobEvent.executionContent = "";
        jobEvent.jobClassName = "";
        jobEvent.jobPlanNodeName = "";

        dispatcher.doDispatch(jobEvent);
    }

    public List<XjobState> queryXjobStateByRunTime(String runTime) throws Exception {
        String wheresql = " where runTime >=" + runTime + "";
        return xjobStateDao.queryXjobStateByWhereSql(wheresql);
    }

    public List<XjobState> queryXjobStateByCurrentExecutorKey(String currentExecutorKey) throws Exception {
        String wheresql = " where currentExecutorKey ='" + currentExecutorKey + "'";
        return xjobStateDao.queryXjobStateByWhereSql(wheresql);
    }

    public XjobState queryXjobState(int xjobStateID) throws Exception {
        return xjobStateDao.queryXjobState(xjobStateID);

    }

    @Override
    public XjobState addJobState(XjobState xjobState) throws Exception {
        return xjobStateDao.save(xjobState);

    }

    @Override
    public void updJobState(XjobState xjobState) throws Exception {
        xjobStateDao.update(xjobState);
    }
}
