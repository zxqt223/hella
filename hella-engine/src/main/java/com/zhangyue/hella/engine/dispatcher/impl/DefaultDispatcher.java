package com.zhangyue.hella.engine.dispatcher.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.zhangyue.hella.common.protocol.JobEvent;
import com.zhangyue.hella.engine.cluster.IEngineClusterManager;
import com.zhangyue.hella.engine.dispatcher.IEventDispatcher;

public class DefaultDispatcher implements IEventDispatcher {

    private final static Logger LOG = Logger.getLogger(DefaultDispatcher.class);
    private Map<String, List<JobEvent>> jobEventsMap;
    private IEngineClusterManager engineClusterManager;

    public DefaultDispatcher(IEngineClusterManager engineClusterManager){
        this.jobEventsMap = new ConcurrentHashMap<String, List<JobEvent>>();
        this.engineClusterManager = engineClusterManager;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.zhangyue.hella.engine.dispatcher.IEventDispatcher#doDispatch(com.
     * zhangyue.hella.common.protocol.JobEvent)
     */
    @Override
    public void doDispatch(JobEvent jobEvent) {
        synchronized (jobEventsMap) {
            if (null == jobEvent) {
                return;
            }
            
            /** 如果执行器节点没有注册，那么直接不发送事件，让该作业计划成为挂起状态 */
            if(!engineClusterManager.isExecutorAlreadyRegister(jobEvent.getExecutorClusterID())){
                LOG.warn("This executor has not register.executorClusterID:"+jobEvent.getExecutorClusterID());
                return ;
            }
            String executorClusterID = jobEvent.getExecutorClusterID();
            List<JobEvent> jobEventList;
            if (jobEventsMap.containsKey(executorClusterID)) {
                jobEventList = jobEventsMap.get(executorClusterID);
            } else {
                jobEventList = new ArrayList<JobEvent>();
            }
            jobEventList.add(jobEvent);
            jobEventsMap.put(executorClusterID, jobEventList);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.zhangyue.hella.engine.dispatcher.IEventDispatcher#
     * getJobEventsByExecutorClusterID(java.lang.String)
     */
    @Override
    public List<JobEvent> getJobEventsByExecutorClusterID(String executorClusterID) {
        synchronized (jobEventsMap) {
            if (jobEventsMap.containsKey(executorClusterID)) {
                return jobEventsMap.remove(executorClusterID);
            }
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.zhangyue.hella.engine.dispatcher.IEventDispatcher#
     * removeJobEventsByExecutorClusterID(java.lang.String)
     */
    @Override
    public void removeJobEventsByExecutorClusterID(String executorClusterID) {
        synchronized (jobEventsMap) {
            if (jobEventsMap.containsKey(executorClusterID)) {
                jobEventsMap.remove(executorClusterID);
            }
        }
    }
}
