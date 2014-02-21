package com.zhangyue.hella.engine.core.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.util.JobPlanNodeType;
import com.zhangyue.hella.common.util.UniqueIDGenerator;
import com.zhangyue.hella.common.util.WorkflowStatus;
import com.zhangyue.hella.engine.core.workflow.node.JobPlanNodeContext;
import com.zhangyue.hella.engine.core.workflow.node.NodeContext;
import com.zhangyue.hella.engine.core.workflow.node.handler.NodeHandler;
import com.zhangyue.hella.engine.core.workflow.node.handler.NodeHandlerFactory;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;
import com.zhangyue.hella.engine.manager.IJobEventManager;

public class LiteWorkflowInstance implements IWorkflowInstance {

    private static Logger LOG = LoggerFactory.getLogger(LiteWorkflowInstance.class);
    private boolean hasAlarm = false;
    private boolean timeOut = false;

    private IWorkflowManager workflowManager;
    private IJobEventManager jobEventManager;
    private String engineRunKey;
    private WorkflowStatus status;
    private String args = null;
    private Map<String, String> vars = new HashMap<String, String>();

    public void initialize(IJobEventManager jobEventManager, IWorkflowManager workflowManager, String args)
        throws WorkflowException {
        this.jobEventManager = jobEventManager;
        this.workflowManager = workflowManager;
        this.status = WorkflowStatus.PREP;
        this.setArgs(args);
        this.engineRunKey = String.valueOf(UniqueIDGenerator.generateID());
    }

    @Override
    public String getWorkflowKey() {
        return this.engineRunKey + workflowManager.getJobExecutionPlan().getId();
    }

    @Override
    public void start() throws Exception {
        if (status != WorkflowStatus.PREP) {
            throw new WorkflowException(" Work flowInstance has not PREP ");
        }
        status = WorkflowStatus.RUNNING;
        String startNodeName = workflowManager.getStartNodeName();
        if (null == startNodeName) {
            throw new WorkflowException(" Work start node has not find ");
        }
        signal(startNodeName, new JobPlanNodeContext(this));
    }

    @Override
    public void completeJobPlanNode(String nodeName, NodeContext context) {
        try {
            NodeHandler pnodeHandler =
                    NodeHandlerFactory.createNodeHandler(jobEventManager, workflowManager.getNode(nodeName));
            List<String> multiExit = pnodeHandler.multiExit(context);
            processNextNodes(multiExit);
        } catch (Exception e) {
            LOG.error("Fail to execute complete job node!", e);
            status = WorkflowStatus.FAILED;
        }

        if (status.isEndState()) {
            WorkflowContext.getInstance().removeWorkflow(this.getWorkflowKey());
        }
    }

    public synchronized void fail() throws WorkflowException {
        if (status.isEndState()) {
            throw new WorkflowException("this Workflow has end,so can not make it failed");
        }
        status = WorkflowStatus.FAILED;
    }

    public synchronized void suspend() throws WorkflowException {
        if (status != WorkflowStatus.RUNNING) {
            throw new WorkflowException("this Workflow status is not running ,so can not suspend");
        }
        this.status = WorkflowStatus.SUSPENDED;
    }

    public boolean isSuspended() {
        return (status == WorkflowStatus.SUSPENDED);
    }

    public synchronized void resume() throws WorkflowException {
        if (status != WorkflowStatus.SUSPENDED) {
            throw new WorkflowException("this Workflow status is not suspended ,so can not resume");
        }
        status = WorkflowStatus.RUNNING;
    }

    @Override
    public WorkflowStatus getStatus() {
        return status;
    }

    @Override
    public void setVar(String name, String value) {
        vars.put(name, value);
    }

    public void removeVar(String name) {
        vars.remove(name);
    }

    @Override
    public String getVar(String name) {
        return vars.get(name);
    }

    @Override
    public Map<String, String> getAllVars() {
        return vars;
    }

    @Override
    public void setAllVars(Map<String, String> varMap) {
        vars = varMap;
    }

    public boolean isHasAlarm() {
        return hasAlarm;
    }

    public void setHasAlarm(boolean hasAlarm) {
        this.hasAlarm = hasAlarm;
    }

    public boolean isTimeOut() {
        return timeOut;
    }

    public void setTimeOut(boolean timeOut) {
        this.timeOut = timeOut;
    }

    @Override
    public IWorkflowManager getWorkflowManager() {
        return workflowManager;
    }

    @Override
    public void setArgs(String args) {
        this.args = args;
    }

    @Override
    public String getArgs() {
        return this.args;
    }

    /**
     * 处理工作流中的当前节点
     * @param nodeName 工作流节点名称
     * @param context 节点上下文
     */
    private void signal(String nodeName, NodeContext context) {
        if (status != WorkflowStatus.RUNNING) {
            return;
        }
        try {
            NodeHandler pnodeHandler =
                    NodeHandlerFactory.createNodeHandler(jobEventManager, workflowManager.getNode(nodeName));
            boolean exiting = pnodeHandler.enter(context);
            if (exiting) {
                List<String> multiExit = pnodeHandler.multiExit(context);
                processNextNodes(multiExit);
            }
        } catch (Exception e) {
            LOG.error("Fail to process job plan node.", e);
            status = WorkflowStatus.FAILED;
        }
    }

    /**
     * 处理工作流中的下游节点
     * @param multiExit
     * @throws Exception
     */
    private void processNextNodes(List<String> multiExit) throws Exception {
        if (status.isEndState()) {
            return ;
        }
        for (String name : multiExit) {
            JobPlanNode nextNode = workflowManager.getNode(name);
            if (null != nextNode) {
                NodeContext nextcontext = new JobPlanNodeContext(this);
                if (!nextNode.getTypeEnum().isFinalType()) {
                    signal(nextNode.getName(), nextcontext);
                } else {
                    NodeHandler nextnodeHandler = NodeHandlerFactory.createNodeHandler(jobEventManager, nextNode);
                    nextnodeHandler.enter(nextcontext);
                    if (nextNode.getTypeEnum() == JobPlanNodeType.end) {
                        nextnodeHandler.exit(nextcontext);
                        status = WorkflowStatus.SUCCEEDED;
                    }
                    
                    if (nextNode.getTypeEnum() == JobPlanNodeType.fail) {
                        nextnodeHandler.exit(nextcontext);
                        status = WorkflowStatus.FAILED;
                    }
                }
            }
        }
    }
}
