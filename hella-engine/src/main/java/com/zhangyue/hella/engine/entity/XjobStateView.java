package com.zhangyue.hella.engine.entity;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.common.util.JobPlanNodeType;
import com.zhangyue.hella.common.util.JobPlanType;

/**
 * @Descriptions The class XjobStateDTO.java's implementation：XjobState 用于联合查询
 *               或者 页面显示 所需数据封装
 * @author scott
 * @date 2013-8-19 下午2:42:46
 * @version 1.0
 */
public class XjobStateView implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String[] COLORS = { "blue", "gold", "purple", "orangered", "slategray" };
    private int id;
    private String jobPlanType;
    private String executorClusterID;
    private String clusterID;
    private String jobPlanVersion;
    private String jepName;
    private String jobPlanNodeName;
    private String jobPlanNodeType;
    private String jobPlanNodeState;
    private String runInfo;
    private String runTime;
    private int executeTimes = 0;
    private int finishedPercent;
    private String currentExecutorKey;

    public String getClusterID() {
        return clusterID;
    }

    public void setClusterID(String clusterID) {
        this.clusterID = clusterID;
    }

    public String getJobPlanVersion() {
        return jobPlanVersion;
    }

    public void setJobPlanVersion(String jobPlanVersion) {
        this.jobPlanVersion = jobPlanVersion;
    }

    public String getJepName() {
        return jepName;
    }

    public void setJepName(String jepName) {
        this.jepName = jepName;
    }

    public String getJobPlanNodeName() {
        return jobPlanNodeName;
    }

    public String getDisplayJobPlanNodeName() {
        return "<font size=3 color=" + getDisplayColor() + ">" + jobPlanNodeName + "</font>";

    }

    public void setJobPlanNodeName(String jobPlanNodeName) {
        this.jobPlanNodeName = jobPlanNodeName;
    }

    public String getJobPlanNodeState() {
        return jobPlanNodeState;
    }

    public JobPlanNodeState getJobPlanNodeStateEnum() {
        if (StringUtils.isNotBlank(jobPlanNodeState)) {
            JobPlanNodeState jobPlanNodeStateEnum = JobPlanNodeState.valueOf(jobPlanNodeState);
            return jobPlanNodeStateEnum;
        }
        return null;
    }

    public String getCurrentExecutorKey() {
        return currentExecutorKey;
    }

    public String getDisplayColor() {
        int index = Math.abs(getCurrentExecutorKey().hashCode()) % 5;
        return COLORS[index];
    }

    public String getJobPlanNodeStateName() {
        if (StringUtils.isNotBlank(jobPlanNodeState)) {
            JobPlanNodeState jobPlanNodeStateEnum = JobPlanNodeState.valueOf(jobPlanNodeState);
            if (jobPlanNodeStateEnum.isFailState()) {
                return "<font size=3 color=red>" + jobPlanNodeStateEnum.getStateName() + "</font>";
            } else if (jobPlanNodeStateEnum == JobPlanNodeState.RESULT_SUCCESS) {
                return "<font size=3 color=green>" + jobPlanNodeStateEnum.getStateName() + "</font>";
            } else {
                return "<font size=3 color=black>" + jobPlanNodeStateEnum.getStateName() + "</font>";
            }
        }
        return null;

    }

    public void setJobPlanNodeState(String jobPlanNodeState) {
        this.jobPlanNodeState = jobPlanNodeState;
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

    public String getRunTimeFormater() {
        if (null != this.runTime && !"".equals(runTime)) {
            return DateUtil.parseCnDate(this.runTime);
        }
        return null;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public int getExecuteTimes() {

        return executeTimes;
    }

    public String getDisplayExecuteTimes() {
        return (executeTimes == 0 ? "" : "<font size=3 color=red>" + executeTimes + "</font>");
    }

    public void setExecuteTimes(int executeTimes) {
        this.executeTimes = executeTimes;
    }

    public int getFinishedPercent() {
        return finishedPercent;
    }

    public String getDisplayFinishedPercent() {
        if (getJobPlanNodeTypeEnum() == JobPlanNodeType.action) {
            return finishedPercent + " %";
        }
        return "";
    }

    public void setFinishedPercent(int finishedPercent) {
        this.finishedPercent = finishedPercent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJobPlanNodeType() {
        return jobPlanNodeType;
    }

    public JobPlanNodeType getJobPlanNodeTypeEnum() {
        return StringUtils.isNotBlank(jobPlanNodeType) ? JobPlanNodeType.valueOf(jobPlanNodeType) : null;
    }

    public void setJobPlanNodeType(String jobPlanNodeType) {
        this.jobPlanNodeType = jobPlanNodeType;
    }

    public String getExecutorClusterID() {
        return executorClusterID;
    }

    public void setExecutorClusterID(String executorClusterID) {
        this.executorClusterID = executorClusterID;
    }

    public JobPlanType getJobPlanTypeEnum() {
        if (StringUtils.isNotBlank(jobPlanType)) {
            return JobPlanType.valueOf(jobPlanType);
        }
        return null;
    }

    public void setJobPlanTypeEnum(JobPlanType jobPlanType) {
        this.jobPlanType = jobPlanType.name();
    }

    public String getJobPlanType() {
        return jobPlanType;
    }

    public void setJobPlanType(String jobPlanType) {
        this.jobPlanType = jobPlanType;
    }

    @Override
    public String toString() {
        return "XjobStateDTO [id=" + id + ", clusterID=" + clusterID + ", jobPlanVersion=" + jobPlanVersion
               + ", jepName=" + jepName + ", jobPlanNodeName=" + jobPlanNodeName + ", jobPlanNodeState="
               + jobPlanNodeState + ", runInfo=" + runInfo + ", runTime=" + runTime + ", executeTimes=" + executeTimes
               + ", finishedPercent=" + finishedPercent + "]";
    }

    public void setCurrentExecutorKey(String currentExecutorKey) {
        this.currentExecutorKey = currentExecutorKey;
    }

}
