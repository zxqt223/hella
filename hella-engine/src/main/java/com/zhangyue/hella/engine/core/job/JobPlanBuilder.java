package com.zhangyue.hella.engine.core.job;

import org.quartz.SchedulerException;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.manager.IJobEventManager;

/**
 * 
 * @Descriptions The class JobPlanBuilder.java's implementation：作业计划创建器
 * @author scott 2013-8-19 下午1:29:08
 * @version 1.0
 */
public interface JobPlanBuilder {

	/**
	 * 初始化 1、QjobBuilder 2、Scheduler
	 * @throws Exception
	 */
	public void initialize(IJobEventManager jobEventManager, Configuration conf) throws SchedulerException;
	
	/**
	 * 启动 Scheduler
	 * @throws Exception
	 */
	public void startScheduler() throws Exception;
	
	
	
	/**
	 *  关闭Scheduler
	 * @throws Exception
	 */
	public void shutDown(boolean  waitForJobsToComplete) throws Exception ;
	
	/**
	 * 创建Qjob
	 * @param jobPlan
	 * @return
	 */
	public boolean buildJobPlan(JobExecutionPlan jobPlan);

	/**
	 * 删除Qjob
	 * 1、暂停 2清除
	 * @param jps
	 * @return
	 */
	public boolean deleteJobPlan(JobExecutionPlan[] jps);

	/**
	 * 暂停Qjob
	 * @param jps
	 * @return
	 */
	public boolean pauseJob(JobExecutionPlan[] jps);

	/**
	 * 重启Qjob
	 * @param jps
	 * @return
	 */
	public boolean resumeJob(JobExecutionPlan[] jps);
	
	/**
     * 更新QJob
     * @param jps
     * @return
     */
    public boolean updateJob(String qJobName, String qJobGroup, CronType cronType, String cronExpression);
	
	
	public void produceJobEvent(String jobEvent);

}
