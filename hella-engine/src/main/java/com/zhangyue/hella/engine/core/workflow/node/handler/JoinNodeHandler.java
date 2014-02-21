package com.zhangyue.hella.engine.core.workflow.node.handler;

import com.zhangyue.hella.common.util.JobPlanNodeType;
import com.zhangyue.hella.engine.core.workflow.WorkflowException;
import com.zhangyue.hella.engine.core.workflow.node.NodeContext;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;

public class JoinNodeHandler extends NodeHandler {

    private JobPlanNode jobPlanNode;

    public JoinNodeHandler(JobPlanNode jobPlanNode){
        this.jobPlanNode = jobPlanNode;
    }

    public boolean enter(NodeContext xjobContext) throws Exception {
        String forkCount = xjobContext.getVar(jobPlanNode.getForkName() + jobPlanNode.getName());
        if (forkCount == null) {
            throw new WorkflowException("fork count is null");
        }
        int count = Integer.parseInt(forkCount) - 1;
        if (count > 0) {
            xjobContext.setVar(jobPlanNode.getForkName() + jobPlanNode.getName(), String.valueOf(count));
        }
        return (count == 0);
    }

    public String exit(NodeContext xjobContext) {
        xjobContext.removeVar(jobPlanNode.getForkName() + jobPlanNode.getName());
        return jobPlanNode.getToNode();
    }

    public void kill(NodeContext xjobContext) {
    }

    public void fail(NodeContext xjobContext) {
    }

    @Override
    public JobPlanNodeType getJobPlanNodeType() {
        return JobPlanNodeType.join;
    }

}
