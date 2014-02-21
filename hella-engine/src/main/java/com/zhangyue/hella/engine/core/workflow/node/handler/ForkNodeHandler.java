package com.zhangyue.hella.engine.core.workflow.node.handler;

import java.util.ArrayList;
import java.util.List;

import com.zhangyue.hella.common.util.JobPlanNodeType;
import com.zhangyue.hella.engine.core.workflow.WorkflowException;
import com.zhangyue.hella.engine.core.workflow.node.NodeContext;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;

public class ForkNodeHandler extends NodeHandler {

    private JobPlanNode jobPlanNode;
    
	public ForkNodeHandler(JobPlanNode jobPlanNode) {
		this.jobPlanNode = jobPlanNode;
	}

	public boolean enter(NodeContext xjobContext) throws WorkflowException {
		return true;
	}

	public List<String> multiExit(NodeContext xjobContext) throws Exception {
		String[] toPaths = jobPlanNode.getToNode().split(",");
		List<String> toNodes = new ArrayList<String>(toPaths.length);
		for (String name : toPaths) {
			toNodes.add(name);
		}
		xjobContext.setVar(jobPlanNode.getName() + jobPlanNode.getJoinName(), String.valueOf(toNodes.size()));
		return toNodes;
	}

	public String exit(NodeContext xjobContext) {
		throw new UnsupportedOperationException();
	}

	public void kill(NodeContext xjobContext) {
	}

	public void fail(NodeContext xjobContext) {
	}

	@Override
	public JobPlanNodeType getJobPlanNodeType() {
		return JobPlanNodeType.fork;
	}

}
