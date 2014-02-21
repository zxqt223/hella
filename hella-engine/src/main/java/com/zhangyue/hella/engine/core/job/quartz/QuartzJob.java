package com.zhangyue.hella.engine.core.job.quartz;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.zhangyue.hella.engine.core.job.QjobContext;
import com.zhangyue.hella.engine.core.job.SystemJob;

public class QuartzJob extends SystemJob implements org.quartz.Job {

    private static Logger LOG = Logger.getLogger(QuartzJob.class);
    private static final int RETRY_COUNT = 3;

    public QuartzJob(){
    }
    
    public void execute(JobExecutionContext context)
        throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        QjobContext qjobContext =
                new QjobContext(data.getString("clusterID"),
                    data.getInt("jobExecutionPlanID"), data.getString("jepID"),
                    data.getString("jepID"), data.getInt("jobPlanVersion"));

        LOG.info("It's time to execute Qjob，event:"
                 + context.getJobDetail().getKey());
        int i = 0;
        while (i++ < RETRY_COUNT) {
            try {
                publishEvent(qjobContext);
                return ;
            } catch (Exception e) {
                LOG.error("Fail to execute Qjob,and try it again after sleep 1 second.The retry count:"+i, e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }
                continue;
            }
        }
    }
}
