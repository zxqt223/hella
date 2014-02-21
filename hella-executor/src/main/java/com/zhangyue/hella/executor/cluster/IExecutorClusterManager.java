package com.zhangyue.hella.executor.cluster;

import java.io.IOException;

import com.zhangyue.hella.common.entity.NodeAddress;

/**
 * @Descriptions The class ClusterManager.java's implementation：执行器端集群管理器
 * @author scott
 * @date 2013-8-19 下午3:28:54
 * @version 1.0
 */
public interface IExecutorClusterManager {

    /**
     * 初始化执行器集群
     * @throws IOException
     */
    public void initialize() throws IOException;
    
    /**
     * 获取执行器启动日期
     * @return
     */
    public String getExecutorStartDate();

    /**
     * 获取引擎主节点地址
     * @return
     * @throws IOException
     */
    public NodeAddress getEngineMasterAddress() throws IOException;

    /**
     * 判断引擎主节点是否存在
     * @return
     * @throws IOException
     */
    public boolean isEngineMasterExist() throws IOException;
    
    /**
     * 判断当前节点是否为主节点
     * 
     * @return
     */
    public boolean isMasterNode();

}
