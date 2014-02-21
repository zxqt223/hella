package com.zhangyue.hella.engine.manager;

import java.util.List;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.common.util.XjobExecutorType;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlanSubmissionContext;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;
import com.zhangyue.hella.engine.dispatcher.IEventDispatcher;
import com.zhangyue.hella.engine.entity.JobPlanState;

/**
 * 
 * @Descriptions The class JobPlanManager.java's implementation：Job计划管理
 * @author scott 
 * @date 2013-8-19 下午2:48:10
 * @version 1.0
 */
public interface IJobPlanManager {
    public void initialize(IEventDispatcher notifier, Configuration conf);
	/**
	 * 启动一个作业计划
	 * @param jobExecutionPlanID
	 * @throws Exception
	 */
	public void startJobPlan(int jobExecutionPlanID,String args) throws Exception ;
	public void startJobPlan(int jobExecutionPlanID) throws Exception ;
	public void startJobPlan(String clusterID,String jepID) throws Exception ;
	public void startJobPlan(String clusterID,String jepID,String args) throws Exception ;


	/**
	 * 执行一个计划结点
	 * @param jobPlanNodeID
	 * @param xjobExecutorType
	 * @return
	 * @throws Exception
	 */
	public boolean executeJobPlanNode(int jobPlanNodeID,XjobExecutorType xjobExecutorType,String args) throws Exception;
	
	/**
	 * 执行一个结点 xjob 
	 * @param jobPlanNodeID
	 * @param xjobStateID
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public void executeXJob(String workflowRunKey, JobPlanNode jobPlanNode, int xjobStateID, XjobExecutorType type, String args);

	/**
	 * 1、根据clusterId 批量更新 JobExecutionPlan 为 不可用 2、 批量停止上一版本Qjob 3、 批量保存本版本
	 * jobpan 以及 xjob 4、创建并启动 本版本的Qjob
	 * 
	 * @param executorPlan
	 * @return
	 */
	public boolean registerJobExecutionPlanSubmissionContext(JobExecutionPlanSubmissionContext jepSubmissionContext) throws Exception;
	
	/**
	 * 根据集群ID,计划版本号,计划ID,计划Name 1、停止Qjob 2、更新JobExecutionPlanVersion为不可用
	 * 
	 * @param clusterId
	 *            为了安全期间 集群ID为 必要参数。
	 * @param jobPlanVersion
	 * @param jepId
	 * @param jepName
	 * @return
	 */
	public boolean disableJobExecutionPlans(int jobExecutionPlanID) throws Exception;
	public boolean disableJobExecutionPlans(String clusterID,String jepID) throws Exception;

	/**
	 * 启动 quartz 服务
	 * 
	 * @throws Exception
	 */
	public void startScheduler() throws Exception;

	/**
	 * 停止 quartz 服务
	 * 
	 * @throws Exception
	 */

	public void shutDown(boolean waitForJobsToComplete) throws Exception;

	/**
	 * 暂停 JobExecutionPlan 服务 1 暂停Qjob 2 更改plan状态为停止
	 * 
	 * @throws Exception
	 */
	public boolean pauseJobExecutionPlan(JobExecutionPlan jep);
	public boolean pauseJobExecutionPlan(String clusterID, String jepID);
	
	
	public boolean deleteJobExecutionPlan(String clusterID, String jepID) throws DaoException;
    public boolean deleteJobExecutionPlan(int jobExecutionPlanID) throws DaoException;


	/**
	 * 重启 JobExecutionPlan 服务 1 恢复Qjob 2 更改plan状态为运行
	 * 
	 * @throws Exception
	 */
	public boolean resumeJobExecutionPlan(int jobExecutionPlanID);
	public boolean resumeJobExecutionPlan(String clusterID, String jepID);

	/**
	 * 加载作业计划完整信息：计划+结点+job
	 * @param jobExecutionPlanID
	 * @return
	 * @throws Exception
	 */
	public JobExecutionPlan loadJobExecutionPlan(int jobExecutionPlanID) throws Exception;
	
	/**
	 * 根据ID查询作业计划
	 * @param jobExecutionPlanID
	 * @return
	 * @throws Exception
	 */
	public  JobExecutionPlan queryJobPlan(int jobExecutionPlanID) throws Exception;

	/**
	 * 判断指定作业执行计划ID列表的的执行计划是否存在
	 * @param ids
	 * @return
	 */
	public boolean isJobPlansExist(List<String> ids) throws Exception;
	/**
	 * 修改作业计划执行状态
	 * @param jobExecutionPlanID
	 * @param currentXjobState
	 * @throws Exception
	 */
	public void changeJobExecutionPlanCurrentXjobState(int jobExecutionPlanID,JobExecutionPlan.CurrentXjobState currentXjobState) throws Exception;

	/**
	 * 修改作业计划触发时间
	 * @param jobExecutionPlanID
	 * @param cronType
	 * @param cronExpression
	 * @throws Exception
	 */
	public void changeJobExecutionPlanCronExpression(int jobExecutionPlanID, CronType cronType, String cronExpression) throws Exception;

	public void changeJobExecutionPlanCronExpression(String clusterID, String jepID, CronType cronType, String cronExpression) throws Exception;
	
	/**
	 * 获取作业计划最新状态列表
	 * @param clusterID
	 * @param jepIDs
	 * @return
	 * @throws Exception
	 */
    public List<JobPlanState> queryJobPlanStates(String clusterID,String[] jepIDs) throws Exception; 

    /**
     * 获取指定的作业计划状态
     */
    public String queryJobPlanState(int jepID) throws Exception;
}
