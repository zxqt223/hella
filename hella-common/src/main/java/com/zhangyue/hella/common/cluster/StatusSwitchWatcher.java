/*
 * Copyright 2014 ireader.com All right reserved. This software is the
 * confidential and proprietary information of ireader.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with ireader.com.
 */
package com.zhangyue.hella.common.cluster;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.zhangyue.hella.common.cluster.AbsClusterManager;
import com.zhangyue.hella.common.util.Constant;

/**
 * 状态切换观察者，主要是监听主节点是否挂掉
 * 
 * @date 2014-1-22
 * @author scott
 */
public class StatusSwitchWatcher implements Watcher {

    private static Logger LOG = Logger.getLogger(StatusSwitchWatcher.class);
    private AbsClusterManager clusterManger;
    private int reconnectInterval;
    private String znodePath;

    /*
     * (non-Javadoc)
     * @see
     * org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
     */
    public StatusSwitchWatcher(AbsClusterManager clusterManger, String znodePath){
        this.clusterManger = clusterManger;
        this.znodePath = znodePath;
        this.reconnectInterval = Constant.ZOOKEEPER_RECONNECT_INTERVAL * 1000;  //将秒转化为毫秒
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
     */
    @Override
    public void process(WatchedEvent event) {
        LOG.info("Master node watcher accept zk Event.State:" + event.getState() + ",Type:" + event.getType());
        if (event.getState() == KeeperState.Expired) {  //是否处理session过期
            processExpired();
        } else if (event.getType() == EventType.NodeDeleted) {  //是否处理主节点消失
            processNodeDelete();
        }
    }

    /**
     * 处理事件状态过期
     */
    private void processExpired() {
        while (true) {
            try {
                clusterManger.reconnect();
                clusterManger.addWatcher(znodePath, this);
                LOG.info("Success to reinitialize slave node to zookeeper when it receives KeeperState.Expired event state.");
                return;
            } catch (Exception e) {
                LOG.error(
                    "Fail to reinitialize slave node to zookeeper when it receives KeeperState.Expired event state. Try it again for "
                            + reconnectInterval + "s.", e);
                try {
                    Thread.sleep(reconnectInterval);
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    /**
     * 处理节点删除事件
     */
    private void processNodeDelete() {
        try {
            clusterManger.upgradeToMaster();
        } catch (Exception e) {
            LOG.error("Fail to upgrate slave to master!It will be exit!");
            System.exit(-1);
        }
    }
}
