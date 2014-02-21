package com.zhangyue.hella.engine.core.job;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.common.util.Daemon;
import com.zhangyue.hella.common.util.PropertiesUtil;
import com.zhangyue.hella.engine.core.job.event.Event;
import com.zhangyue.hella.engine.core.job.event.EventQjobBuilder;
import com.zhangyue.hella.engine.core.job.event.JobEventConsumer;
import com.zhangyue.hella.engine.core.job.event.JobEventProducer;
import com.zhangyue.hella.engine.core.job.event.JobEventStorehouse;
import com.zhangyue.hella.engine.core.job.quartz.CronQjobBuilder;
import com.zhangyue.hella.engine.core.job.quartz.QJobListener;
import com.zhangyue.hella.engine.core.job.quartz.QTriggerListener;
import com.zhangyue.hella.engine.core.job.quartz.SimpleQjobBuilder;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.manager.IJobEventManager;
import com.zhangyue.hella.engine.util.EngineConstant;

/**
 * @Descriptions The class DefaultJobPlanBuilder.java's implementation：job计划创建者
 * @author scott 2013-8-19 下午1:28:49
 * @version 1.0
 */
public class DefaultJobPlanBuilder implements JobPlanBuilder {

    private static Logger LOG = Logger.getLogger(DefaultJobPlanBuilder.class);

    private SchedulerFactory sf = null;
    private Scheduler quartzScheduler = null;
    private Daemon eventScheduler = null;
    private Daemon jobEventProducerDaemon = null;

    private JobEventConsumer jobEventConsumer;
    private JobEventProducer jobEventProducer;

    private Map<CronType, QjobBuilder> builders = new HashMap<CronType, QjobBuilder>();

    public void initialize(IJobEventManager jobEventManager, Configuration conf) throws SchedulerException {
        QjobBuilder simpleQjobBuilder = new SimpleQjobBuilder();
        QjobBuilder cronQjobBuilder = new CronQjobBuilder();
        QjobBuilder eventQjobBuilder = new EventQjobBuilder();

        builders.put(simpleQjobBuilder.getJobType(), simpleQjobBuilder);
        builders.put(cronQjobBuilder.getJobType(), cronQjobBuilder);
        builders.put(eventQjobBuilder.getJobType(), eventQjobBuilder);

        sf = new StdSchedulerFactory(getProperties(conf));
        quartzScheduler = sf.getScheduler();

        quartzScheduler.getListenerManager().addJobListener(new QJobListener());
        quartzScheduler.getListenerManager().addTriggerListener(new QTriggerListener());

        JobEventStorehouse jobEventResource = new JobEventStorehouse();
        jobEventProducer = new JobEventProducer(jobEventManager, jobEventResource);
        jobEventConsumer = new JobEventConsumer(jobEventManager, jobEventResource);
    }

    private Properties getProperties(Configuration conf) throws SchedulerException {
        Properties prop;
        try {
            prop = PropertiesUtil.loadPropertyFile(EngineConstant.QUARTZ_PROPERTIES);
        } catch (IOException e) {
            throw new SchedulerException("Fail to load quartz property file.", e);
        }
        if (prop == null) {
            throw new SchedulerException("Can't find quartz property file.");
        }
        // 加入数据库配置
        prop.setProperty("org.quartz.dataSource.qzDS.driver", conf.get("jdbc.driver"));
        prop.setProperty("org.quartz.dataSource.qzDS.URL", conf.get("jdbc.url"));
        prop.setProperty("org.quartz.dataSource.qzDS.user", conf.get("jdbc.username"));
        prop.setProperty("org.quartz.dataSource.qzDS.password", conf.get("jdbc.password"));
        return prop;
    }

    /**
     * 启动 quartz 环境
     * 
     * @throws SchedulerException
     */
    public void startScheduler() throws Exception {
        quartzScheduler.start();
        LOG.info("success to start Quartz Scheduler ...... " + quartzScheduler.isStarted());

        jobEventConsumer.setShouldRun(true);
        jobEventProducer.setShouldRun(true);
        eventScheduler = new Daemon(jobEventConsumer);
        jobEventProducerDaemon = new Daemon(jobEventProducer);
        eventScheduler.start();
        jobEventProducerDaemon.start();

        LOG.info("success to start Event Scheduler ...... " + quartzScheduler.isStarted());

    }

    @Override
    public void shutDown(boolean waitForJobsToComplete) throws Exception {
        if (null != quartzScheduler) {
            quartzScheduler.shutdown(waitForJobsToComplete);
            LOG.info("success to shutdown Quartz Scheduler ......");
        }

        jobEventConsumer.setShouldRun(false);
        eventScheduler.interrupt();

        jobEventProducer.setShouldRun(false);
        jobEventProducerDaemon.interrupt();
    }

