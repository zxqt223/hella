package com.zhangyue.hella.engine.db.entity;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.zhangyue.hella.common.util.JobPlanNodeType;
import com.zhangyue.hella.engine.util.EngineConstant;

/**
 * 作业计划节点
 * 
 * @author scott
 * @date 2013-8-19 下午2:43:52
 * @version 1.0
 */
public class JobPlanNode implements Serializable {

    public enum DelayType {
        soft, hard;
    }

    private static final long serialVersionUID = -102156401793L;
    public static final String TABLE_NAME = EngineConstant.SCHED_TABLE_PREFIX + "JOBPLAN_NODE";
    
    private int id;
    private int jobExecutionPlanID;
    private String type;
    private String name;
    private String executorClusterID;
    private String forkName;
    private String joinName;
    private String toNode;
    private String okNode;
    private String errorNode;
    private String delayType = DelayType.soft.name();
    private int delayTime = 0;
    private int errorMaxRedoTimes = 0;
    private int errorRedoPeriod = 0;
    private transient XjobMeta xjobMeta = null;

    public XjobMeta getXjobMeta() {
        return xjobMeta;
    }

    public void setXjobMeta(XjobMeta xjobMeta) {
        this.xjobMeta = xjobMeta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobExecutionPlanID() {
        return jobExecutionPlanID;
    }

    public void setJobExecutionPlanID(int jobExecutionPlanID) {
        this.jobExecutionPlanID = jobExecutionPlanID;
    }

    public String getType() {
        return type;
    }

    public JobPlanNodeType getTypeEnum() {
        if (StringUtils.isNotBlank(type)) {
            return JobPlanNodeType.valueOf(type);
        }
        return null;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTypeEnum(JobPlanNodeType type) {
        this.type = type.name();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOkNode() {
        return okNode;
    }

    public void setOkNode(String okNode) {
        this.okNode = okNode;
    }

    public String getErrorNode() {
        return errorNode;
    }

    public void setErrorNode(String errorNode) {
        this.errorNode = errorNode;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public int getErrorMaxRedoTimes() {
        return errorMaxRedoTimes;
    }

    public void setErrorMaxRedoTimes(int errorMaxRedoTimes) {
        this.errorMaxRedoTimes = errorMaxRedoTimes;
    }

    public DelayType getDelayTypeEnum() {
        if (StringUtils.isNotBlank(delayType)) {
            return DelayType.valueOf(delayType);
        }
        return null;
    }

    public String getDelayType() {
        return delayType;
    }

    public void setDelayType(String delayType) {
        this.delayType = delayType;
    }

    public void setDelayTypeEnum(DelayType delayType) {
        this.delayType = delayType.name();
    }

    public String getToNode() {
        return toNode;
    }

    public void setToNode(String toNode) {
        this.toNode = toNode;
    }

    public String getForkName() {
        return forkName;
    }

    public void setForkName(String forkName) {
        this.forkName = forkName;
    }

    public String getJoinName() {
        return joinName;
    }

    public void setJoinName(String joinName) {
        this.joinName = joinName;
    }

    public String getExecutorClusterID() {
        return executorClusterID;
    }

    public void setExecutorClusterID(String executorClusterID) {
        this.executorClusterID = executorClusterID;
    }

    public int getErrorRedoPeriod() {
        return errorRedoPeriod;
    }

    public void setErrorRedoPeriod(int errorRedoPeriod) {
        this.errorRedoPeriod = errorRedoPeriod;
    }

    @Override
    public String toString() {
        return "JobPlanNode [id=" + id + ", jobExecutionPlanID=" + jobExecutionPlanID + ", type=" + type + ", name="
               + name + ", executorClusterID=" + executorClusterID + ", forkName=" + forkName + ", joinName="
               + joinName + ", toNode=" + toNode + ", okNode=" + okNode + ", errorNode=" + errorNode + ", delayType="
               + delayType + ", delayTime=" + delayTime + ", errorMaxRedoTimes=" + errorMaxRedoTimes
               + ", errorRedoPeriod=" + errorRedoPeriod + "]";
    }

}
