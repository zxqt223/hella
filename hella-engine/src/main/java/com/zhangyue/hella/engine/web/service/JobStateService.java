package com.zhangyue.hella.engine.web.service;

import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.engine.dao.Page;

/**
 * @Descriptions The class JobStateService.java's implementation：作业状态服务接口
 * @author scott
 * @date 2013-8-19 下午3:07:49
 * @version 1.0
 */
public interface JobStateService {

    public void initialize();
    
    public Page queryJobStates(Page page, String executorClusterID,
        String jepID, String jepName, String jobPlanNodeName,
        JobPlanNodeState[] jobPlanNodeStateList, String jobRunDateBegin,
        String jobRunDateEnd, Integer finishedPercent, String currentExecutorKey)
        throws Exception;

}
