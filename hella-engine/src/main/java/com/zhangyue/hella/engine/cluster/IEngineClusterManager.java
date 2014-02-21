package com.zhangyue.hella.engine.cluster;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;

import com.zhangyue.hella.common.entity.NodeAddress;
import com.zhangyue.hella.common.exception.InitializationException;
import com.zhangyue.hella.engine.entity.NodeInfo;

/**
 * 引擎集群管理器
 * 
 * @date 2013-8-19 下午1:25:55
 * @author
 * @version 1.0
 */
public interface IEngineClusterManager {

    /**
     * 初始化集群管理器
     * 
     * @throws InitializationException
     */
    public void initialize() throws InitializationException;

    /**
     * 判断当前节点是否为主节点
     * 
     * @return
     */
    public boolean isMasterNode();

    /**
     * 获取当前引擎节点地址
     * 
     * @return
     */
    public String getCurrentEngineNodeAddress();

    /**
     * 获取引擎master节点地址
     * 
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    public NodeAddress getEngineMasterAddress() throws KeeperException, InterruptedException, IOException;

    /**
     * 获取引擎slave节点地址
     * 
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    public NodeAddress getEngineSlaveAddress() throws KeeperException, InterruptedException, IOException;

    /**
     * 获取执行器集群信息列表
     * 
     * @return
     * @throws IOException
     */
    public List<NodeInfo> getExecutorClusterInfoList() throws IOException;

    /**
     * 存储执行器注册日期
     * 
     * @param clusterID 执行器集群ID
     * @param date
     */
    public void storeExecutorRegisterDate(String clusterID, String date);

    /**
     * 删除执行器注册日期
     * 
     * @param clusterID 执行器集群ID
     */
    public void removeExecutorRegisterDate(String clusterID);

    /**
     * 判断执行器是否已经注册
     * @param clusterID 执行器集群ID
     * @return 如果注册过则返回true，否则false
     */
    public boolean isExecutorAlreadyRegister(String clusterID);
    /**
     * 关闭集群管理器
     */
    public void close();

    /**
     * 获取集群启动时间
     * 
     * @return
     */
    public String getStartDate();

}
