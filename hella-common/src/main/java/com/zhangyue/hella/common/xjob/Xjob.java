package com.zhangyue.hella.common.xjob;

import java.util.Date;

import com.zhangyue.hella.common.protocol.JobEvent;
import com.zhangyue.hella.common.util.JobPlanNodeState;

/**
 * 
 * @Descriptions The class Xjob.java's implementation：客户端JOB
 * @author scott 2013-8-19 上午11:07:42
 * @version 1.0
 */
public interface Xjob {

	/**
	 * 根据JOB事件执行JOB计划
	 * @param jobEvent
	 */
	public void execute(JobEvent jobEvent);
	
	/**
	 * 销毁该作业：1.移除进度收集 2.移除运行作业
	 */
	public void destroy();

	/**
	 * 获取JOB当前进度
	 * @return
	 */
	public int getProgress();
	
	/**
	 * 获取JOB当前日志
	 * @return
	 */
	public String getRunInfo();
	
	/**
	 * 获取JOB当前时间
	 * @return
	 */
	public Date getJobRunDate();
	
	/**
	 * 获取JOB当前状态
	 * @return
	 */
	public JobPlanNodeState getJobPlanNodeState();

}
