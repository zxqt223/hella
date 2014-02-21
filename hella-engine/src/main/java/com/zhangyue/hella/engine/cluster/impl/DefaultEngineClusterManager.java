package com.zhangyue.hella.engine.cluster.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import com.zhangyue.hella.common.cluster.AbsClusterManager;
import com.zhangyue.hella.common.cluster.NodeWatcher;
import com.zhangyue.hella.common.cluster.StatusSwitchWatcher;
import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.entity.NodeAddress;
import com.zhangyue.hella.common.exception.InitializationException;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.engine.cluster.IEngineClusterManager;
import com.zhangyue.hella.engine.entity.NodeInfo;
import com.zhangyue.hella.engine.manager.impl.JobPlanManagerFactory;

/**
 * 默认集群管理器实现
 * 
 * @date 2014-1-22
 * @author scott
 */
public class DefaultEngineClusterManager extends AbsClusterManager implements IEngineClusterManager {

    private final static String MASTER_NODE_NAME = "Master Node";
    private final static String MASTER_NODE_TYPE = "Master";
    private final static String MASTER_NODE_STATE = "Active";
    private final static String SLAVE_NODE_NAME = "Slave Node";
    private final static String SLAVE_NODE_TYPE = "Slave";
    private final static String SLAVE_NODE_STATE = "Standby";

    private Map<String, String> executorRegisterDateMap = null; // 执行器注册日期map
    private String startDate;
    private String rpcAddressStr;

    public DefaultEngineClusterManager(Configuration conf, NodeAddress nodeAddress){
        this.conf = conf;
        this.rpcAddressStr = nodeAddress.toString();
        this.executorRegisterDateMap = new HashMap<String, String>();
        this.startDate = DateUtil.dateFormaterByString(new Date());
    }

    public void initialize() throws InitializationException {
        super.initialize(Constant.ENGINE_MASTER_ZNODE_PATH);
    }

    public String getStartDate() {
        return startDate;
    }

    public void createZNode(String znodePath) throws IOException {
        createZNode(znodePath, rpcAddressStr.getBytes());
    }

    public boolean isMasterNode() {
        return isMasterStatus;
    }

    public void close() {
        conn.close();
    }

    public NodeAddress getEngineMasterAddress() throws KeeperException, InterruptedException, IOException {
        if (conn.getZookeeper().exists(Constant.ENGINE_MASTER_ZNODE_PATH, false) == null) {
            return null;
        }
        return new NodeAddress(new String(conn.getZookeeper().getData(Constant.ENGINE_MASTER_ZNODE_PATH, null,
            new Stat())));
    }

    public String getCurrentEngineNodeAddress() {
        return rpcAddressStr;
    }

    @Override
    public NodeAddress getEngineSlaveAddress() throws KeeperException, InterruptedException, IOException {
        if (conn.getZookeeper().exists(Constant.ENGINE_SLAVE_ZNODE_PATH, false) == null) {
            return null;
        }
        return new NodeAddress(new String(conn.getZookeeper().getData(Constant.ENGINE_SLAVE_ZNODE_PATH, null,
            new Stat())));
    }

    @Override
    public List<NodeInfo> getExecutorClusterInfoList() throws IOException {
        List<String> clusterIDList;
        String executorMasterAddress;
        NodeInfo nodeInfo;
        try {
            clusterIDList = conn.getZookeeper().getChildren(Constant.EXECUTOR_ZK_ROOT_PATH, null, new Stat());
        } catch (KeeperException e) {
            throw new IOException("Fail to get executor cluster id from zookeeper!", e);
        } catch (InterruptedException e) {
            throw new IOException("Fail to get executor cluster id from zookeeper!", e);
        }

        if (null == clusterIDList || clusterIDList.isEmpty()) {
            return null;
        }
        List<NodeInfo> executorList = new ArrayList<NodeInfo>();

        for (String clusterID : clusterIDList) {

            /** 获取executor主节点信息 */
            executorMasterAddress = getExecutorMasterAddress(clusterID);
            if (null != executorMasterAddress) {
                nodeInfo = new NodeInfo();
                nodeInfo.setClusterID(clusterID);
                nodeInfo.setAddress(executorMasterAddress);
                nodeInfo.setName(MASTER_NODE_NAME);
                nodeInfo.setState(MASTER_NODE_STATE);
                nodeInfo.setType(MASTER_NODE_TYPE);
                nodeInfo.setCreateTime(executorRegisterDateMap.get(clusterID));

                executorList.add(nodeInfo);
            }

            /** 获取executor slave节点信息 */
            String executorSlaveAddress = getExecutorSlaveAddress(clusterID);
            if (null != executorSlaveAddress) {
                nodeInfo = new NodeInfo();
                nodeInfo.setClusterID(clusterID);
                nodeInfo.setAddress(executorSlaveAddress);
                nodeInfo.setName(SLAVE_NODE_NAME);
                nodeInfo.setState(SLAVE_NODE_STATE);
                nodeInfo.setType(SLAVE_NODE_TYPE);
                executorList.add(nodeInfo);
            }
        }
        return executorList;
    }

