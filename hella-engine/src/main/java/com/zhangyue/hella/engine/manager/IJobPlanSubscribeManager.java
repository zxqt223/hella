package com.zhangyue.hella.engine.manager;

import com.zhangyue.hella.common.exception.InitializationException;
import com.zhangyue.hella.engine.db.entity.JobPlanSubscribe;

/**
 * @Descriptions The class JobPlanSubscribeManager.java's
 *               implementation：作业异常信息通知订阅：邮件/短信等
 * @author scott
 * @date 2013-8-19 下午2:48:45
 * @version 1.0
 */
public interface IJobPlanSubscribeManager {

    public void initialize() throws InitializationException;

    public void sendSubscribe(String clusterID, String jobPlanNodeName, String runTime, String runInfo, int type)
        throws Exception;

    public void add(JobPlanSubscribe jobPlanSubscribe) throws Exception;

    public boolean deleteByID(int[] ids);

}
