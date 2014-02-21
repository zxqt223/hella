package com.zhangyue.hella.engine.core.task;

import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.zhangyue.hella.common.metircs.AbsMonitor;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.SystemLogType;
import com.zhangyue.hella.engine.db.entity.XjobState;
import com.zhangyue.hella.engine.manager.IJobStateManager;
import com.zhangyue.hella.engine.metrics.AlarmMessageManager;

/**
 * @Descriptions The class SystemJobMonitorTask.java's
 *               implementation：系统监控作业任务：4小时无任何作业执行，则预警
 * @author scott
 * @date 2013-8-19 下午2:12:30
 * @version 1.0
 */
public class JobMonitorTask extends TimerTask {

    private static Logger LOG = Logger.getLogger(JobMonitorTask.class);
    private IJobStateManager jobStateManager;
    private int jobRunningMaxTimeInterval; // 作业运行最大时间间隔，超过辞职就报警，单位：分钟

    public JobMonitorTask(AbsMonitor monitor,IJobStateManager jobStateManager, int jobRunningMaxTimeInterval){
        this.jobRunningMaxTimeInterval = jobRunningMaxTimeInterval;
        this.jobStateManager = jobStateManager;
    }

    @Override
    public void run() {
        AlarmMessageManager alarmMessageManager = AlarmMessageManager.getAlarmMessageManager();
        try {
            LOG.info("system job monitor task is running，check job count ... ");
            List<XjobState> list =
                    jobStateManager.queryXjobStateByRunTime(DateUtil.getDateMinuteAgo(jobRunningMaxTimeInterval));
            if (null != list && !list.isEmpty()) {
                return;
            }
            String msg = "There is no job running at " + jobRunningMaxTimeInterval + " min";
            alarmMessageManager.addAdminAlarmMessage(SystemLogType.sysException.name(), msg);
        } catch (Exception e) {
            LOG.error("Fail to execute job monitor task!", e);
        }
    }

}