    /*
     * (non-Javadoc)
     * @see com.zhangyue.hella.engine.cluster.EngineClusterManager#
     * storeExecutorRegisterDate(java.lang.String, java.lang.String)
     */
    @Override
    public void storeExecutorRegisterDate(String clusterID, String date) {
        executorRegisterDateMap.put(clusterID, date);
    }

    /*
     * (non-Javadoc)
     * @see com.zhangyue.hella.engine.cluster.EngineClusterManager#
     * deleteExecutorRegisterDate(java.lang.String)
     */
    @Override
    public void removeExecutorRegisterDate(String cluterID) {
        executorRegisterDateMap.remove(cluterID);
    }

    /*
     * (non-Javadoc)
     * @see com.zhangyue.hella.engine.cluster.EngineClusterManager#
     * isExecutorAlreadyRegister(java.lang.String)
     */
    @Override
    public boolean isExecutorAlreadyRegister(String clusterID) {
        if(null == clusterID){
            return false;
        }
        return executorRegisterDateMap.containsKey(clusterID);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.zhangyue.hella.common.cluster.AbsClusterManager#initializeMaster()
     */
    @Override
    protected void initializeMaster() throws IOException {
        createZNode(Constant.ENGINE_MASTER_ZNODE_PATH);
        addWatcher(Constant.ENGINE_MASTER_ZNODE_PATH, new NodeWatcher(this, Constant.ENGINE_MASTER_ZNODE_PATH));
    }

    /*
     * (non-Javadoc)
     * @see
     * com.zhangyue.hella.common.cluster.AbsClusterManager#initializeSlave()
     */
    @Override
    protected void initializeSlave() throws IOException {
        createZNode(Constant.ENGINE_SLAVE_ZNODE_PATH);
        addWatcher(Constant.ENGINE_SLAVE_ZNODE_PATH, new NodeWatcher(this, Constant.ENGINE_SLAVE_ZNODE_PATH));
        addWatcher(Constant.ENGINE_MASTER_ZNODE_PATH, new StatusSwitchWatcher(this, Constant.ENGINE_MASTER_ZNODE_PATH));
    }

    /*
     * (non-Javadoc)
     * @see
     * com.zhangyue.hella.common.cluster.AbsClusterManager#upgradeToMaster()
     */
    @Override
    public void upgradeToMaster() throws Exception {
        JobPlanManagerFactory.getJobPlanManager().startScheduler();
        initializeMaster();
        conn.getZookeeper().delete(Constant.ENGINE_SLAVE_ZNODE_PATH, ZOOKEEPER_ALL_VERSION);
        isMasterStatus = true;
    }

    /**
     * 根据集群id，获取主执行器地址
     * 
     * @param clusterID 集群id
     * @return
     * @throws IOException
     */
    private String getExecutorMasterAddress(String clusterID) throws IOException {
        String znodePath = Constant.EXECUTOR_ZK_ROOT_PATH + "/" + clusterID + MASTER_NODE;
        try {
            if (conn.getZookeeper().exists(znodePath, false) == null) {
                return null;
            }
        } catch (KeeperException e) {
            throw new IOException("Fail to connect zookeeper!", e);
        } catch (InterruptedException e) {
            throw new IOException("Fail to connect zookeeper!", e);
        }
        try {
            return new String(conn.getZookeeper().getData(znodePath, null, new Stat()));
        } catch (KeeperException e) {
            throw new IOException("Fail to get data from zookeeper!", e);
        } catch (InterruptedException e) {
            throw new IOException("Fail to get data from zookeeper!", e);
        }
    }

    /**
     * 根据集群id，获取备份执行器地址列表
     * 
     * @param clusterID 集群id
     * @return
     * @throws IOException
     */
    private String getExecutorSlaveAddress(String clusterID) throws IOException {
        String znodePath = Constant.EXECUTOR_ZK_ROOT_PATH + "/" + clusterID + SLAVE_NODE;
        try {
            if (conn.getZookeeper().exists(znodePath, false) == null) {
                return null;
            }
        } catch (KeeperException e) {
            throw new IOException("Fail to connect zookeeper!", e);
        } catch (InterruptedException e) {
            throw new IOException("Fail to connect zookeeper!", e);
        }
        try {
            return new String(conn.getZookeeper().getData(znodePath, null, new Stat()));
        } catch (KeeperException e) {
            throw new IOException("Fail to get data from zookeeper!", e);
        } catch (InterruptedException e) {
            throw new IOException("Fail to get data from zookeeper!", e);
        }
    }
}
