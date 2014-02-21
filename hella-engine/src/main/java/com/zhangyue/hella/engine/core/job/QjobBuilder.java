package com.zhangyue.hella.engine.core.job;

import org.quartz.Scheduler;

import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;

/**
 * 
 * @Descriptions The class QjobBuilder.java's implementation：Job创建者：根据 job计划 的job定义 JobDefine 进行创建
 * @author scott 2013-8-19 下午1:29:39
 * @version 1.0
 */
public interface QjobBuilder {

	public void buildJob(Scheduler sched,JobExecutionPlan jp) throws Exception;

	public CronType getJobType();
	
}
