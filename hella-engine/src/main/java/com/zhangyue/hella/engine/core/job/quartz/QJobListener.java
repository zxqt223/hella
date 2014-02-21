package com.zhangyue.hella.engine.core.job.quartz;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import com.zhangyue.hella.common.util.SystemLogType;
import com.zhangyue.hella.engine.metrics.AlarmMessageManager;

/**
 * Quartz作业监听器
 * @author scott
 * @date 2013-8-19 下午2:10:30
 * @version 1.0
 */
public class QJobListener implements JobListener {

    private static Logger LOG = Logger.getLogger(QJobListener.class);
    private final String name = "Hella_QJobListener";
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        StringBuffer key = new StringBuffer();
        key.append(data.getString("clusterID")).append(data.getInt("jobExecutionPlanID")).append(
            data.getString("jepID")).append(data.getString("jepID")).append(data.getInt("jobPlanVersion"));

        LOG.info("job To Be Executed key:" + key);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        StringBuffer key = new StringBuffer();
        key.append(data.getString("clusterID")).append(data.getInt("jobExecutionPlanID")).append(
            data.getString("jepID")).append(data.getString("jepID")).append(data.getInt("jobPlanVersion"));
        LOG.error("job Execution Vetoed key:" + key);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        StringBuffer key = new StringBuffer();
        key.append(data.getString("clusterID")).append(data.getInt("jobExecutionPlanID")).append(
            data.getString("jepID")).append(data.getString("jepID")).append(data.getInt("jobPlanVersion"));
        if (null != jobException) {
            // 执行出现错误 //预警
            String msg="job Was Executed key:" + key;
            LOG.error(msg, jobException);
            AlarmMessageManager.getAlarmMessageManager().addAdminAlarmMessage(SystemLogType.sysException.name(), msg);
            return ;
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("job Was Executed key:" + key);
        }
    }

}
