package com.zhangyue.hella.engine.core.job.event;

import java.util.List;

import org.apache.log4j.Logger;

import com.zhangyue.hella.engine.core.job.QjobContext;
import com.zhangyue.hella.engine.core.job.SystemJob;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.manager.IJobEventManager;

public class EventJob extends SystemJob {

    private static Logger log = Logger.getLogger(EventJob.class);
    private IJobEventManager jobEventManager;

    public EventJob(IJobEventManager jobEventManager){
        this.jobEventManager = jobEventManager;
    }

    public void execute(Event event) throws Exception {
        /**
         *  1.查询订阅此event的 作业计划 条件：clusterID event state
         *  2.迭代发生消息通知
         */
        log.info("execute Event Job，this event is " + event.toString());

        List<JobExecutionPlan> jobExecutionPlanList = jobEventManager.queryJobPlanByEvent(event.getName());

        if (null == jobExecutionPlanList || jobExecutionPlanList.size() == 0) {
            return;
        }
        for (JobExecutionPlan jobExecutionPlan : jobExecutionPlanList) {
            QjobContext qjobContext =
                    new QjobContext(jobExecutionPlan.getClusterID(), jobExecutionPlan.getId(),
                        jobExecutionPlan.getJepID(), jobExecutionPlan.getJepName(),
                        jobExecutionPlan.getExecutePlanVersion());
            log.info("execute Event Job，this qjobContext is " + qjobContext.toString());
            super.publishEvent(qjobContext);
        }

    }

}
