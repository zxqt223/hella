package com.zhangyue.hella.engine.core.job.quartz;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

import com.zhangyue.hella.common.util.SystemLogType;
import com.zhangyue.hella.engine.metrics.AlarmMessageManager;

public class QTriggerListener implements TriggerListener {

    private static Logger LOG = Logger.getLogger(QTriggerListener.class);
    private final String name = "Hella_QTriggerListener";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        StringBuffer key = new StringBuffer();
        key.append(data.getString("clusterID")).append(data.getInt("jobExecutionPlanID")).append(
            data.getString("jepID")).append(data.getInt("jobPlanVersion"));

        LOG.info("Trigger Fired,key=" + key);
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        // 预警
        AlarmMessageManager.getAlarmMessageManager().addAdminAlarmMessage(SystemLogType.sysException.name(),
            "Trigger Miss fired, key=" + trigger.getKey().getName());
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
        CompletedExecutionInstruction triggerInstructionCode) {
        StringBuffer key = new StringBuffer(trigger.getKey().getName());
        if (LOG.isDebugEnabled()) {
            LOG.debug("trigger Complete,key=" + key);
        }
    }

}
