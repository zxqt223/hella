package com.zhangyue.hella.engine.core.job.event;

import org.apache.log4j.Logger;

import com.zhangyue.hella.engine.manager.IJobEventManager;

/**
 * @Descriptions The class JobEventConsumer.java's implementation：作业事件消费者
 * @author scott
 * @date 2013-8-19 下午2:06:17
 * @version 1.0
 */
public class JobEventConsumer implements Runnable {

    private static Logger log = Logger.getLogger(JobEventConsumer.class);
    volatile boolean shouldRun = true;

    private JobEventStorehouse jobEventResource;
    private IJobEventManager jobEventManager;

    public JobEventConsumer(IJobEventManager jobEventManager,
                            JobEventStorehouse jobEventResource){
        this.jobEventResource = jobEventResource;
        this.jobEventManager = jobEventManager;
    }

    public void run() {
        EventJob eventJob = new EventJob(jobEventManager);
        while (shouldRun) {
            Event event = jobEventResource.pop();
            log.info("JobEventConsumer consume " + event.toString());
            try {
                eventJob.execute(event);
            } catch (Exception e1) {
                log.error("JobEventConsumer consume Exception :" + e1.getMessage());
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
        }

    }

    public void setShouldRun(boolean shouldRun) {
        this.shouldRun = shouldRun;
        if (shouldRun) {
            return;
        }
        try {
            String[] products = jobEventResource.popAll();
            if (null == products) {
                log.info("JobEvent products is empty.");
                return;
            }
            // 从db加载未消费的事件
            jobEventManager.saveJobEvent(products);
            log.info(" JobEventProducer save No_Consumption_JobEvent success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
