package com.zhangyue.hella.engine.core.job.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.engine.core.job.AbstractQjobBuilder;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;

/**
 * 
 * @Descriptions The class CronQjobBuilder.java's implementation：定时作业cron构建器
 *
 * @author scott 
 * @date 2013-8-19 下午2:09:12
 * @version 1.0
 */
public class CronQjobBuilder extends AbstractQjobBuilder {

	@Override
	protected Trigger newQjobTrigger(JobExecutionPlan jobExecutionPlan) throws Exception {
		return TriggerBuilder
				.newTrigger()
				.withIdentity(jobExecutionPlan.getQJobName(), jobExecutionPlan.getQJobGroup())
				.withSchedule(CronScheduleBuilder.cronSchedule(jobExecutionPlan.getCronExpression()))
				.build();

	}

	@Override
	public CronType getJobType() {
		return CronType.cron;
	}
 

}
