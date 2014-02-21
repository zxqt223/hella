package com.zhangyue.hella.engine.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.common.protocol.JobEvent;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.common.util.JobPlanNodeType;
import com.zhangyue.hella.common.util.JobPlanType;
import com.zhangyue.hella.common.util.SystemLogType;
import com.zhangyue.hella.common.util.UniqueIDGenerator;
import com.zhangyue.hella.common.util.XjobExecutorType;
import com.zhangyue.hella.engine.core.job.DefaultJobPlanBuilder;
import com.zhangyue.hella.engine.core.job.JobPlanBuilder;
import com.zhangyue.hella.engine.core.workflow.LiteWorkflowInstance;
import com.zhangyue.hella.engine.core.workflow.LiteWorkflowManager;
import com.zhangyue.hella.engine.core.workflow.WorkflowContext;
import com.zhangyue.hella.engine.core.workflow.IWorkflowInstance;
import com.zhangyue.hella.engine.dao.DBPools;
import com.zhangyue.hella.engine.dao.JobExecutionPlanDao;
import com.zhangyue.hella.engine.dao.XjobStateDao;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlanSubmissionContext;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;
import com.zhangyue.hella.engine.db.entity.XjobMeta;
import com.zhangyue.hella.engine.db.entity.XjobState;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan.CurrentXjobState;
import com.zhangyue.hella.engine.dispatcher.IEventDispatcher;
import com.zhangyue.hella.engine.entity.JobPlanState;
import com.zhangyue.hella.engine.manager.IJobEventManager;
import com.zhangyue.hella.engine.manager.IJobPlanManager;
import com.zhangyue.hella.engine.metrics.AlarmMessageManager;

public class DefaultJobPlanManager implements IJobPlanManager {

    private static Logger LOG = Logger.getLogger(DefaultJobPlanManager.class);
    private JobExecutionPlanDao jobDao;
    private XjobStateDao xjobStateDao;
    private JobPlanBuilder jobPlanBuilder;
    private IJobEventManager jobEventManager;
    private IEventDispatcher dispatcher;
    private Configuration conf;

    /** 不让其他包的类去直接new对象，必须通过工厂获得该类的实例 */
    DefaultJobPlanManager(){
    }

    public void initialize(IEventDispatcher dispatcher, Configuration conf) {
        this.jobPlanBuilder = new DefaultJobPlanBuilder();
        this.conf = conf;
        this.jobEventManager = new DefaultJobEventManager(jobPlanBuilder);
        this.dispatcher = dispatcher;
        this.jobDao = new JobExecutionPlanDao();
        this.xjobStateDao = new XjobStateDao();
    }

    @Override
    public void startJobPlan(int jobExecutionPlanID, String args) throws Exception {
        JobExecutionPlan jobExecutionPlan = this.loadJobExecutionPlan(jobExecutionPlanID);
        if (null == jobExecutionPlan) {
            LOG.warn("it does not find job plan at database.jobExecutionPlanID:" + jobExecutionPlanID);
            return;
        }
        IWorkflowInstance workflowInstance = new LiteWorkflowInstance();
        workflowInstance.initialize(jobEventManager, new LiteWorkflowManager(jobExecutionPlan), args);
        workflowInstance.start();
        WorkflowContext.getInstance().addWorkflow(workflowInstance);
    }

    public void startJobPlan(int jobExecutionPlanID) throws Exception {
        this.startJobPlan(jobExecutionPlanID, null);
    }

    @Override
    public void startJobPlan(String clusterID, String jepID) throws Exception {
        this.startJobPlan(clusterID, jepID, null);
    }

    @Override
    public void startJobPlan(String clusterID, String jepID, String args) throws Exception {
        List<JobExecutionPlan> list = this.jobDao.queryJobPlan(clusterID, jepID, JobExecutionPlan.State.able);
        if (null != list && !list.isEmpty()) {
            this.startJobPlan(list.get(0).getId(), args);
        }
    }

