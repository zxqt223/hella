package com.zhangyue.hella.engine.core.workflow.node.handler;

import java.util.ArrayList;
import java.util.List;

import com.zhangyue.hella.common.util.JobPlanNodeType;
import com.zhangyue.hella.engine.core.workflow.node.NodeContext;

/**
 * 作业流中的节点处理器
 * @author scott
 * @date 2013-8-19 下午2:32:34
 * @version 1.0
 */
public abstract class NodeHandler {

    public abstract boolean enter(NodeContext xjobContext) throws Exception;

    public abstract JobPlanNodeType getJobPlanNodeType();

    public abstract String exit(NodeContext xjobContext) throws Exception;

    public List<String> multiExit(NodeContext xjobContext) throws Exception {
        List<String> transitions = new ArrayList<String>(1); // 默认情况下就是一个输出，当作业流节点是fork的时候，需要重写此方法
        transitions.add(exit(xjobContext));
        return transitions;
    }

    public void kill(NodeContext xjobContext) {

    }

    public void fail(NodeContext xjobContext) {

    }
}
