package com.zhangyue.hella.engine.core.job.event;

import java.util.List;

import org.apache.log4j.Logger;

import com.zhangyue.hella.engine.db.entity.NoConsumptionJobEvent;
import com.zhangyue.hella.engine.manager.IJobEventManager;

/**
 * @Descriptions The class JobEventProducer.java's implementation：作业事件生产者
 * @author scott
 * @date 2013-8-19 下午2:06:45
 * @version 1.0
 */
public class JobEventProducer implements Runnable {

    private static Logger log = Logger.getLogger(JobEventProducer.class);
    volatile boolean shouldRun = true;

    private JobEventStorehouse storeHouse;
    private IJobEventManager jobEventManager;
    public JobEventProducer(IJobEventManager jobEventManager, JobEventStorehouse storeHouse){
        this.storeHouse = storeHouse;
        this.jobEventManager = jobEventManager;
    }

    public void produceJobEvent(Event jobEvent) {
        storeHouse.push(jobEvent);
    }

    public void loadJobEvent() {
        // 从db加载未消费的事件
        List<NoConsumptionJobEvent> list;
        try {
            list = jobEventManager.findAll();
            if (null == list || list.size() == 0) {
                return;
            }
            for (NoConsumptionJobEvent noConsumptionJobEvent : list) {
                this.produceJobEvent(noConsumptionJobEvent.getEvent());
            }
            log.info(" JobEventProducer load No_Consumption_JobEvent success ,this size is "
                     + list.size());

            jobEventManager.delAllNoConsumptionJobEvent();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        while (shouldRun) {
            try {
                loadJobEvent();
            } catch (Exception e) {
                log.error("JobEventProducer load No_Consumption_JobEvent :"
                          + e.getMessage());
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
            }
        }
    }

    public void setShouldRun(boolean shouldRun) {
        this.shouldRun = shouldRun;
    }
}
