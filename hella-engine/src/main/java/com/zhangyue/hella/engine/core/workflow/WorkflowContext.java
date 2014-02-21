package com.zhangyue.hella.engine.core.workflow;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Descriptions The class WorkflowContext.java's implementation：工作流上下文
 * @author scott
 * @date 2013-8-19 下午2:30:03
 * @version 1.0
 */
public class WorkflowContext {

    private static WorkflowContext workflowContext = null;
    private Map<String, IWorkflowInstance> workflowsMap = new ConcurrentHashMap<String, IWorkflowInstance>();
    private Map<String, JobNodeRuningInfo> jobNodeRuningInfosMap = new ConcurrentHashMap<String, JobNodeRuningInfo>();

    private WorkflowContext(){
    }

    public static WorkflowContext getInstance() {
        if (workflowContext == null) {
            synchronized (WorkflowContext.class) {
                if (workflowContext == null) {
                    workflowContext = new WorkflowContext();
                }
            }
        }
        return workflowContext;
    }

    public synchronized void addWorkflow(IWorkflowInstance workflow) {
        workflowsMap.put(workflow.getWorkflowKey(), workflow);
    }

    public synchronized IWorkflowInstance getWorkflow(String workflowRunKey) {
        return workflowsMap.get(workflowRunKey);
    }

    public synchronized Set<String> getWorkflowRunKeys() {
        return workflowsMap.keySet();
    }

    public synchronized void removeWorkflow(String workflowRunKey) {
        workflowsMap.remove(workflowRunKey);
    }

    public synchronized void clearWorkflows() {
        workflowsMap.clear();
        jobNodeRuningInfosMap.clear();
    }

    public void addJobNodeRuningInfo(String eventID, String jobNodeName, int xjobStateID, String workflowRunKey) {
        if (workflowRunKey == null) {
            throw new RuntimeException("The workflowRunKey must not null!eventID:" + eventID + ",jobNodeName:"
                                       + jobNodeName + ",xjobStateID:" + xjobStateID + ",workflowRunKey:"
                                       + workflowRunKey);
        }
        JobNodeRuningInfo jobNodeRuningInfo = new JobNodeRuningInfo(jobNodeName, xjobStateID, workflowRunKey);
        jobNodeRuningInfosMap.put(eventID, jobNodeRuningInfo);
    }

    public void removeJobNodeRuningInfo(String eventID) {
        jobNodeRuningInfosMap.remove(eventID);
    }

    public IWorkflowInstance getWorkflowByEventID(String eventID) {
        return getWorkflow(jobNodeRuningInfosMap.get(eventID).getWorkflowRunKey());
    }

    public int getXjobStateIDByEventID(String eventID) {
        return jobNodeRuningInfosMap.get(eventID).getXjobStateID();
    }

    public String getJobNodeNameByEventID(String eventID) {
        JobNodeRuningInfo o = jobNodeRuningInfosMap.get(eventID);
        return null == o ? null : o.getJobNodeName();
    }

    /**
     * 正在运行的作业节点信息
     * 
     * @date 2014-1-29
     * @author scott
     */
    private class JobNodeRuningInfo {

        private String jobNodeName;
        private int xjobStateID;
        private String workflowRunKey;

        public JobNodeRuningInfo(String jobNodeName, int xjobStateID, String workflowRunKey){
            super();
            this.jobNodeName = jobNodeName;
            this.xjobStateID = xjobStateID;
            this.workflowRunKey = workflowRunKey;
        }

        public int getXjobStateID() {
            return xjobStateID;
        }

        public String getWorkflowRunKey() {
            return workflowRunKey;
        }

        public String getJobNodeName() {
            return jobNodeName;
        }
    }
}
