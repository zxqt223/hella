package com.zhangyue.hella.common.util;

/**
 * Description: xjob执行类型：自动则会触发下级依赖job执行，一次则是之执行本次job.<br>
 * Copyright: Copyright (c) 2012 <br>
 * Company: www.renren.com
 * 
 * @author zhuhui{hui.zhu@renren-inc.com} 2012-8-17
 * @version 1.0
 */
public enum JobPlanNodeState {

    INIT("初始化"), NOTIFY_SUCCESS("作业通知成功，下一步进行作业调度"), NOTIFY_FAIL("作业通知失败"), DISPATCH_SUCCESS("作业调度成功，正在返回结果"),
    DISPATCH_FAIL("作业调度失败"), RESULT_SUCCESS("成功"), RESULT_ERROR("失败");

    /** 结果状态 */
    public final static JobPlanNodeState[] RESULT_STATES = { DISPATCH_FAIL, RESULT_SUCCESS, RESULT_ERROR };
    /** 运行中的作业计划节点状态 */
    public final static JobPlanNodeState[] RUNNING_JOB_PLAN_NODE_STATES = { INIT, NOTIFY_SUCCESS, DISPATCH_SUCCESS };
    /** 失败的作业计划节点状态 */
    public final static JobPlanNodeState[] FAIL_JOB_PLAN_NODE_STATES = { NOTIFY_FAIL, DISPATCH_FAIL, RESULT_ERROR };

    /** 类型名称 */
    private final String stateName;

    /**
     * @param stateName {@link #stateName}
     */
    private JobPlanNodeState(String stateName){
        this.stateName = stateName;
    }

    /**
     * @return {@link #typeName}
     */
    public String getStateName() {
        return stateName;
    }

    public boolean isResultState() {
        for (JobPlanNodeState t : RESULT_STATES) {
            if (this == t) {
                return true;
            }
        }
        return false;
    }

    public boolean isFailState() {
        for (JobPlanNodeState t : FAIL_JOB_PLAN_NODE_STATES) {
            if (this == t) {
                return true;
            }
        }
        return false;
    }

    public boolean isRunningState() {
        for (JobPlanNodeState t : RUNNING_JOB_PLAN_NODE_STATES) {
            if (this == t) {
                return true;
            }
        }
        return false;
    }
}
