package com.zhangyue.hella.engine.core.workflow;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.zhangyue.hella.common.util.JobPlanNodeType;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;

public class LiteWorkflowManager implements IWorkflowManager {

    private JobExecutionPlan jobExecutionPlan;

    private Map<String, JobPlanNode> nodesMap = new LinkedHashMap<String, JobPlanNode>();

    private boolean complete = false;

    public LiteWorkflowManager(JobExecutionPlan jobExecutionPlan) throws WorkflowException{
        this.jobExecutionPlan = jobExecutionPlan;
        addNode(jobExecutionPlan.getJobPlanNodeList());
    }

    private LiteWorkflowManager addNode(List<JobPlanNode> list) throws WorkflowException {
        for (JobPlanNode node : list) {
            try {
                if (nodesMap.containsKey(node.getName())) {
                    throw new WorkflowException("this node " + node.getName() + " already defined");
                }
                if (null != node.getToNode() && node.getToNode().equals(node.getName())) {
                    throw new WorkflowException("Node " + node.getName() + " cannot transition to itself");
                }
                nodesMap.put(node.getName(), node);
                if (node.getTypeEnum() == JobPlanNodeType.end) {
                    complete = true;
                }
            } catch (Exception e) {
                throw new WorkflowException("Node " + node.getName() + " cannot transition to itself",e);
            }
        }
        if (!complete) {
            throw new WorkflowException("this end node has not defined");
        }
        return this;
    }

    public Collection<JobPlanNode> getNodeDefs() {
        return Collections.unmodifiableCollection(nodesMap.values());
    }

    public JobPlanNode getNode(String name) {
        return nodesMap.get(name);
    }

    public String getStartNodeName() {
        for (JobPlanNode node : getNodeDefs()) {
            if (node.getTypeEnum() == JobPlanNodeType.start) {
                return node.getName();
            }
        }
        return null;
    }

    @Override
    public JobExecutionPlan getJobExecutionPlan() {
        return jobExecutionPlan;
    }

}
