/*
 * Copyright 2014 ireader.com All right reserved. This software is the
 * confidential and proprietary information of ireader.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with ireader.com.
 */
package com.zhangyue.hella.common.cluster;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.exception.InitializationException;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.common.zk.ZookeeperConnection;

/**
 * 集群管理抽象类，主要负责实现一些集群中公共的操作
 * 
 * @date 2014-1-22
 * @author scott
 */
public abstract class AbsClusterManager {

    protected static final String MASTER_NODE = "/master";
    protected static final String SLAVE_NODE = "/slave";
    protected final static int ZOOKEEPER_ALL_VERSION = -1;  //ZK中匹配所有版本号znode

    protected Configuration conf;
    protected ZookeeperConnection conn;
    protected boolean isMasterStatus;

    /**
     * 创建zookeeper中节点
     * 
     * @param znodePath
     * @throws IOException
     */
    public abstract void createZNode(String znodePath) throws IOException;

    /**
     * slave节点升级到master节点
     * 
     * @throws Exception
     */
    public abstract void upgradeToMaster() throws Exception;

    /**
     * 初始化主节点
     * 
     * @throws IOException
     */
    protected abstract void initializeMaster() throws IOException;

    /**
     * 初始化slave节点
     * 
     * @throws IOException
     */
    protected abstract void initializeSlave() throws IOException;

    /**
     * 重新连接zookeeper
     * 
     * @throws IOException
     */
    protected void reconnect() throws IOException{
        conn.reconnect();
    }

    protected void createZNode(String znodePath, byte[] data) throws IOException {
        try {
            if (conn.getZookeeper().exists(znodePath, false) != null) {
                conn.getZookeeper().setData(znodePath, data, -1);
            } else {
                conn.getZookeeper().create(znodePath, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }
        } catch (KeeperException e) {
            throw new IOException("Fail to create znode:" + znodePath, e);
        } catch (InterruptedException e) {
            throw new IOException("Fail to create znode:" + znodePath, e);
        }
    }

    protected void addWatcher(String znodePath, Watcher watcher) throws IOException {
        try {
            conn.getZookeeper().exists(znodePath, watcher);
        } catch (KeeperException e) {
            throw new IOException("Fail to set watcher to znode :" + znodePath, e);
        } catch (InterruptedException e) {
            throw new IOException("Fail to set watcher to znode :" + znodePath, e);
        }
    }

    /**
     * 初始化集群
     * 
     * @param masterZnodePath
     * @throws InitializationException
     */
    protected void initialize(String masterZnodePath) throws InitializationException {
        String zookeeperAddress = conf.get(Constant.HELLA_ZOOKEEPER_QUORUM);
        int zookeeperTimeout =
                conf.getInt(Constant.HELLA_ZOOKEEPER_TIMEOUT, Constant.DEFAULT_HELLA_ZOOKEEPER_TIMEOUT) * 1000;
        conn = new ZookeeperConnection(zookeeperAddress, zookeeperTimeout);
        /** 初始化znode节点 */
        try {
            if (conn.getZookeeper().exists(Constant.ZK_ROOT_PATH, false) == null) {
                conn.getZookeeper().create(Constant.ZK_ROOT_PATH, "scheduler root path".getBytes(),
                    Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                conn.getZookeeper().create(Constant.ENGINE_ZK_ROOT_PATH, "scheduler ha path".getBytes(),
                    Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                conn.getZookeeper().create(Constant.EXECUTOR_ZK_ROOT_PATH, "executor ha path".getBytes(),
                    Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            throw new InitializationException("Fail to create znode at zookeeper.", e);
        }

        /** 判断主节点是否已经存在 */
        Stat stat = null;
        try {
            stat = conn.getZookeeper().exists(masterZnodePath, false);
        } catch (Exception e) {
            throw new InitializationException("Fail to judge znode exist or not!", e);
        }

        /** 初始化znode，并添加相关watcher */
        try {
            if (null == stat) {
                initializeMaster();
                isMasterStatus = true;
            } else {
                initializeSlave();
                isMasterStatus = false;
            }
        } catch (IOException e) {
            throw new InitializationException("Fail to initialize znode!", e);
        }
    }
}
