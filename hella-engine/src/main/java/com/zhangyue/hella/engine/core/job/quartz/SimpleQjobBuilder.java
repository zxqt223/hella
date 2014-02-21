package com.zhangyue.hella.engine.core.job.quartz;

import java.util.Date;

import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.engine.core.job.AbstractQjobBuilder;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;

/**
 * 
 * @Descriptions The class SimpleQjobBuilder.java's implementation：TODO described the implementation of class
 *
 * @author scott 
 * @date 2013-8-19 下午2:11:59
 * @version 1.0
 */
public class SimpleQjobBuilder extends AbstractQjobBuilder {
	protected Trigger newQjobTrigger(JobExecutionPlan jobExecutionPlan) throws Exception {
		
		Integer cron = Integer.valueOf(jobExecutionPlan.getCronExpression());
		
		return TriggerBuilder.newTrigger().withIdentity(jobExecutionPlan.getQJobName(), jobExecutionPlan.getQJobGroup())
				.startAt(new Date())
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(cron).repeatForever())
				.build();

	}

	@Override
	public CronType getJobType() {
		return CronType.simple;
	}

}