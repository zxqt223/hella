package com.zhangyue.hella.engine.manager;

import java.util.List;

import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.NoConsumptionJobEvent;

public interface IJobEventManager {

    public void produceJobEvent(String jobEvent) throws Exception;

    public void saveJobEvent(String[] jobEvent) throws Exception;

    public void delAllNoConsumptionJobEvent() throws Exception;

    public List<NoConsumptionJobEvent> findAll() throws Exception;

    public List<JobExecutionPlan> queryJobPlanByEvent(String event) throws Exception;

}
