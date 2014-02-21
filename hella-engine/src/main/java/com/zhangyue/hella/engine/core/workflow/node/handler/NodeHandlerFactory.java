package com.zhangyue.hella.engine.core.workflow.node.handler;

import com.zhangyue.hella.engine.core.workflow.WorkflowException;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;
import com.zhangyue.hella.engine.manager.IJobEventManager;
import com.zhangyue.hella.engine.manager.IJobPlanManager;
import com.zhangyue.hella.engine.manager.IJobStateManager;

/**
 * 
 * @Descriptions The class NodeHandlerFactory.java's implementation：结点任务处理工厂类
 *
 * @author scott 
 * @date 2013-8-19 下午2:32:54
 * @version 1.0
 */
public class NodeHandlerFactory {

    private static IJobPlanManager jobPlanManager;
    private static IJobStateManager jobStateManager;

    /**
     * 创建节点处理器
     * @param jobEventManager 作业事件管理器
     * @param jobPlanNode 作业计划节点
     * @return 节点处理器实例，不会出现空
     * @throws WorkflowException 当存在不识别的节点类型的时候会抛出异常
     */
    public static NodeHandler createNodeHandler(IJobEventManager jobEventManager,JobPlanNode jobPlanNode)
        throws WorkflowException {
        NodeHandler nodeHandler = null;
        switch (jobPlanNode.getTypeEnum()) {
            case start:
                nodeHandler = new StartNodeHandler(jobPlanNode,jobStateManager,jobPlanManager);
                break;
            case action:
                nodeHandler = new ActionNodeHandler(jobPlanNode,jobStateManager,jobPlanManager);
                break;
            case fork:
                nodeHandler = new ForkNodeHandler(jobPlanNode);
                break;
            case join:
                nodeHandler = new JoinNodeHandler(jobPlanNode);
                break;
            case fail:
                nodeHandler = new FailNodeHandler(jobPlanNode,jobStateManager,jobPlanManager);
                break;
            case end:
                nodeHandler = new EndNodeHandler(jobPlanNode,jobStateManager,jobPlanManager,jobEventManager);
                break;
            default:
                throw new WorkflowException("It does not find nodeHandler!jobPlanNodeType:"+jobPlanNode.getTypeEnum().name());
        }
        return nodeHandler;
    }
    
    public static void initialize(IJobPlanManager jobPlanManager,IJobStateManager jobStateManager){
        NodeHandlerFactory.jobPlanManager = jobPlanManager;
        NodeHandlerFactory.jobStateManager = jobStateManager;
    }

}
