package com.zhangyue.hella.executor.client.impl;

import java.io.IOException;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.entity.NodeAddress;
import com.zhangyue.hella.common.exception.InitializationException;
import com.zhangyue.hella.common.exception.ReflectionException;
import com.zhangyue.hella.common.protocol.EngineNodeProtocol;
import com.zhangyue.hella.common.protocol.HeartbeatResponse;
import com.zhangyue.hella.common.protocol.JobEvent;
import com.zhangyue.hella.common.protocol.JobProgress;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.common.util.ReflectionUtil;
import com.zhangyue.hella.common.xjob.Xjob;
import com.zhangyue.hella.executor.client.IJobClient;
import com.zhangyue.hella.executor.cluster.IExecutorClusterManager;
import com.zhangyue.hella.executor.manager.IProgressCollector;
import com.zhangyue.hella.executor.manager.IProgressManager;
import com.zhangyue.hella.executor.util.ExecutorConstant;

/**
 * 执行引擎客户端类，负责与engine node通讯
 * 
 * @date 2013-8-19 下午3:23:17
 * @author scott
 * @version 1.0
 */
public class DefaultJobClient implements IJobClient {

    private static Logger LOG = LoggerFactory.getLogger(DefaultJobClient.class);
    private volatile boolean isConnected = false;
    private volatile boolean isRunning = true;
    private String clusterID = null;
    private Configuration conf;

    private TFramedTransport transport = null;
    private TSocket tSocket = null;
    private EngineNodeProtocol.Client engineNodeClient = null;
    private IExecutorClusterManager executorClusterManager = null;
    private IProgressCollector progressCollector = null; // 进度收集器
    private IProgressManager progessManager = null; // 进度管理器
    private StatusReporter statusReporter = null; // 状态汇报定时器

    public DefaultJobClient(Configuration conf, IExecutorClusterManager executorClusterManager,
                            IProgressManager progessManager, IProgressCollector progressCollector){
        this.conf = conf;
        this.executorClusterManager = executorClusterManager;
        this.progressCollector = progressCollector;
        this.progessManager = progessManager;
        this.clusterID = conf.get("cluster.id");
    }

    public void initialize() throws IOException {
        int heartbeatInterval =
                conf.getInt(ExecutorConstant.EXECUTOR_NODE_HEARTBEAT_INTERVAL,
                    ExecutorConstant.DEFAULT_EXECUTOR_NODE_HEARTBEAT_INTERVAL) * 1000; // 单位是秒，因此需要转换成毫秒
        /** 验证启动条件 */
        doValidate();

        /** 启动状态汇报定时线程 */
        statusReporter = new StatusReporter(heartbeatInterval);
        statusReporter.setDaemon(true);
        statusReporter.start();

        /** 实例化与引擎节点通讯的代理 */
        buildEngineNodeProxy();
    }

    public void close() {
        isRunning = false;
        if (null != transport) {
            transport.close();
        }
    }

    public void doRegister() throws IOException {
        try {
            engineNodeClient.doRegister(clusterID, System.currentTimeMillis(),
                executorClusterManager.getExecutorStartDate());
        } catch (TException e) {
            throw new IOException("Fail to register this executor to active engine node.", e);
        }
    }

    /**
     * 状态汇报定时任务
     * 
     * @date 2014-1-26
     * @author scott
     */
    private class StatusReporter extends Thread {

        private int heartbeatInterval;

        private StatusReporter(int heartbeatInterval){
            this.heartbeatInterval = heartbeatInterval;
        }

        @Override
        public void run() {
            while (isRunning) {
                try {
                    Thread.sleep(heartbeatInterval);
                } catch (InterruptedException e) {
                }
                if (!isConnected) {
                    buildEngineNodeProxy();
                }
                doReport();
            }
        }
        /**
         * 发送心跳并汇报进度
         */
        private void doReport(){
            // 获取所有的进度信息
            List<JobProgress> jobProgresses = progressCollector.getJobProgressList();
            try {
                HeartbeatResponse heartbeatResponse =
                        engineNodeClient.sendHeartbeat(clusterID, jobProgresses, System.currentTimeMillis());
                progressCollector.clear(jobProgresses);
                if (heartbeatResponse.getExecutorStatus() == Constant.EXECUTOR_NORMAL) {
                    processHeartbeat(heartbeatResponse.getJobEvents());
                } else if (heartbeatResponse.getExecutorStatus() == Constant.EXECUTOR_HEARTBEAT_TIMEOUT
                           || heartbeatResponse.getExecutorStatus() == Constant.EXECUTOR_UNREGISTER) {
                    doRegister();
                } else {
                    LOG.error("It does not support command code: " + heartbeatResponse.getExecutorStatus());
                }
            } catch (Exception e) {
                isConnected = false;
                LOG.error("Fail to send heartbeat to active engine node.The message is :"
                          + buildJobProgressMessage(jobProgresses), e);
                buildEngineNodeProxy();
            }
        }
    }

