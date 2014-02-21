package com.zhangyue.hella.executor.manager;

import java.util.Date;

import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.executor.client.impl.XjobRunner;

/**
 * @Descriptions The class ProgessManager.java's implementation：进度管理器
 * @author scott
 * @date 2013-8-19 下午3:23:45
 * @version 1.0
 */
public interface IProgressManager {
    /**
     * 创建并执行该XJOB,然后添加到正在运行集合
     * 
     * @param runXjob
     */
    public void add(XjobRunner xjobRunner);

    /**
     * 从正在运行集合中获取指定事件ID的RunXjob进度信息
     * 
     * @param eventID
     * @return
     */
    public int getJobProgress(String eventID);

    public JobPlanNodeState getJobPlanNodeState(String eventID);

    /**
     * 从正在运行集合中根据 runKey 找到RunXjob 获取执行异常/错误信息
     * 
     * @param eventID
     * @return
     */
    public String getRunInfo(String eventID);

    public Date getJobRunDate(String eventID);

    /**
     * 从正在运行集合中移除指定事件ID的Xjob Runner
     * 
     * @param eventID 事件唯一标识
     */
    public void removeXjobRunner(String eventID);

    /**
     * 杀死指定事件ID的作业
     * @param eventID 事件唯一标识
     * @return
     */
    public boolean killJob(String eventID);
    
    /**
     * 是否存在指定runkey的作业正在执行
     * @param eventID
     * @return
     */
    public boolean isContainsRuningXjob(String runKey);
    
    /**
     * 关闭或清理所有打开的资源，例如，XjobRunner线程
     */
    public void close();

}