    public boolean buildJobPlan(JobExecutionPlan jobPlan) {
        QjobBuilder qjobBuilder = null;
        if (null == jobPlan || null == jobPlan.getCronType()) {
            return false;
        }
        qjobBuilder = builders.get(jobPlan.getCronTypeEnum());
        if (null == qjobBuilder) {
            LOG.warn("Fail to get qjob builder.");
            return false;
        }
        try {
            qjobBuilder.buildJob(quartzScheduler, jobPlan);
        } catch (Exception e) {
            LOG.warn("Fail to build JobPlan，clusterID:" + jobPlan.getClusterID() + " jepID:" + jobPlan.getJepID(), e);
            return false;
        }
        return true;
    }

    public boolean deleteJobPlan(JobExecutionPlan[] jps) {
        Scheduler sched;
        JobKey jobKey;
        if (null == jps || jps.length == 0) {
            LOG.warn("There is no available job execution plan.");
            return false;
        }
        for (JobExecutionPlan jp : jps) {
            try {
                jobKey = new JobKey(jp.getQJobName(), jp.getQJobGroup());
                TriggerKey triggerKey = new TriggerKey(jp.getQJobName(), jp.getQJobGroup());
                sched = sf.getScheduler();
                sched.pauseTrigger(triggerKey);// 停止触发器
                sched.unscheduleJob(triggerKey);// 移除触发器
                sched.deleteJob(jobKey);
            } catch (Exception e) {
                LOG.error("Fail to delete JobPlan，job execution plans:" + jp.toString(), e);
                return false;
            }
        }

        return true;
    }

    public boolean pauseJob(JobExecutionPlan[] jps) {
        if (null == jps || jps.length == 0) {
            LOG.warn("jps is null");
            return false;
        }
        for (JobExecutionPlan jobPlan : jps) {
            try {
                JobKey jobKey = new JobKey(jobPlan.getQJobName(), jobPlan.getQJobGroup());
                TriggerKey triggerKey = new TriggerKey(jobPlan.getQJobName(), jobPlan.getQJobGroup());

                quartzScheduler.pauseJob(jobKey);
                quartzScheduler.pauseTrigger(triggerKey);
            } catch (Exception e) {
                LOG.error(
                    "Fail to pauseJob JobPlan，clusterID:" + jobPlan.getClusterID() + " jepID:" + jobPlan.getJepID(), e);
                return false;
            }
        }
        return true;
    }

    public boolean resumeJob(JobExecutionPlan[] jps) {
        if (null == jps || jps.length == 0) {
            LOG.warn("jps is null");
            return false;
        }
        for (JobExecutionPlan jobPlan : jps) {
            try {
                JobKey jobKey = new JobKey(jobPlan.getQJobName(), jobPlan.getQJobGroup());
                TriggerKey triggerKey = new TriggerKey(jobPlan.getQJobName(), jobPlan.getQJobGroup());
                quartzScheduler.resumeJob(jobKey);
                quartzScheduler.resumeTrigger(triggerKey);
            } catch (Exception e) {
                LOG.error(
                    "Fail to resumeJob JobPlan，clusterID:" + jobPlan.getClusterID() + " jepID:" + jobPlan.getJepID(), e);
                return false;
            }
        }
        return true;
    }

    public void produceJobEvent(String jobEvent) {
        jobEventProducer.produceJobEvent(new Event(jobEvent));
    }

    /*
     * (non-Javadoc)
     * @see
     * com.zhangyue.hella.engine.core.job.JobPlanBuilder#updateJob(com.zhangyue
     * .hella.engine.db.entity.JobExecutionPlan[])
     */
    @Override
    public boolean updateJob(String qJobName, String qJobGroup, CronType cronType, String cronExpression) {
        Scheduler sched;
        TriggerKey triggerKey;
        try {
            triggerKey = new TriggerKey(qJobName, qJobName);
            sched = sf.getScheduler();
            if (cronType == CronType.cron) {
                CronTriggerImpl ct = (CronTriggerImpl) sched.getTrigger(triggerKey);
                ct.setCronExpression(cronExpression);
                sched.rescheduleJob(triggerKey, ct);
            } else if (cronType == CronType.simple) {
                SimpleTriggerImpl st = (SimpleTriggerImpl) sched.getTrigger(triggerKey);
                st.setRepeatInterval(Long.parseLong(cronExpression) * Constant.MS_IN_RATE);  //将秒转化成毫秒
                sched.rescheduleJob(triggerKey, st);
            } else {
                LOG.warn("Can't update job timer.It does not support this cronType:" + cronType);
                return false;
            }
        } catch (Exception e) {
            LOG.error("Fail to update JobPlan，qJobName:" + qJobName + ",qJobGroup:" + qJobGroup + ",cronType:"
                      + cronType + ",cronExpression:" + cronExpression, e);
            return false;
        }

        return true;
    }

}
