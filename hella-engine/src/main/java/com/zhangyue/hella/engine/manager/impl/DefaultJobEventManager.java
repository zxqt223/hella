package com.zhangyue.hella.engine.manager.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.engine.core.job.JobPlanBuilder;
import com.zhangyue.hella.engine.dao.JobExecutionPlanDao;
import com.zhangyue.hella.engine.dao.NoConsumptionJobEventDao;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.NoConsumptionJobEvent;
import com.zhangyue.hella.engine.manager.IJobEventManager;

public class DefaultJobEventManager implements IJobEventManager {

    private JobPlanBuilder jobPlanBuilder;
    private JobExecutionPlanDao jobExecutionPlanDao = new JobExecutionPlanDao();
    private NoConsumptionJobEventDao noConsumptionJobEventDao =
            new NoConsumptionJobEventDao();

    public DefaultJobEventManager(JobPlanBuilder jobPlanBuilder){
        this.jobPlanBuilder = jobPlanBuilder;
    }

    @Override
    public void produceJobEvent(String jobEvent) throws Exception {
        jobPlanBuilder.produceJobEvent(jobEvent);
    }

    @Override
    public List<JobExecutionPlan> queryJobPlanByEvent(String event)
        throws Exception {
        try {
            return jobExecutionPlanDao.queryJobPlanByCronType(
                JobExecutionPlan.State.able, CronType.event, event);
        } catch (DaoException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<NoConsumptionJobEvent> findAll() throws Exception {
        try {
            return noConsumptionJobEventDao.findAll();
        } catch (DaoException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void saveJobEvent(String[] jobEvent) throws Exception {
        if (null == jobEvent) {
            return;
        }
        for (String eventName : jobEvent) {
            if (StringUtils.isNotBlank(eventName)) {
                NoConsumptionJobEvent noConsumptionJobEvent =
                        new NoConsumptionJobEvent(eventName);
                noConsumptionJobEventDao.save(noConsumptionJobEvent);
            }
        }
    }

    @Override
    public void delAllNoConsumptionJobEvent() throws Exception {
        try {
            noConsumptionJobEventDao.delAllNoConsumptionJobEvent();
        } catch (DaoException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

}
