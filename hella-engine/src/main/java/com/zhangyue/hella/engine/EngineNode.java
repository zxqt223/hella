/*
 * Copyright 2014 ireader.com All right reserved. This software is the
 * confidential and proprietary information of ireader.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with ireader.com.
 */
package com.zhangyue.hella.engine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.entity.NodeAddress;
import com.zhangyue.hella.common.protocol.EngineNodeProtocol;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.engine.cluster.IEngineClusterManager;
import com.zhangyue.hella.engine.cluster.impl.DefaultEngineClusterManager;
import com.zhangyue.hella.engine.core.task.JobMonitorTask;
import com.zhangyue.hella.engine.core.task.JobTimeOutCheckTask;
import com.zhangyue.hella.engine.core.task.SystemLogTask;
import com.zhangyue.hella.engine.core.workflow.node.handler.NodeHandlerFactory;
import com.zhangyue.hella.engine.dao.DBPools;
import com.zhangyue.hella.engine.dispatcher.IEventDispatcher;
import com.zhangyue.hella.engine.dispatcher.impl.DefaultDispatcher;
import com.zhangyue.hella.engine.manager.IJobPlanManager;
import com.zhangyue.hella.engine.manager.IJobPlanSubscribeManager;
import com.zhangyue.hella.engine.manager.IJobStateManager;
import com.zhangyue.hella.engine.manager.impl.DefaultJobPlanSubscribeManager;
import com.zhangyue.hella.engine.manager.impl.DefaultJobStateManager;
import com.zhangyue.hella.engine.manager.impl.JobPlanManagerFactory;
import com.zhangyue.hella.engine.metrics.EngineMonitor;
import com.zhangyue.hella.engine.parser.IJobExecutionPlanParser;
import com.zhangyue.hella.engine.parser.impl.DefaultExecutionPlanParser;
import com.zhangyue.hella.engine.protocol.IClosable;
import com.zhangyue.hella.engine.protocol.impl.DefaultEngineNodeServer;
import com.zhangyue.hella.engine.util.EngineConstant;
import com.zhangyue.hella.engine.web.WebServer;
import com.zhangyue.hella.engine.web.service.JobPlanService;
import com.zhangyue.hella.engine.web.service.JobStateService;
import com.zhangyue.hella.engine.web.service.ServiceFactory;
import com.zhangyue.hella.engine.web.service.SysService;
import com.zhangyue.hella.engine.web.service.SystemAdministrator;

/**
 * 引擎节点主类，负责初始化和启动各个模块
 * 
 * @date 2014-1-11
 * @author scott
 */
public class EngineNode {

    private final static Logger LOG = Logger.getLogger(EngineNode.class);
    private EngineNodeProtocol.Iface engineNodeServer = null; // 引擎节点提供给执行节点交互的接口的实例
    private TServer tServer = null; // thrift server
    private WebServer webServer = null;
    private Timer systemTaskTimer = null; // 启动系统定时任务的定时器
    private IJobPlanManager jobPlanManager = null;
    private EngineMonitor engineMonitor = null;
    private IEngineClusterManager engineClusterManager = null;

    private EngineNode(){
    }

    public static NodeAddress getEngineNodeHost(Configuration conf) throws IOException {
        int port = conf.getInt(EngineConstant.RPC_PORT, EngineConstant.DEFAULT_RPC_PORT);
        String host = conf.get(Constant.SERVER_HOST);
        if (null == host) {
            host = InetAddress.getLocalHost().getHostAddress();
        }

        return new NodeAddress(host, port);
    }

    private void initialize() throws IOException {
        IJobPlanSubscribeManager jobPlanSubscribeManager;
        IEventDispatcher dispatcher;
        IJobStateManager jobStateManager;
        IJobExecutionPlanParser jobExecutionPlanParser;
        JobPlanService jobPlanService;
        JobStateService jobStateService;
        SysService sysService;
        int httpPort;
        Configuration conf = new Configuration();
        conf.initialize(new String[] { EngineConstant.JDBC_PROPERTIES, EngineConstant.ENGINE_PROPERTIES });
        httpPort = conf.getInt(EngineConstant.HTTP_PORT, EngineConstant.DEFAULT_HTTP_PORT);
        /** 初始化数据库连接池 */
        DBPools.init(conf);
        NodeAddress nodeAddress = getEngineNodeHost(conf);


        /** 初始化集群管理器 */
        engineClusterManager = new DefaultEngineClusterManager(conf,nodeAddress);
        engineClusterManager.initialize();
        
        /** 初始化作业计划管理器 */
        dispatcher = new DefaultDispatcher(engineClusterManager);
        jobPlanManager = JobPlanManagerFactory.getJobPlanManager();
        jobPlanManager.initialize(dispatcher, conf);
        
        jobPlanSubscribeManager = new DefaultJobPlanSubscribeManager();
        jobPlanSubscribeManager.initialize();
        jobExecutionPlanParser = new DefaultExecutionPlanParser();
        jobStateManager = new DefaultJobStateManager(conf, jobPlanManager, jobPlanSubscribeManager, dispatcher);
        engineNodeServer = new DefaultEngineNodeServer(engineClusterManager, dispatcher, jobStateManager, conf);

        try {
            initializeRPC(conf, nodeAddress);
        } catch (TTransportException e) {
            throw new IOException("Fail to initialize rpc!", e);
        }

        /** 初始化web server */
        webServer = new WebServer();
        webServer.initialize(conf, nodeAddress.getHost(), httpPort);
        /** 初始化web服务层中使用的服务类 */
        jobPlanService = ServiceFactory.getServiceInstance(JobPlanService.class);
        jobStateService = ServiceFactory.getServiceInstance(JobStateService.class);
        sysService = ServiceFactory.getServiceInstance(SysService.class);
        jobPlanService.initialize(engineClusterManager, jobPlanManager, jobExecutionPlanParser);
        jobStateService.initialize();
        sysService.initialize(jobPlanSubscribeManager, engineClusterManager, conf.get(Constant.HELLA_ZOOKEEPER_QUORUM));

        /** 管理员初始化 */
        SystemAdministrator.initialize(conf);

        /** 初始化引擎监控模块 */
        engineMonitor = new EngineMonitor();
        engineMonitor.initialize(conf);
        
        /** 初始化节点处理器工厂 */
        NodeHandlerFactory.initialize(jobPlanManager, jobStateManager);
        /** 初始化引擎节点的系统任务模块 */
        initializeSystemTask(conf, engineMonitor, jobStateManager);
    }

