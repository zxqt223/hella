package com.zhangyue.hella.engine.core.workflow.node;

import com.zhangyue.hella.common.protocol.JobProgress;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.engine.core.workflow.IWorkflowInstance;

public class JobPlanNodeContext implements NodeContext {

    private JobProgress currentJobProgress;
    private IWorkflowInstance workflowInstance;
    private JobPlanNodeState jobPlanNodeState;

    public JobPlanNodeContext(IWorkflowInstance workflowInstance){
        this.workflowInstance = workflowInstance;
    }

    public JobPlanNodeContext(IWorkflowInstance workflowInstance,
                              JobProgress currentJobProgress){
        this.workflowInstance = workflowInstance;
        this.currentJobProgress = currentJobProgress;
    }

    @Override
    public void setVar(String name, String value) {
        workflowInstance.setVar(name, value);
    }

    @Override
    public String getVar(String name) {
        return workflowInstance.getVar(name);
    }

    @Override
    public JobProgress getJobProgress() {
        return currentJobProgress;
    }

    public JobProgress getCurrentJobProgress() {
        return currentJobProgress;
    }

    public void setCurrentJobProgress(JobProgress currentJobProgress) {
        this.currentJobProgress = currentJobProgress;
    }

    public IWorkflowInstance getWorkflowInstance() {
        return workflowInstance;
    }

    public void setWorkflowInstance(IWorkflowInstance workflowInstance) {
        this.workflowInstance = workflowInstance;
    }

    public String getCurrentExecutorKey() {
        return (null == workflowInstance ? null : workflowInstance.getWorkflowKey());
    }

    @Override
    public void removeVar(String name) {
        workflowInstance.removeVar(name);
    }

    @Override
    public JobPlanNodeState getJobPlanNodeState() {
        return jobPlanNodeState;
    }

    @Override
    public void setJobPlanNodeState(JobPlanNodeState jobPlanNodeState) {
        this.jobPlanNodeState = jobPlanNodeState;
    }

    public String getArgs() {
        return workflowInstance.getArgs();
    }

}
