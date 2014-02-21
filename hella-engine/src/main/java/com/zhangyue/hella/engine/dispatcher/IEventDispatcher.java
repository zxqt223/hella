package com.zhangyue.hella.engine.dispatcher;

import java.util.List;

import com.zhangyue.hella.common.protocol.JobEvent;

/**
 * 事件分发器，主要包括两个功能：1.发送事件；2.获取指定执行器集群ID的作业事件
 * 
 * @date 2013-8-19 下午2:54:07
 * @author scott
 * @version 1.0
 */
public interface IEventDispatcher {

    /**
     * 发送作业执行消息
     * 
     * @param jobEvent 作业事件
     */
    public void doDispatch(JobEvent jobEvent);

    /**
     * 获取指定执行器集群ID的所有作业事件列表
     * 
     * @param executorClusterID 执行器集群ID
     * @return 作业事件列表，如果不存在该执行器集群ID，则返回null
     */
    public List<JobEvent> getJobEventsByExecutorClusterID(String executorClusterID);
    
    /**
     * 删除掉指定执行器集群ID的所有作业事件列表，当执行器被检测宕机时调用该接口
     * @param executorClusterID 执行器集群ID
     */
    public void removeJobEventsByExecutorClusterID(String executorClusterID);
}
