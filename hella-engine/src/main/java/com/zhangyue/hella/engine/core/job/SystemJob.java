package com.zhangyue.hella.engine.core.job;

import java.util.Date;

import org.apache.log4j.Logger;

import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.SystemLogType;
import com.zhangyue.hella.engine.core.LogCollector;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.SystemLog;
import com.zhangyue.hella.engine.manager.IJobPlanManager;
import com.zhangyue.hella.engine.manager.impl.JobPlanManagerFactory;

public class SystemJob {

    private static Logger LOG = Logger.getLogger(SystemJob.class);
    protected IJobPlanManager jobPlanManager;
    
    public SystemJob(){
        jobPlanManager = JobPlanManagerFactory.getJobPlanManager();
    }
    public void publishEvent(QjobContext qjobContext) throws Exception {
        if (null == qjobContext) {
            return;
        }
        // 暂停则不通知
        JobExecutionPlan jobExecutionPlan = jobPlanManager.queryJobPlan(qjobContext.getJobExecutionPlanID());
        if (jobExecutionPlan != null && jobExecutionPlan.getStateEnum() == JobExecutionPlan.State.disable) {
            jobPlanManager.disableJobExecutionPlans(qjobContext.getJobExecutionPlanID());
            return;
        }

        try {
            jobPlanManager.startJobPlan(qjobContext.getJobExecutionPlanID());
        } catch (Exception e) {
            String msg =
                    "Fail to start JobPlan,executorID:" + qjobContext.getClusterID() + ",jepID:"
                            + qjobContext.getJepID() + ",jepName:" + qjobContext.getJepName();
            LOG.error(msg, e);
            LogCollector.getSchedCollector().addSystemLog(
                new SystemLog("sys", "master", SystemLogType.sysException.name(), msg,
                    DateUtil.dateFormaterBySeconds(new Date())));
        }
    }
}
