package com.zhangyue.hella.engine.core.workflow;

import java.util.Collection;

import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;

/**
 * @Descriptions The class WorkflowApp.java's implementation：工作流接口定义
 * @author scott
 * @date 2013-8-19 下午2:29:11
 * @version 1.0
 */
public interface IWorkflowManager {

    public JobExecutionPlan getJobExecutionPlan();

    public Collection<JobPlanNode> getNodeDefs();

    public JobPlanNode getNode(String name);

    public String getStartNodeName();
}
