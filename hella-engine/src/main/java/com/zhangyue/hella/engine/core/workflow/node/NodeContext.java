package com.zhangyue.hella.engine.core.workflow.node;

import com.zhangyue.hella.common.protocol.JobProgress;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.engine.core.workflow.IWorkflowInstance;

/**
 * 
 * @Descriptions XJob上下文
 *
 * @author scott 
 * @date 2013-8-19 下午2:31:30
 * @version 1.0
 */
public interface NodeContext {

    /**
     * 获取作业计划节点状态
     * @return
     */
    public JobPlanNodeState getJobPlanNodeState();

    /**
     * 设置作业计划节点状态
     * @param jobPlanNodeState
     */
    public void setJobPlanNodeState(JobPlanNodeState jobPlanNodeState);

    /**
     * 获取作业进度
     * @return
     */
    public JobProgress getJobProgress();

    /**
     * 设置当前作业进度
     * @param jobProgress
     */
    public void setCurrentJobProgress(JobProgress jobProgress);

    /**
     * 获取当前执行器key
     */
    public String getCurrentExecutorKey();

    /**
     * 设置变量值
     * @param name 变量名
     * @param value 变量值
     */
    public void setVar(String name, String value);

    /**
     * 获取某变量值
     * @param name
     * @return
     */
    public String getVar(String name);

    /**
     * 删除某变量
     * @param name
     */
    public void removeVar(String name);

    /**
     * 获取作业流实例
     * @return
     */
    public IWorkflowInstance getWorkflowInstance();

    /**
     * 获取作业参数
     * @return
     */
    public String getArgs();

}
