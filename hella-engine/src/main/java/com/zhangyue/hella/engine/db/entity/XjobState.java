package com.zhangyue.hella.engine.db.entity;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.engine.util.EngineConstant;

/**
 * @Descriptions The class XjobState.java's implementation：Xjob状态
 * @author scott
 * @date 2013-8-19 下午2:46:25
 * @version 1.0
 */
public class XjobState implements Serializable {

    private static final long serialVersionUID = -102151628615401793L;
    public static final String TABLE_NAME = EngineConstant.SCHED_TABLE_PREFIX
                                            + "XJOB_STATE";

    private int id;
    private int jobPlanNodeID;
    private String currentExecutorKey;
    private String jobPlanNodeState = JobPlanNodeState.INIT.name();
    /** 失败信息 */
    private String runInfo = "";
    /** job执行时间 */
    private String runTime;
    /** 重做次数 */
    private int executeTimes = 0;
    /** 已完成百分比 */
    private int finishedPercent = 0;

    /** 临时变量，没有进行关联查询 需手动set值 **/
    private transient String jobPlanNodeName;

    /****/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobPlanNodeID() {
        return jobPlanNodeID;
    }

    public void setJobPlanNodeID(int jobPlanNodeID) {
        this.jobPlanNodeID = jobPlanNodeID;
    }

    public String getRunInfo() {
        return runInfo;
    }

    public void setRunInfo(String runInfo) {
        this.runInfo = runInfo;
    }

    public String getRunTime() {
        return runTime;
    }

    public String getRunTimeDisplay() {
        if (StringUtils.isNotBlank(runTime)) {
            return DateUtil.parseEnDate(runTime);
        }
        return null;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public int getExecuteTimes() {
        return executeTimes;
    }

    public void setExecuteTimes(int executeTimes) {
        this.executeTimes = executeTimes;
    }

    public int getFinishedPercent() {
        return finishedPercent;
    }

    public void setFinishedPercent(int finishedPercent) {
        this.finishedPercent = finishedPercent;
    }

    public String getJobPlanNodeName() {
        return jobPlanNodeName;
    }

    public void setJobPlanNodeName(String jobPlanNodeName) {
        this.jobPlanNodeName = jobPlanNodeName;
    }

    public String getCurrentExecutorKey() {
        return currentExecutorKey;
    }

    public void setCurrentExecutorKey(String currentExecutorKey) {
        this.currentExecutorKey = currentExecutorKey;
    }

    public String getJobPlanNodeState() {
        return jobPlanNodeState;
    }

    public JobPlanNodeState getJobPlanNodeStateEnum() {
        if (StringUtils.isNotBlank(jobPlanNodeState)) {
            return JobPlanNodeState.valueOf(jobPlanNodeState);
        }
        return null;
    }

    public void setJobPlanNodeState(String jobPlanNodeState) {
        this.jobPlanNodeState = jobPlanNodeState;
    }

    public void setJobPlanNodeStateEnum(JobPlanNodeState jobPlanNodeState) {
        this.jobPlanNodeState = jobPlanNodeState.name();
    }

    @Override
    public String toString() {
        return "XjobState [id=" + id + ", jobPlanNodeID=" + jobPlanNodeID
               + ", currentExecutorKey=" + currentExecutorKey
               + ", jobPlanNodeState=" + jobPlanNodeState + ", runInfo="
               + runInfo + ", runTime=" + runTime + ", executeTimes="
               + executeTimes + ", finishedPercent=" + finishedPercent + "]";
    }

}
