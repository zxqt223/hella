/*
 * Copyright 2014 ireader.com All right reserved. This software is the
 * confidential and proprietary information of ireader.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with ireader.com.
 */
package com.zhangyue.hella.executor;

import java.io.IOException;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.executor.client.IJobClient;
import com.zhangyue.hella.executor.client.impl.DefaultJobClient;
import com.zhangyue.hella.executor.cluster.IExecutorClusterManager;
import com.zhangyue.hella.executor.cluster.impl.DefaultExecutorClusterManager;
import com.zhangyue.hella.executor.manager.IProgressCollector;
import com.zhangyue.hella.executor.manager.IProgressManager;
import com.zhangyue.hella.executor.manager.impl.DefaultProgressManager;
import com.zhangyue.hella.executor.manager.impl.DefaultProgressCollector;
import com.zhangyue.hella.executor.util.ExecutorConstant;

/**
 * 执行器节点
 * 
 * @date 2014-1-21
 * @author scott
 */
public class ExecutorNode {

    private static Logger LOG = LoggerFactory.getLogger(ExecutorNode.class);
    private IExecutorClusterManager clusterManager = null;
    private IProgressManager progessManager = null;
    private IJobClient jobClient = null;

    private ExecutorNode(){
    }

    public static String getExecutorNodeHost(Configuration conf) throws IOException {
        String host = conf.get(Constant.SERVER_HOST);
        return null != host ? host:InetAddress.getLocalHost().getHostAddress();
    }
    
    private void initialize() throws IOException {
        Configuration conf = new Configuration();
        conf.initialize(new String[] { ExecutorConstant.EXECUTOR_PROPERTIES });
        String host = getExecutorNodeHost(conf);

        progessManager = new DefaultProgressManager(conf);
        IProgressCollector progressCollector = new DefaultProgressCollector(progessManager);

        clusterManager = new DefaultExecutorClusterManager(conf,jobClient,host);
        clusterManager.initialize();

        jobClient = new DefaultJobClient(conf, clusterManager, progessManager, progressCollector);
        
        jobClient.initialize();
    }

    private void start() throws IOException {
        /** 添加钩子程序，关闭进程时，处理善后工作 */
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                LOG.info("SHUT_DOWN_MSG : Shutting down executor node process!");
                close();
            }
        });
        if(clusterManager.isMasterNode()){  //判断是否为主节点
            jobClient.doRegister();
            LOG.info("Success to start executor master node.");
            return ;
        }
        LOG.info("Success to start executor slave node!");
    }

    private void close() {
        if(null != jobClient){
            jobClient.close();
        }
        if (null != progessManager) {
            progessManager.close();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        ExecutorNode executorNode = new ExecutorNode();
        try {
            executorNode.initialize();
        } catch (IOException e) {
            LOG.error("Fail to initialize executor node!", e);
            System.exit(-1);
        }
        LOG.info("Success to initialize executor node!");
        /** 启动执行器节点 */
        try {
            executorNode.start();
        } catch (IOException e) {
            LOG.info("Fail to start executor node!",e);
        }
        /** 阻塞当前线程，防止退出*/
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
        }

    }

}
