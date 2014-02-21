package com.zhangyue.hella.engine.entity;

import org.apache.commons.lang.StringUtils;

import com.zhangyue.hella.common.util.DateUtil;

public class JobCount {

    private String clusterID;
    private String createTime;

    private long ableJobPlanNum;
    private long disableJobPlanNum;

    private long runningXjobsNum;
    private long failXjobsNum;
    private long successXjobsNum;

    public String getClusterID() {
        return clusterID;
    }

    public void setClusterID(String clusterID) {
        this.clusterID = clusterID;
    }

    public String getCreateTimeDisplay() {
        if (StringUtils.isNotBlank(createTime)) {
            return DateUtil.parseCnDate(createTime);
        }
        return null;
    }

    public long getAbleJobPlanNum() {
        return ableJobPlanNum;
    }

    public void setAbleJobPlanNum(long ableJobPlanNum) {
        this.ableJobPlanNum = ableJobPlanNum;
    }

    public long getDisableJobPlanNum() {
        return disableJobPlanNum;
    }

    public void setDisableJobPlanNum(long disableJobPlanNum) {
        this.disableJobPlanNum = disableJobPlanNum;
    }

    public long getRunningXjobsNum() {
        return runningXjobsNum;
    }

    public void setRunningXjobsNum(long runningXjobsNum) {
        this.runningXjobsNum = runningXjobsNum;
    }

    public long getFailXjobsNum() {
        return failXjobsNum;
    }

    public void setFailXjobsNum(long failXjobsNum) {
        this.failXjobsNum = failXjobsNum;
    }

    public long getSuccessXjobsNum() {
        return successXjobsNum;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setSuccessXjobsNum(long successXjobsNum) {
        this.successXjobsNum = successXjobsNum;
    }

}
