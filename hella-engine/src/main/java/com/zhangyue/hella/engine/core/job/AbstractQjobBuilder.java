package com.zhangyue.hella.engine.core.job;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import com.zhangyue.hella.engine.core.job.quartz.QuartzJob;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;

/**
 * 
 * @Descriptions The class AbstractQjobBuilder.java's implementation：QJob构建器
 * @author scott 2013-8-19 下午1:28:10
 * @version 1.0
 */
public abstract class AbstractQjobBuilder implements QjobBuilder {

	protected abstract Trigger newQjobTrigger(JobExecutionPlan jobExecutionPlan) throws Exception;

	public void buildJob(Scheduler sched, JobExecutionPlan jobExecutionPlan) throws Exception {
		JobDetail job = JobBuilder.newJob(QuartzJob.class)
				.withIdentity(jobExecutionPlan.getQJobName(), jobExecutionPlan.getQJobGroup()).build();
		this.setJobDataMap(job, jobExecutionPlan);
		sched.scheduleJob(job, this.newQjobTrigger(jobExecutionPlan));
	}

	private void setJobDataMap(JobDetail job, JobExecutionPlan jobExecutionPlan) {
		job.getJobDataMap().put("jobExecutionPlanID", jobExecutionPlan.getId());
		job.getJobDataMap().put("clusterID", jobExecutionPlan.getClusterID());
		job.getJobDataMap().put("jepID", jobExecutionPlan.getJepID());
		job.getJobDataMap().put("jepName", jobExecutionPlan.getJepName());
		job.getJobDataMap().put("jobPlanVersion", jobExecutionPlan.getExecutePlanVersion());
	}

}
