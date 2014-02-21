package com.zhangyue.hella.engine.core.job.event;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.engine.core.job.AbstractQjobBuilder;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;

/**
 * @Descriptions The class EventQjobBuilder.java's implementation：时间作业构造器
 * 
 * @author scott
 * @date 2013-8-19 下午2:04:13
 * @version 1.0
 */
public class EventQjobBuilder extends AbstractQjobBuilder {

    @Override
    protected Trigger newQjobTrigger(JobExecutionPlan jobExecutionPlan)
        throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public CronType getJobType() {
        return CronType.event;
    }

}
