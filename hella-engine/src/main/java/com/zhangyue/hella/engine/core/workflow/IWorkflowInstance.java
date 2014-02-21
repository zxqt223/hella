package com.zhangyue.hella.engine.core.workflow;

import java.util.Map;

import com.zhangyue.hella.common.util.WorkflowStatus;
import com.zhangyue.hella.engine.core.workflow.node.NodeContext;
import com.zhangyue.hella.engine.manager.IJobEventManager;

/**
 * @Descriptions The class WorkflowInstance.java's implementation：流程实例
 * @author scott
 * @date 2013-8-19 下午2:30:53
 * @version 1.0
 */
public interface IWorkflowInstance {

    public void initialize(IJobEventManager jobEventManager, IWorkflowManager workflowManager, String args)
        throws WorkflowException;

    public String getWorkflowKey();

    public void start() throws Exception;

    public void completeJobPlanNode(String nodeName, NodeContext context);

    public void suspend() throws Exception;

    public void resume() throws Exception;

    public WorkflowStatus getStatus();

    public void setVar(String name, String value);

    public String getVar(String name);

    public void removeVar(String name);

    public Map<String, String> getAllVars();

    public void setAllVars(Map<String, String> varMap);

    public boolean isHasAlarm();

    public void setHasAlarm(boolean hasAlarm);

    public boolean isTimeOut();

    public void setTimeOut(boolean timeOut);

    public IWorkflowManager getWorkflowManager();

    public void setArgs(String args);

    public String getArgs();

}
