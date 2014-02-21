package com.zhangyue.hella.executor.manager.impl;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.executor.client.impl.XjobRunner;
import com.zhangyue.hella.executor.manager.IProgressManager;
import com.zhangyue.hella.executor.util.ExecutorConstant;

public class DefaultProgressManager implements IProgressManager {

    private static Logger LOG = LoggerFactory.getLogger(DefaultProgressManager.class);

    private ConcurrentHashMap<String, XjobRunner> xjobRunners; // key:事件IDrunKey，value:xjob运行器
    private ExecutorService pool = null; // 执行xjob运行器的线程池

    public DefaultProgressManager(Configuration conf){
        this.xjobRunners = new ConcurrentHashMap<String, XjobRunner>();
        this.pool =
                Executors.newFixedThreadPool(conf.getInt(ExecutorConstant.EXECUTOR_NODE_EHREAD_POOL_CAPACITY,
                    ExecutorConstant.DEFAULT_EXECUTOR_NODE_EHREAD_POOL_CAPACITY));

    }

    @Override
    public void add(XjobRunner xjobRunner) {
        if (null == xjobRunner) {
            return;
        }
        xjobRunners.put(xjobRunner.getEventID(), xjobRunner);
        /** 将xjob添加到线程池进行调度执行 */
        pool.execute(xjobRunner);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Xjob Runner thread has started，xjobName:" + xjobRunner.getEvent().jobPlanNodeName);
        }
    }

    @Override
    public int getJobProgress(String eventID) {
        XjobRunner xjobRunner = xjobRunners.get(eventID);
        return xjobRunner == null ? Constant.PROGRESS_INIT : xjobRunner.getXjob().getProgress();
    }

    @Override
    public String getRunInfo(String eventID) {
        XjobRunner xjobRunner = xjobRunners.get(eventID);
        return null == xjobRunner ? null : xjobRunner.getXjob().getRunInfo();
    }

    @Override
    public Date getJobRunDate(String eventID) {
        XjobRunner xjobRunner = xjobRunners.get(eventID);
        return null == xjobRunner ? null : xjobRunner.getXjob().getJobRunDate();
    }

    @Override
    public void removeXjobRunner(String eventID) {
        xjobRunners.remove(eventID);
    }

    @Override
    public JobPlanNodeState getJobPlanNodeState(String eventID) {
        XjobRunner xjobRunner = xjobRunners.get(eventID);
        return xjobRunner == null ? null : xjobRunner.getXjob().getJobPlanNodeState();
    }

    @Override
    public boolean killJob(String eventID) {
        LOG.info(" kill the job, eventID:" + eventID);
        synchronized (xjobRunners) {
            XjobRunner xjobRunner = xjobRunners.get(eventID);
            if (null == xjobRunner) {
                return false;
            }
            xjobRunner.getXjob().destroy();
            return true;
        }
    }

    public boolean isContainsRuningXjob(String eventID) {
        return xjobRunners.containsKey(eventID);
    }

    /*
     * (non-Javadoc)
     * @see com.zhangyue.hella.client.ProgessManager#close()
     */
    @Override
    public void close() {
        synchronized (xjobRunners) {
            if (xjobRunners.isEmpty()) {
                return;
            }
            /** 销毁所有正在执行的线程*/
            Collection<XjobRunner> coll = xjobRunners.values();
            for (XjobRunner o : coll) {
                o.getXjob().destroy();
            }
            /** 关闭线程池 */
            pool.shutdown();
        }
    }
}
