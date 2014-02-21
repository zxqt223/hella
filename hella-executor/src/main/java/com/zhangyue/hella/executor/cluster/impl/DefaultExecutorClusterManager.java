package com.zhangyue.hella.executor.cluster.impl;

import java.io.IOException;
import java.util.Date;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import com.zhangyue.hella.common.cluster.AbsClusterManager;
import com.zhangyue.hella.common.cluster.NodeWatcher;
import com.zhangyue.hella.common.cluster.StatusSwitchWatcher;
import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.entity.NodeAddress;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.executor.client.IJobClient;
import com.zhangyue.hella.executor.cluster.IExecutorClusterManager;

/**
 * 默认executor node集群管理器
 * 
 * @date 2014-1-23
 * @author scott
 */
public class DefaultExecutorClusterManager extends AbsClusterManager implements IExecutorClusterManager {

    private String executorMasterNodePath = null;
    private String executorSlaveNodePath = null;
    private String startDate;
    private String host;
    private IJobClient jobClient = null;

    public DefaultExecutorClusterManager(Configuration conf, IJobClient jobClient, String host){
        this.conf = conf;
        this.jobClient = jobClient;
        this.host = host;
        this.startDate = DateUtil.dateFormaterByString(new Date());
        this.executorMasterNodePath = Constant.EXECUTOR_ZK_ROOT_PATH + "/" + conf.get("cluster.id") + MASTER_NODE;
        this.executorSlaveNodePath = Constant.EXECUTOR_ZK_ROOT_PATH + "/" + conf.get("cluster.id") + SLAVE_NODE;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.zhangyue.hella.executor.cluster.ExecutorClusterManager#initialize()
     */
    @Override
    public void initialize() throws IOException {
        super.initialize(executorMasterNodePath);
    }

    /*
     * (non-Javadoc)
     * @see com.zhangyue.hella.executor.cluster.ExecutorClusterManager#
     * getExecutorStartDate()
     */
    @Override
    public String getExecutorStartDate() {
        return startDate;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.zhangyue.hella.common.cluster.AbsClusterManager#createZNode(java.
     * lang.String)
     */
    @Override
    public void createZNode(String znodePath) throws IOException {
        createZNode(znodePath, host.getBytes());
    }

    /*
     * (non-Javadoc)
     * @see
     * com.zhangyue.hella.common.cluster.AbsClusterManager#upgradeToMaster()
     */
    @Override
    public void upgradeToMaster() throws Exception {
        jobClient.doRegister();
        initializeMaster();
        conn.getZookeeper().delete(executorSlaveNodePath, ZOOKEEPER_ALL_VERSION);
        isMasterStatus = true;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.zhangyue.hella.executor.cluster.ExecutorClusterManager#isMasterNode()
     */
    @Override
    public boolean isMasterNode() {
        return isMasterStatus;
    }

    /*
     * (non-Javadoc)
     * @see com.zhangyue.hella.executor.cluster.ExecutorClusterManager#
     * getEngineMasterAddress()
     */
    @Override
    public NodeAddress getEngineMasterAddress() throws IOException {
        try {
            if (conn.getZookeeper().exists(Constant.ENGINE_MASTER_ZNODE_PATH, false) == null) {
                return null;
            }
        } catch (KeeperException e) {
            throw new IOException("Fail to connect zookeeper!", e);
        } catch (InterruptedException e) {
            throw new IOException("Fail to connect zookeeper!", e);
        }
        try {
            return new NodeAddress(new String(conn.getZookeeper().getData(Constant.ENGINE_MASTER_ZNODE_PATH,
                null, new Stat())));
        } catch (KeeperException e) {
            throw new IOException("Fail to get engine master address from zookeeper!", e);
        } catch (InterruptedException e) {
            throw new IOException("Fail to get engine master address from zookeeper!", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.zhangyue.hella.executor.cluster.ExecutorClusterManager#
     * isEngineMasterExist()
     */
    @Override
    public boolean isEngineMasterExist() throws IOException {
        try {
            if (conn.getZookeeper().exists(Constant.ENGINE_MASTER_ZNODE_PATH, false) == null) {
                return false;
            }
        } catch (KeeperException e) {
            throw new IOException("Fail to connect zk.", e);
        } catch (InterruptedException e) {
            throw new IOException("Fail to connect zk.", e);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.zhangyue.hella.common.cluster.AbsClusterManager#initializeMaster()
     */
    @Override
    protected void initializeMaster() throws IOException {
        createZNode(executorMasterNodePath);
        addWatcher(executorMasterNodePath, new NodeWatcher(this, executorMasterNodePath));
    }

    /*
     * (non-Javadoc)
     * @see
     * com.zhangyue.hella.common.cluster.AbsClusterManager#initializeSlave()
     */
    @Override
    protected void initializeSlave() throws IOException {
        createZNode(executorSlaveNodePath);
        addWatcher(executorSlaveNodePath, new NodeWatcher(this, executorSlaveNodePath));
        addWatcher(executorSlaveNodePath, new StatusSwitchWatcher(this, executorSlaveNodePath));
    }
}
