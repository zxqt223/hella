package com.zhangyue.hella.engine.protocol.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.exception.SchedException;
import com.zhangyue.hella.common.protocol.HeartbeatResponse;
import com.zhangyue.hella.common.protocol.JobEvent;
import com.zhangyue.hella.common.protocol.JobProgress;
import com.zhangyue.hella.engine.cluster.IEngineClusterManager;
import com.zhangyue.hella.engine.dispatcher.IEventDispatcher;
import com.zhangyue.hella.engine.manager.IJobStateManager;
import com.zhangyue.hella.engine.protocol.IClosable;
import com.zhangyue.hella.engine.util.EngineConstant;
import com.zhangyue.hella.common.protocol.EngineNodeProtocol;
import com.zhangyue.hella.common.util.Constant;

/**
 * engine node protocol协议实现
 * 
 * @date 2014-1-3
 * @author scott
 */
public class DefaultEngineNodeServer implements EngineNodeProtocol.Iface, IClosable {

    private final int EXECUTOR_DOWN_EXPIRE_TIME_FACTOR = 3;
    private IEngineClusterManager clusterManager = null;
    private IEventDispatcher dispatcher = null;
    private IJobStateManager jobStateManager = null;
    private Map<String, Long> executorsLatestAccessTimeMap; // 执行器节点最近访问时间，key:执行器集群ID，value:访问时间戳
    private int heartbeatMaxTimeout;
    private int executorDownExpireTime;
    private int executorDownCheckInterval;
    private ExecutorChecker executorChecker = null;
    private volatile boolean isRunning = false;

    private static Logger LOG = Logger.getLogger(DefaultEngineNodeServer.class);

    /**
     * 初始化
     * 
     * @throws SchedException
     */
    public DefaultEngineNodeServer(IEngineClusterManager clusterManager, IEventDispatcher dispatcher,
                                   IJobStateManager jobStateManager, Configuration conf){
        this.clusterManager = clusterManager;
        this.dispatcher = dispatcher;
        this.jobStateManager = jobStateManager;
        this.executorsLatestAccessTimeMap = new ConcurrentHashMap<String, Long>();
        this.heartbeatMaxTimeout =
                conf.getInt(EngineConstant.HEARTBEAT_MAX_TIMEOUT, EngineConstant.DEFAULT_HEARTBEAT_MAX_TIMEOUT) * 60 * 1000; // 单位：分钟，转化成秒
        this.executorDownExpireTime = this.heartbeatMaxTimeout * EXECUTOR_DOWN_EXPIRE_TIME_FACTOR;
        this.executorDownCheckInterval =
                conf.getInt(EngineConstant.EXECUTOR_DOWN_CHECK_INTERVAL,
                    EngineConstant.DEFAULT_EXECUTOR_DOWN_CHECK_INTERVAL) * 60 * 1000; // 单位：分钟，转化成秒
        executorChecker = new ExecutorChecker();
        executorChecker.start();

    }

    /**
     * executor向engine节点注册
     * 
     * @param clusterID 集群唯一标示
     * @param registerTimestamp executor注册时间戳(取执行器时间，不能取引擎端时间戳，防止两个机器的时钟不一致)
     * @param executorStartDate 开始日期
     */
    public synchronized void doRegister(String clusterID, long registerTimestamp, String executorStartDate)
        throws org.apache.thrift.TException {
        if (clusterManager.isExecutorAlreadyRegister(clusterID)) {
            throw new TException("This cluster has registed.clusterID:" + clusterID);
        }
        try {
            executorsLatestAccessTimeMap.put(clusterID, System.currentTimeMillis());
            clusterManager.storeExecutorRegisterDate(clusterID, executorStartDate);
            LOG.info("Success to register executor,cluster:" + clusterID);
        } catch (Exception e) {
            LOG.error("Fail to register executor,clusterID:" + clusterID, e);
        }
    }

    @Override
    public boolean isEngineAlive() {
        return true;
    }

    /**
     * 处理executor发送过来的心跳信息
     * 
     * @param progressList 作业计划进度信息列表
     * @param lastSeen 发送消息时间戳
     * @return 返回executor该执行的任务
     */
    @Override
    public synchronized HeartbeatResponse sendHeartbeat(String executorClusterID, List<JobProgress> progressList,
        long lastSeen) throws TException {
        HeartbeatResponse response = new HeartbeatResponse();
        if (!executorsLatestAccessTimeMap.containsKey(executorClusterID)) { // 判断是否没有注册，如果没注册，则设置状态直接返回
            response.setExecutorStatus(Constant.EXECUTOR_UNREGISTER);
            return response;
        }

        if (lastSeen - executorsLatestAccessTimeMap.get(executorClusterID) > heartbeatMaxTimeout) {
            clusterManager.removeExecutorRegisterDate(executorClusterID); // 去掉注册信息
            response.setExecutorStatus(Constant.EXECUTOR_HEARTBEAT_TIMEOUT);
            return response;
        }
        // 更新执行器最后访问时间
        executorsLatestAccessTimeMap.put(executorClusterID, lastSeen);
        processJobProgress(executorClusterID, progressList);
        List<JobEvent> jobEvents = dispatcher.getJobEventsByExecutorClusterID(executorClusterID);
        response.setExecutorStatus(Constant.EXECUTOR_NORMAL);
        response.setJobEvents(jobEvents);

        return response;
    }

    private void processJobProgress(String executorClusterID, List<JobProgress> progressList) {
        if (progressList == null || progressList.isEmpty()) {
            return;
        }
        String msg;
        for (JobProgress jobProgress : progressList) {
            msg = "executorClusterID:" + executorClusterID + ",eventID:" + jobProgress.eventID;
            LOG.info("Success to receive heartbeat!" + msg);
            try {
                jobStateManager.handlejobProgress(executorClusterID, jobProgress);
            } catch (Exception e) { // 如果处理心跳信息失败，则直接跳过。如果抛出异常，executor重发心跳将会导致部分作业重复执行
                LOG.error("Fail to handle heartbeat!" + msg, e);
            }
        }
    }

    public void close() {
        isRunning = false;
        if (null != executorChecker) {
            executorChecker.interrupt(); // 直接终止线程，不需要等待线程结束
        }
    }

    private class ExecutorChecker extends Thread {

        public void run() {
            String clusterID;
            while (isRunning) {
                try {
                    Thread.sleep(executorDownCheckInterval);
                } catch (InterruptedException e) {
                    LOG.warn("Executor checker has done.", e);
                }
                if (executorsLatestAccessTimeMap.isEmpty()) {
                    continue;
                }
                Set<Map.Entry<String, Long>> set = executorsLatestAccessTimeMap.entrySet();
                /** 遍历所有的executor访问时间表，如果超时将删除注册信息 */
                for (Map.Entry<String, Long> me : set) {
                    if (me.getValue() > executorDownExpireTime) {
                        clusterID = me.getKey();
                        executorsLatestAccessTimeMap.remove(clusterID);
                        dispatcher.removeJobEventsByExecutorClusterID(clusterID); // 删除该执行器事件列表，防止事件累积
                        clusterManager.removeExecutorRegisterDate(clusterID);
                        LOG.info("The executor has done.It must remove this executor.executorClusterID:" + clusterID);
                    }
                }
            }
        }
    }
}