    @Override
    public boolean executeJobPlanNode(int jobPlanNodeID, XjobExecutorType xjobExecutorType, String args)
        throws Exception {
        JobPlanNode jobPlanNode = jobDao.queryJobPlanNodeByID(jobPlanNodeID);
        XjobState xjobState = new XjobState();
        xjobState.setJobPlanNodeID(jobPlanNode.getId());
        xjobState.setCurrentExecutorKey(String.valueOf(UniqueIDGenerator.generateID()));
        xjobState.setJobPlanNodeName(jobPlanNode.getName());
        xjobState.setRunTime(DateUtil.dateFormaterBySeconds(new Date()));
        xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.INIT);
        xjobState = xjobStateDao.save(xjobState);
        boolean rs = false;
        try {
            executeXJob(xjobState.getCurrentExecutorKey(), jobPlanNode, xjobState.getId(), xjobExecutorType, args);
            rs = true;
        } catch (Exception e) {
            xjobState.setJobPlanNodeStateEnum(JobPlanNodeState.NOTIFY_FAIL);
            LOG.error("Fail to execute xjob.jobPlanNodeName:" + jobPlanNode.getName());
        }
        xjobStateDao.update(xjobState);
        return rs;
    }

    public void executeXJob(String workflowRunKey, JobPlanNode jobPlanNode, int xjobStateID, XjobExecutorType type,
        String args){
        XjobMeta xjobMeta = jobPlanNode.getXjobMeta();
        JobEvent jobEvent = new JobEvent();
        // 必要参数
        jobEvent.eventID = UniqueIDGenerator.generateIDByPrifix();
        jobEvent.eventType = Constant.EXECUTE_JOB_EVENT;
        jobEvent.executorClusterID = jobPlanNode.getExecutorClusterID();
        jobEvent.xjobExecutorType = type.name();
        // JOB参数
        jobEvent.mode = xjobMeta.getMode();
        jobEvent.executeUser = xjobMeta.getExecuteUser();
        jobEvent.executionContent = xjobMeta.getExecutionContent();
        jobEvent.jobClassName = xjobMeta.getJobClassName();
        jobEvent.argsValue = StringUtils.isNotBlank(args) ? args : xjobMeta.getArgs();
        jobEvent.jobPlanNodeName = jobPlanNode.getName();

        /** 将节点运行时信息添加到工作流上下文中，在心跳处理进度信息时需要使用 */
        WorkflowContext.getInstance().addJobNodeRuningInfo(jobEvent.eventID, jobPlanNode.getName(), xjobStateID,
            workflowRunKey);

        /** 发送事件 */
        dispatcher.doDispatch(jobEvent);
    }

    /**
     * 启动 quartz 环境
     * 
     * @throws SchedulerException
     */
    public void startScheduler() throws Exception {
        jobPlanBuilder.initialize(jobEventManager, conf);
        jobPlanBuilder.startScheduler();
        LOG.info("Success to start job plan manager!");
    }

    public void shutDown(boolean waitForJobsToComplete) throws Exception {
        jobPlanBuilder.shutDown(waitForJobsToComplete);
    }

    /**
     * 事务控制
     */
    public boolean registerJobExecutionPlanSubmissionContext(JobExecutionPlanSubmissionContext jepSubmissionContext)
        throws Exception {
        if (StringUtils.isBlank(jepSubmissionContext.getClusterId())
            || null == jepSubmissionContext.getJobExecutionPlans()) {
            return false;
        }
        try {
            //
            List<JobExecutionPlan> buildJobPlanList = saveJobPlan(jepSubmissionContext);
            // 进行Quartz任务创建 失败不需要回滚，记录回滚日志即可
            this.buildJobPlan(buildJobPlanList);

        } catch (Exception e) {
            // 打印 回滚日志
            String error = "Fail to save job plan.clusterId:" + jepSubmissionContext.getClusterId();
            LOG.error(error, e);
            AlarmMessageManager.getAlarmMessageManager().addAdminAlarmMessage(SystemLogType.sysException.name(), error);

            DBPools.roolback();
            return false;
        }
        return true;
    }

    @Override
    public boolean disableJobExecutionPlans(int jobExecutionPlanID) throws Exception {
        JobExecutionPlan jps = null;
        try {
            jps = jobDao.queryJobPlan(jobExecutionPlanID);
            if (null != jps) {
                // 1停止Qjob
                boolean rs = jobPlanBuilder.deleteJobPlan(new JobExecutionPlan[] { jps });
                if (rs) {
                    // 2 更新 jepVersion 为 不可用
                    jobDao.updateJobPlanState(jps.getId(), jps.getClusterID(), JobExecutionPlan.State.disable, true);
                }
            }
        } catch (Exception e) {
            throw new Exception("fail  to  deleteJobPlan  by jobPlanBuilder! jobExecutionPlanID:" + jobExecutionPlanID,
                e);
        }

        return false;
    }

    public boolean disableJobExecutionPlans(String clusterID, String jepID) throws Exception {
        List<JobExecutionPlan> list = null;
        try {
            list = jobDao.queryJobPlan(clusterID, jepID);
            if (null != list && list.size() > 0) {
                for (JobExecutionPlan jps : list) {
                    // 1停止Qjob
                    boolean rs = jobPlanBuilder.deleteJobPlan(new JobExecutionPlan[] { jps });
                    if (rs) {
                        // 2 更新 jepVersion 为 不可用
                        jobDao.updateJobPlanState(jps.getId(), jps.getClusterID(), JobExecutionPlan.State.disable, true);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("fail  to  deleteJobPlan  by jobPlanBuilder! jepID:" + jepID, e);
        }

        return false;
    }

    public JobExecutionPlan queryJobPlan(int jobExecutionPlanID) throws Exception {
        try {
            return jobDao.queryJobPlan(jobExecutionPlanID);
        } catch (DaoException e) {
            throw new Exception(e.getMessage());
        }
    }

    // **************************** 以下为私有方法***********************************
    private List<JobExecutionPlan> saveJobPlan(JobExecutionPlanSubmissionContext jepSubmissionContext) throws Exception {
        if (null == jepSubmissionContext.getJobExecutionPlans()
            || jepSubmissionContext.getJobExecutionPlans().size() == 0) {
            return null;
        }

        List<JobExecutionPlan> buildJobPlanList = new ArrayList<JobExecutionPlan>();
        JobExecutionPlan jobExecutionPlan;
        // 1.进行基础数据保存 失败则全部回滚
        for (JobExecutionPlan jep : jepSubmissionContext.getJobExecutionPlans()) {
            /** 对当前版本已存在的作业计划 进行清除操作 */
            jobExecutionPlan = jobDao.queryJobPlan(jepSubmissionContext.getClusterId(), jep.getJepID(), true);
            if (null != jobExecutionPlan) {
                deleteJobPlan(jobExecutionPlan.getId());
            }

            /** 存储数据到database */
            jobExecutionPlan = saveJobExecutionPlan(jep);

            /** 2.根据集群ID和作业ID查询是否存在旧数据 存在则删除 */
            if (jobExecutionPlan.getJobPlanTypeEnum() == JobPlanType.distributed) {
                boolean isLocal = false;
                for (JobPlanNode jobPlanNode : jobExecutionPlan.getJobPlanNodeList()) {
                    /** start 结点指向的集群ID 是否当前执行计划集群ID */
                    if (jobPlanNode.getTypeEnum() == JobPlanNodeType.start
                        && jobPlanNode.getExecutorClusterID().equals(jepSubmissionContext.getClusterId())) {
                        isLocal = true;
                        break;
                    }
                }
                if (isLocal) {
                    buildJobPlanList.add(jobExecutionPlan);
                }
            } else {
                // 本地任务 或者 分布式任务 启动结点是本地 则创建JOB
                buildJobPlanList.add(jobExecutionPlan);
            }
        }

        return buildJobPlanList;
    }

    private boolean buildJobPlan(List<JobExecutionPlan> buildJobPlanList) {
        // 进行Quartz任务创建 失败不需要回滚，记录回滚日志即可
        for (JobExecutionPlan jp : buildJobPlanList) {
            try {
                // 删除过期定时任务
                jobPlanBuilder.deleteJobPlan(new JobExecutionPlan[] { jp });

                if (!jobPlanBuilder.buildJobPlan(jp)) {
                    AlarmMessageManager.getAlarmMessageManager().addAdminAlarmMessage(
                        SystemLogType.sysException.name(),
                        "Fail to buildJobPlan and need roolback,clusterID:" + jp.getClusterID() + ",jepID:"
                                + jp.getJepID());
                }
            } catch (Exception e) {
                String msg =
                        "Fail to buildJobPlan and need roolback,clusterID:" + jp.getClusterID() + ",jepID:"
                                + jp.getJepID();
                LOG.error(msg, e);
                AlarmMessageManager.getAlarmMessageManager().addAdminAlarmMessage(SystemLogType.sysException.name(),
                    msg);
                return false;
            }
        }
        return true;
    }

    // 2.根据集群ID和作业ID查询是否存在旧数据 存在则删除

    private void deleteJobPlan(int jobExecutionPlanID) throws Exception {
        jobDao.delJobExecutionPlan(jobExecutionPlanID, true);
        jobDao.delJobPlanNode(jobExecutionPlanID, true);
        jobDao.delXjobMeta(jobExecutionPlanID, true);
    }

    private JobExecutionPlan saveJobExecutionPlan(JobExecutionPlan jobExecutionPlan) throws DaoException {
        JobExecutionPlan newJobExecutionPlan = this.jobDao.save(jobExecutionPlan, true);
        jobExecutionPlan.setId(newJobExecutionPlan.getId());

        for (JobPlanNode jobPlanNode : jobExecutionPlan.getJobPlanNodeList()) {
            jobPlanNode.setJobExecutionPlanID(jobExecutionPlan.getId());
            jobPlanNode = this.jobDao.save(jobPlanNode, true);
            if (null != jobPlanNode.getXjobMeta()) {
                jobPlanNode.getXjobMeta().setJobExecutionPlanID(jobExecutionPlan.getId());
                jobPlanNode.getXjobMeta().setJobPlanNodeID(jobPlanNode.getId());
                this.jobDao.save(jobPlanNode.getXjobMeta(), true);
            }
        }
        return jobExecutionPlan;
    }

    @Override
    public boolean pauseJobExecutionPlan(JobExecutionPlan jobExecutionPlan) {
        try {
            /** 1 获取JOB计划 */
            if (null == jobExecutionPlan) {
                LOG.warn("There is no available job execution plan.");
                return false;
            }
            /** 2 暂停Qjob */
            boolean rs = jobPlanBuilder.pauseJob((new JobExecutionPlan[] { jobExecutionPlan }));
            /** 3 更新版本信息 */
            if (rs) {
                this.jobDao.updateJobPlanState(jobExecutionPlan.getId(), null, JobExecutionPlan.State.disable, true);
            }
            return rs;
        } catch (DaoException e) {
            LOG.error("Fail to operate database!", e);
            return false;
        }
    }

    @Override
    public boolean resumeJobExecutionPlan(int id) {
        JobExecutionPlan jobExecutionPlan = null;
        if (id == 0) {
            LOG.warn("It's unavailable job execution plan ID!");
            return false;
        }
        try {
            /** 1获取JOB计划 */
            jobExecutionPlan = this.jobDao.queryJobPlan(id);
            if (null == jobExecutionPlan) {
                LOG.warn("Can't find job execution plan for database.jobExecutionID:" + id);
                return false;
            }
            /** 重启Qjob */
            boolean rs = jobPlanBuilder.resumeJob((new JobExecutionPlan[] { jobExecutionPlan }));
            /** 更新版本信息 */
            if (rs) {
                this.jobDao.updateJobPlanState(id, null, JobExecutionPlan.State.able, true);
            }
            return rs;
        } catch (DaoException e) {
            LOG.error("Fail to operate database!jobExecutionID:" + id);
            return false;
        }
    }

    @Override
    public boolean pauseJobExecutionPlan(String clusterID, String jepID) {
        List<JobExecutionPlan> list = null;
        try {
            list = this.jobDao.queryJobPlan(clusterID, jepID, JobExecutionPlan.State.able);
        } catch (DaoException e) {
            LOG.error("Fail to query job plan from database!", e);
            return false;
        }
        if (null == list || list.size() == 0) {
            LOG.error("There is no job plan from database!");
            return false;
        }
        for (JobExecutionPlan jp : list) {
            this.pauseJobExecutionPlan(jp);
        }
        return false;
    }

    @Override
    public boolean deleteJobExecutionPlan(String clusterID, String jepID) {
        List<JobExecutionPlan> list = null;
        try {
            list = this.jobDao.queryJobPlan(clusterID, jepID);
        } catch (DaoException e) {
            LOG.error("Fail to query job plan from database!", e);
            return false;
        }
        if (null == list || list.size() == 0) {
            LOG.error("There is no job plan from database!");
            return false;
        }
        boolean rs = jobPlanBuilder.deleteJobPlan(list.toArray(new JobExecutionPlan[list.size()]));
        if (!rs) {
            LOG.error("Fail to delete job plan from quartz!");
            return false;
        }
        for (JobExecutionPlan jps : list) {
            try {
                this.deleteJobPlan(jps.getId());
            } catch (Exception e) {
                LOG.error("Fail to delete job plan from database!", e);
                rs = false; // 当删除失败时，循环删除剩下所有作业计划，但最终结果还是失败，需要管理员人工清楚脏数据
            }
        }
        return rs;
    }

    @Override
    public boolean deleteJobExecutionPlan(int jobExecutionPlanID) throws DaoException {
        JobExecutionPlan jobExecutionPlan = this.jobDao.queryJobPlan(jobExecutionPlanID);
        if (null == jobExecutionPlan) {
            LOG.error("Fail to query job plan from database!");
            return false;
        }
        boolean rs = jobPlanBuilder.deleteJobPlan(new JobExecutionPlan[] { jobExecutionPlan });
        if (rs) {
            try {
                this.deleteJobPlan(jobExecutionPlan.getId());
            } catch (Exception e) {
                LOG.error("Fail to delete job plan from database!", e);
                rs = false;
            }
        }
        return rs;
    }

    @Override
    public boolean resumeJobExecutionPlan(String clusterID, String jepID) {
        JobExecutionPlan jobExecutionPlan = null;
        try {
            jobExecutionPlan = this.jobDao.queryJobPlan(clusterID, jepID, true);
        } catch (DaoException e) {
            LOG.error("Fail to query job plan from database!", e);
            return false;
        }
        if (null == jobExecutionPlan) {
            return false;
        }
        try {
            return this.resumeJobExecutionPlan(jobExecutionPlan.getId());
        } catch (Exception e) {
            LOG.error("Fail to resume job execution plan!", e);
            return false;
        }
    }

    public JobExecutionPlan loadJobExecutionPlan(int jobExecutionPlanID) throws Exception {
        return this.jobDao.loadJobExecutionPlan(jobExecutionPlanID);
    }

    @Override
    public void changeJobExecutionPlanCurrentXjobState(int jobExecutionPlanID, CurrentXjobState currentXjobState)
        throws Exception {
        this.jobDao.updateJobPlanCurrentXjobState(jobExecutionPlanID, currentXjobState,
            DateUtil.dateFormaterBySeconds(new Date()), true);
    }

    @Override
    public void changeJobExecutionPlanCronExpression(int jobExecutionPlanID, CronType cronType, String cronExpression)
        throws Exception {
        /** 1.修改 JobExecutionPlan */
        JobExecutionPlan jobExecutionPlan = this.queryJobPlan(jobExecutionPlanID);
        if (null == jobExecutionPlan) {
            LOG.warn("There is no available job execution plan at database.jobExecutionPlanID:" + jobExecutionPlanID
                     + ",cronType:" + cronType + ",cronExpression:" + cronExpression);
            return;
        }
        jobDao.updateJobPlanCronExpression(jobExecutionPlanID, cronType, cronExpression, true);

        /** 2.更新quartz job */
        boolean rs =
                jobPlanBuilder.updateJob(jobExecutionPlan.getQJobName(), jobExecutionPlan.getQJobGroup(), cronType,
                    cronExpression);

        if (!rs) {
            throw new Exception("Fail to update job cron express for quartz!");
        }
    }

    @Override
    public void changeJobExecutionPlanCronExpression(String clusterID, String jepID, CronType cronType,
        String cronExpression) throws Exception {
        JobExecutionPlan jobExecutionPlan = this.jobDao.queryJobPlan(clusterID, jepID, true);
        if (null != jobExecutionPlan) {
            this.changeJobExecutionPlanCronExpression(jobExecutionPlan.getId(), cronType, cronExpression);
        }
    }

    @Override
    public List<JobPlanState> queryJobPlanStates(String clusterID, String[] jepIDs) throws Exception {
        return this.jobDao.queryJobPlanStates(clusterID, jepIDs);
    }

    public String queryJobPlanState(int jepID) throws Exception {
        JobExecutionPlan jep = this.jobDao.queryJobPlan(jepID);
        return null == jep ? null : jep.getState();
    }

    @Override
    public boolean isJobPlansExist(List<String> ids) throws Exception {
        if (null == ids || ids.isEmpty()) {
            return false;
        }
        return this.jobDao.isJobPlansExist(ids);
    }
}