    private void processHeartbeat(List<JobEvent> jobEvents) {
        if (null == jobEvents || jobEvents.isEmpty()) {
            return;
        }
        for (JobEvent event : jobEvents) {
            if (progessManager.isContainsRuningXjob(event.eventID)) {
                LOG.warn("This is job is running.runing key:" + event.eventID + ",executorClusterID:"
                         + event.executorClusterID + ", jobPlanNodeName:" + event.jobPlanNodeName);
                return;
            }
            if (event.eventType == Constant.EXECUTE_JOB_EVENT) { // 事件类型为执行操作
                executeXJob(event);
            } else if (event.eventType == Constant.KILL_JOB_EVENT) { // 事件类型为杀死作业操作
                killXjob(event);
            } else { // 不支持的事件操作
                LOG.warn("It does not support eventType : " + event.eventType);
            }
        }
    }

    /**
     * 执行xjob
     * 
     * @param event
     */
    private void executeXJob(JobEvent event) {
        Xjob xjob = null;
        XjobRunner xjobRunner = null;
        if (event == null) {
            return;
        }

        /*** 通过反射获取需要执行的xjob */
        try {
            xjob = (Xjob) ReflectionUtil.newInstance(event.getJobClassName());
        } catch (ReflectionException e) {
            LOG.error("Fail to create Xjob,it does not consturct xjob for " + event.getJobClassName(), e);
            return;
        }

        if (null != xjob) {
            xjobRunner = new XjobRunner(xjob, event);

            /** 将xjob添加到进度收集器和进度管理器 */
            progressCollector.add(xjobRunner);
            progessManager.add(xjobRunner);
        }
    }

    /**
     * 杀死已经提交的xjob
     */
    private void killXjob(JobEvent event) {
        progessManager.killJob(event.eventID);
        progressCollector.clear(event.eventID);
    }

    private void buildEngineNodeProxy() {
        NodeAddress engineMasterAddress = null;
        int rpcTimeout = conf.getInt(ExecutorConstant.RPC_TIMEOUT, ExecutorConstant.DEFAULT_RPC_TIMEOUT) * 1000;
        try {
            engineMasterAddress = executorClusterManager.getEngineMasterAddress();
        } catch (IOException e) {
            LOG.error("Fail to get engine master address.", e);
            return;
        }
        if(null == engineMasterAddress){
            LOG.warn("It does not find engine master address from zookeeper.");
            return ;
        }
        /** 启动engine master client proxy */
        if (null != transport) {
            transport.close();
        }
        if (null != tSocket) {
            tSocket.close();
        }
        tSocket = new TSocket(engineMasterAddress.getHost(), engineMasterAddress.getPort(), rpcTimeout);
        try {
            transport = new TFramedTransport(tSocket);
            // 协议要和服务端一致
            engineNodeClient = new EngineNodeProtocol.Client(new TBinaryProtocol(transport));
            transport.open();
            isConnected = true;
        } catch (TTransportException e) {
            LOG.error("Fail to create thrift client transport,and try it again for " + rpcTimeout + " ms", e);
        }
    }

    private String buildJobProgressMessage(List<JobProgress> jobProgresses) {
        if (null == jobProgresses || jobProgresses.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean isFirst = true;
        for (JobProgress jp : jobProgresses) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(",");
            }
            sb.append("executorClusterID:" + clusterID);
            sb.append(",");
            sb.append("jobPlanNodeState:" + jp.jobPlanNodeState);
            sb.append(",");
            sb.append("eventID:" + jp.eventID);
            sb.append(",");
            sb.append("runTime:" + jp.runTime);
            sb.append(",");
            sb.append("runInfo:" + jp.runInfo);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 验证是否初始化条件
     * 
     * @throws IOException
     */
    private void doValidate() throws IOException {
        if (null == clusterID) {
            throw new InitializationException(
                "There is no available cluster id.Please check the configuation for cluster.id parameter.");
        }
        if (!executorClusterManager.isEngineMasterExist()) {
            throw new InitializationException("There is no available hella master node!");
        }
    }
}
