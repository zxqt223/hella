package com.zhangyue.hella.engine.manager;

import java.util.List;

import com.zhangyue.hella.common.protocol.JobProgress;
import com.zhangyue.hella.engine.db.entity.XjobState;

/**
 * @Descriptions The class JobStateManager.java's implementation：Xjob状态管理
 * @author scott
 * @date 2013-8-19 下午2:49:15
 * @version 1.0
 */
public interface IJobStateManager {

    /** 作业处理状态 */
    public final static int JOB_PROCESS_ERROR = 0; // 报错
    public final static int JOB_PROCESS_TIMEOUT = 1; // 超时
    public final static int JOB_PROCESS_TIMEOUT_BUT_SUCCESS = 2; // 超时已成功

    
    public void handlejobProgress(String executorClusterID, JobProgress jobProgress) throws Exception;

    public XjobState addJobState(XjobState xjobState) throws Exception;

    public void updJobState(XjobState xjobState) throws Exception;

    public XjobState queryXjobState(int xjobStateID) throws Exception;

    public List<XjobState> queryXjobStateByRunTime(String runTime) throws Exception;

    public List<XjobState> queryXjobStateByCurrentExecutorKey(String currentExecutorKey) throws Exception;

    public void autoTimeOutHandlejobProgress() throws Exception;

}
