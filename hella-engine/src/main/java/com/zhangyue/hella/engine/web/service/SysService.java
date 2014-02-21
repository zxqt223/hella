package com.zhangyue.hella.engine.web.service;

import java.util.List;

import com.zhangyue.hella.common.util.SystemLogType;
import com.zhangyue.hella.engine.cluster.IEngineClusterManager;
import com.zhangyue.hella.engine.dao.Page;
import com.zhangyue.hella.engine.entity.SystemInfoView;
import com.zhangyue.hella.engine.manager.IJobPlanSubscribeManager;

/**
 * @Descriptions The class SysService.java's
 *               implementation：管理员系统服务：系统日志/配置/参数/集群状态/jvm信息等查询
 * @author scott
 * @date 2013-8-19 下午3:08:13
 * @version 1.0
 */
public interface SysService {

    public static final String MASTER_NODE_NAME="Master Node";
    public static final String SLAVE_NODE_NAME="Slave Node";
    public static final String MASTER_NODE_TYPE="Active";
    public static final String SLAVE_NODE_TYPE="Standby";
    public static final String MASTER_NODE_STATE="Master";
    public static final String SLAVE_NODE_STATE="Slave";
    
    public void initialize(IJobPlanSubscribeManager jobPlanSubscribeManager, IEngineClusterManager clusterManager, String zookeeperAddress);
    
    public Page querySystemLog(Page page, String ip, String operatorName,
        SystemLogType systemLogType, String dateBegin, String dateEnd)
        throws Exception;

    public Page queryJobPlanSubscribe(Page page, String clusterID)
        throws Exception;

    public SystemInfoView getSysInfo();

    public void addJobPlanSubscribe(String clusterID, String userEmail,
        String userPhoneNumber) throws Exception;

    public List<String> getClusterIDList() throws Exception;

    public boolean deleteJobPlanSubscribeByID(int[] ids);

    public void changeJobPlanSubscribeState(int[] ids, boolean state)
        throws Exception;

}