    private void initializeSystemTask(Configuration conf, EngineMonitor engineMonitor, IJobStateManager jobStateManager) {
        systemTaskTimer = new Timer();
        boolean isRunJobMonitorTask =
                conf.getBoolean(EngineConstant.JOB_MONITOR_TASK, EngineConstant.DEFAULT_JOB_MONITOR_TASK);
        int jobRunningMaxTimeInterval =
                conf.getInt(EngineConstant.JOB_NO_RUNNING_MAX_TIME_INTERVAL,
                    EngineConstant.DEFAULT_JOB_NO_RUNNING_MAX_TIME_INTERVAL);
        int logTaskInterval =
                conf.getInt(EngineConstant.LOG_TASK_INTERVAL, EngineConstant.DEFAULT_LOG_TASK_INTERVAL) * 1000;
        int jobMonitorTaskInterval;
        /** 单位是分钟，因此需要转化成毫秒 */
        int jobTimeoutCheckTaskInterval =
                conf.getInt(EngineConstant.JOB_TIMEOUT_CHECK_TASK_INTERVAL,
                    EngineConstant.DEFAULT_JOB_TIMEOUT_CHECK_TASK_INTERVAL) * 60 * 1000;

        systemTaskTimer.schedule(new SystemLogTask(engineMonitor), logTaskInterval, logTaskInterval);

        if (isRunJobMonitorTask) { // 判断是否启动作业监控任务，监控任务定时间隔参数单位是分钟，需要转化成毫秒
            jobMonitorTaskInterval =
                    conf.getInt(EngineConstant.JOB_MONITOR_TASK_INTERVAL,
                        EngineConstant.DEFAULT_JOB_MONITOR_TASK_INTERVAL) * 60 * 1000;
            systemTaskTimer.schedule(new JobMonitorTask(engineMonitor, jobStateManager, jobRunningMaxTimeInterval),
                jobMonitorTaskInterval, jobMonitorTaskInterval);
        }

        systemTaskTimer.schedule(new JobTimeOutCheckTask(jobStateManager), jobTimeoutCheckTaskInterval,
            jobTimeoutCheckTaskInterval);
    }

    private void initializeRPC(Configuration conf, NodeAddress nodeAddress) throws TTransportException,
        UnknownHostException {
        TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(nodeAddress.getPort());

        // 异步IO，需要使用TFramedTransport，它将分块缓存读取。
        TTransportFactory transportFactory = new TFramedTransport.Factory();

        // 设置处理器
        TProcessor processor = new EngineNodeProtocol.Processor<EngineNodeProtocol.Iface>(engineNodeServer);
        TNonblockingServer.Args args = new TNonblockingServer.Args(serverTransport);
        args.processor(processor);
        args.transportFactory(transportFactory);
        args.protocolFactory(new TBinaryProtocol.Factory());
        // 创建服务器
        tServer = new TNonblockingServer(args);
    }

    private void start() throws Exception {
        /** 添加钩子程序，关闭进程时，处理善后工作，例如关闭数据流或连接等 */
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                LOG.info("SHUT_DOWN_MSG : Shutting down engine node process!");
                close();
            }
        });
        if(engineClusterManager.isMasterNode()){
            jobPlanManager.startScheduler();
            LOG.info("Success to start active engine node.");
        }else{
            LOG.info("Success to start standby engine node.");
        }
        webServer.start();
        tServer.serve();
    }

    private void close() {
        /** 关闭作业计划管理器 */
        if (null != jobPlanManager) {
            try {
                jobPlanManager.shutDown(true);
            } catch (Exception e) {
                LOG.error("Fail to close job plan manager.", e);
            }
        }

        /** 关闭thrift server */
        if (null != tServer) {
            tServer.stop();
        }

        /** 关闭web server */
        if (null != webServer) {
            try {
                webServer.stop();
            } catch (Exception e) {
                LOG.error("Fail to close web server!", e);
            }
        }

        /** 关闭定时器 */
        if (null != systemTaskTimer) {
            systemTaskTimer.cancel();
        }

        /** 关闭监控器 */
        if (null != engineMonitor) {
            engineMonitor.close();
        }
        if(null != engineClusterManager){
            engineClusterManager.close();
        }
        
        if(null != engineNodeServer){
            ((IClosable)engineNodeServer).close();
        }
    }
    /**
     * 引擎节点启动类主函数，完成初始化工作以及启动各组件
     * @param args
     */
    public static void main(String[] args) {
        EngineNode engineNode = new EngineNode();
        try {
            engineNode.initialize();
        } catch (IOException e) {
            LOG.error("Fail to initialize engine node!", e);
            System.exit(-1);
        }
        try {
            engineNode.start();
        } catch (Exception e) {
            LOG.error("Fail to start engine node!", e);
            System.exit(-1);
        }
    }

}
