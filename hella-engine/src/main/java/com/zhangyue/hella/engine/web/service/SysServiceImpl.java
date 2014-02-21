package com.zhangyue.hella.engine.web.service;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import java.text.DecimalFormat;

import com.zhangyue.hella.common.entity.NodeAddress;
import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.common.util.SystemLogType;
import com.zhangyue.hella.engine.cluster.IEngineClusterManager;
import com.zhangyue.hella.engine.dao.JobExecutionPlanDao;
import com.zhangyue.hella.engine.dao.JobPlanSubscribeDao;
import com.zhangyue.hella.engine.dao.Page;
import com.zhangyue.hella.engine.dao.SystemLogDao;
import com.zhangyue.hella.engine.db.entity.JobPlanSubscribe;
import com.zhangyue.hella.engine.entity.ClusterInfo;
import com.zhangyue.hella.engine.entity.NodeInfo;
import com.zhangyue.hella.engine.entity.SystemInfoView;
import com.zhangyue.hella.engine.manager.IJobPlanSubscribeManager;
import com.zhangyue.monitor.metrics.impl.JVMMetrics;

public class SysServiceImpl implements SysService {

    private SystemLogDao systemLogDao;
    private JobExecutionPlanDao jobExecutionPlanDao;
    private JobPlanSubscribeDao jobPlanSubscribeDao;
    private IJobPlanSubscribeManager jobPlanSubscribeManager;
    private IEngineClusterManager clusterManager;
    private String zookeeperAddress;

    protected SysServiceImpl(){
    }

    public void initialize(IJobPlanSubscribeManager jobPlanSubscribeManager, IEngineClusterManager clusterManager, String zookeeperAddress) {
        this.jobPlanSubscribeManager = jobPlanSubscribeManager;
        this.clusterManager = clusterManager;
        this.zookeeperAddress = zookeeperAddress;
        systemLogDao = new SystemLogDao();
        jobExecutionPlanDao = new JobExecutionPlanDao();
        jobPlanSubscribeDao = new JobPlanSubscribeDao();
    }

    @Override
    public void addJobPlanSubscribe(String clusterID, String userEmail, String userPhoneNumber) throws Exception {
        if (StringUtils.isBlank(clusterID)) {
            return;
        }
        JobPlanSubscribe jobPlanSubscribe = new JobPlanSubscribe();
        jobPlanSubscribe.setClusterID(clusterID);
        jobPlanSubscribe.setUserEmail(userEmail);
        jobPlanSubscribe.setUserPhoneNumber(userPhoneNumber);
        jobPlanSubscribeManager.add(jobPlanSubscribe);
    }

    @Override
    public Page querySystemLog(Page page, String ip, String operatorName, SystemLogType systemLogType,
        String dateBegin, String dateEnd) throws Exception {
        StringBuffer systemLogSql = new StringBuffer();
        if (StringUtils.isNotBlank(ip)) {
            systemLogSql.append(" AND ip  LIKE '%" + ip + "%'");
        }
        if (StringUtils.isNotBlank(operatorName)) {
            systemLogSql.append(" AND operatorName  LIKE '%" + operatorName + "%'");
        }
        if (null != systemLogType) {
            systemLogSql.append(" AND logType ='" + systemLogType.name() + "'");
        }

        if (StringUtils.isNotBlank(dateBegin)) {
            systemLogSql.append(" AND createDate >='" + dateBegin + "'");
        }

        if (StringUtils.isNotBlank(dateEnd)) {
            systemLogSql.append(" AND createDate <='" + dateEnd + "'");
        }
        systemLogSql.append(" ORDER BY id DESC");
        return systemLogDao.findPage(page, systemLogSql.toString());
    }

    @Override
    public Page queryJobPlanSubscribe(Page page, String clusterID) throws Exception {
        StringBuffer sql = new StringBuffer(" WHERE 1=1 ");
        if (StringUtils.isNotBlank(clusterID)) {
            sql.append(" AND clusterID  LIKE '%" + clusterID + "%'");
        }
        return jobPlanSubscribeDao.findPage(page, sql.toString());
    }

    @Override
    public SystemInfoView getSysInfo() {
        SystemInfoView sysInfoDTO = new SystemInfoView();
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setCurrentAppAddress(clusterManager.getCurrentEngineNodeAddress());
        try {
            if (null != clusterManager.getEngineMasterAddress()) {
                NodeAddress ipAndPort = clusterManager.getEngineMasterAddress();

                NodeInfo nodenfo = new NodeInfo();
                nodenfo.setAddress(ipAndPort.toString());
                nodenfo.setName(MASTER_NODE_NAME);
                nodenfo.setState(MASTER_NODE_STATE);
                nodenfo.setType(MASTER_NODE_TYPE);
                clusterInfo.addEngineList(nodenfo);
                if (ipAndPort.toString().equals(clusterInfo.getCurrentAppAddress())) {
                    sysInfoDTO.setState(nodenfo.getState());
                }
            }
            if (null != clusterManager.getEngineSlaveAddress()) {
                NodeAddress ipAndPort = clusterManager.getEngineSlaveAddress();
                NodeInfo nodenfo = new NodeInfo();
                nodenfo.setAddress(ipAndPort.toString());
                nodenfo.setName(SLAVE_NODE_NAME);
                nodenfo.setState(SLAVE_NODE_STATE);
                nodenfo.setType(SLAVE_NODE_TYPE);
                clusterInfo.addEngineList(nodenfo);
                if (ipAndPort.toString().equals(clusterInfo.getCurrentAppAddress())) {
                    sysInfoDTO.setState(nodenfo.getState());
                }
            }
            List<NodeInfo> executorList = clusterManager.getExecutorClusterInfoList();
            if (executorList != null) {
                clusterInfo.setExecutorMap(executorList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sysInfoDTO.setClusterInfo(clusterInfo);
        sysInfoDTO.setStartDate(clusterManager.getStartDate());
        sysInfoDTO.setZkAddress(zookeeperAddress);
        sysInfoDTO.setMemoryUsed(formatSize(JVMMetrics.getHeapMemoryUsage()) + "/"
                                 + formatSize(JVMMetrics.getTotalMemory()));
        // 3.获取任务统计信息
        return sysInfoDTO;
    }

    @Override
    public boolean deleteJobPlanSubscribeByID(int[] ids) {
        return jobPlanSubscribeManager.deleteByID(ids);
    }

    @Override
    public void changeJobPlanSubscribeState(int[] ids, boolean state) throws Exception {
        try {
            jobPlanSubscribeDao.updateState(ids, state);
        } catch (DaoException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String formatSize(long size) {
        DecimalFormat df = new DecimalFormat("#.0");
        double data = size / 1024d;
        if (data < 1024) {
            return df.format(data) + "KB";
        } else if (data < 1048576) {
            data = data / 1024d;
            return df.format(data) + "MB";
        } else {
            data = data / 1048576d;
            return df.format(data) + "GB";
        }
    }

    @Override
    public List<String> getClusterIDList() throws Exception {
        return jobExecutionPlanDao.getClusterIDList();
    }
}
