package com.zhangyue.hella.common.zk;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

/**
 * zookeeper连接管理类
 * 
 * @date 2013-8-15
 * @author scott
 * @version 1.0
 */
public class ZookeeperConnection {

    private int ZOOKEEPER_CONNECTION_RETRY_INTERVAL = 2000;
    private ZooKeeper conn;
    private Watcher watcher = null;
    private String zkAddress = null;
    private int sessionTimeOut;

    private static Logger LOG = Logger.getLogger(ZookeeperConnection.class);

    public ZookeeperConnection(String zkAddress, int sessionTimeOut){
        this.zkAddress = zkAddress;
        this.sessionTimeOut = sessionTimeOut;
        this.watcher = new ReconnectionWarcher();
    }

    private void connect() throws IOException {
        conn = new ZooKeeper(zkAddress, sessionTimeOut, this.watcher);
        LOG.info("Success to get new zookeeper!");
    }

    public synchronized ZooKeeper getZookeeper() throws IOException {
        if (null == conn) {
            connect();
        }
        return conn;
    }

    public synchronized void reconnect() throws IOException {
        try {
            conn.getChildren("/", null);
            LOG.info("The old connection is ok,It no need to reconnect!");
            return;
        } catch (KeeperException e) {
            LOG.warn("Check zk connection,It will be reconnect.", e);
        } catch (InterruptedException e) {
            LOG.warn("Check zk connection,It will be reconnect.", e);
        }
        close();
        connect();
    }

    public void close() {
        try {
            conn.close();
            conn = null;
        } catch (InterruptedException e) {
            LOG.warn("Fail to close zookeeper connection!", e);
        }
    }

    class ReconnectionWarcher implements Watcher {

        public void process(WatchedEvent event) {
            LOG.info("ReconnectionWarcher accept zk Event:" + event.getState() + " " + event.getType());
            if (event.getState() == KeeperState.Expired) {
                LOG.info("Start to reconnect zookeeper!");
                while (true) {
                    try {
                        reconnect();
                        break;
                    } catch (IOException e) {
                        LOG.error("Fail to reconnect zookeeper!wait " + ZOOKEEPER_CONNECTION_RETRY_INTERVAL
                                  + "ms and try reconnect again", e);
                    }
                    try {
                        Thread.sleep(ZOOKEEPER_CONNECTION_RETRY_INTERVAL);
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }
        }

    }
}
