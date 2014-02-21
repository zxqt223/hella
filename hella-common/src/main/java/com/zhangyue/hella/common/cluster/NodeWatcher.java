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
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.zhangyue.hella.common.cluster.AbsClusterManager;
import com.zhangyue.hella.common.util.Constant;

/**
 * Descriptions of the class MasterNodeWatcher.java's implementation：TODO
 * described the implementation of class
 * 
 * @date 2014-1-22
 * @author scott
 */
public class NodeWatcher implements Watcher {

    private static Logger LOG = Logger.getLogger(NodeWatcher.class);
    private AbsClusterManager clusterManger;
    private int reconnectInterval;
    private String znodePath;

    /*
     * (non-Javadoc)
     * @see
     * org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
     */
    public NodeWatcher(AbsClusterManager clusterManger, String znodePath){
        this.clusterManger = clusterManger;
        this.znodePath = znodePath;
        this.reconnectInterval = Constant.ZOOKEEPER_RECONNECT_INTERVAL * 1000;
    }

    @Override
    public void process(WatchedEvent event) {
        LOG.info("Node watcher accept zk Event.State:" + event.getState() + ",Type:" + event.getType());
        if (event.getState() == KeeperState.Expired) {
            while (true) {
                try {
                    clusterManger.reconnect();
                    clusterManger.createZNode(znodePath);
                    clusterManger.addWatcher(znodePath, this);
                    LOG.info("Success to reinitialize node to zookeeper when it receives KeeperState.Expired event state.");
                    break;
                } catch (Exception e) {
                    LOG.error(
                        "Fail to reinitialize node to zookeeper when it receives KeeperState.Expired event state. Try it again for "
                                + reconnectInterval + "s.", e);
                    try {
                        Thread.sleep(reconnectInterval);
                    } catch (InterruptedException e1) {
                    }
                }
            }
        }
    }

}
