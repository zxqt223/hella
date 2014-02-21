package com.zhangyue.hella.engine.db.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.zhangyue.hella.common.util.CronType;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.JobPlanType;
import com.zhangyue.hella.engine.util.EngineConstant;

/**
 * @Descriptions The class JobExecutionPlan.java's implementation：JOB 执行计划
 * @author scott
 * @date 2013-8-19 下午2:43:19
 * @version 1.0
 */
public class JobExecutionPlan implements Serializable {

    private static final long serialVersionUID = -102156401793L;

    public static final String TABLE_NAME = EngineConstant.SCHED_TABLE_PREFIX + "JOB_EXECUTION_PLAN";

    private int id;
    private String jobPlanType = JobPlanType.local.name();
    /**
     * 作业执行计划默认版本ID为0
     */
    private int executePlanVersionID=0;
    private String clusterID;
    private String jepID;
    private String jepName;
    private String cronExpression;
    private boolean ignoreError = true;
    private String cronType;
    private String event;
    private String state = JobExecutionPlan.State.able.name();
    private String currentNode;
    private String currentXjobDate;
    private String currentXjobState;
    private String createDate;
    private String description;

    /**
     *  临时属性，不与数据库对应，只做关联查询封装
     */
    private transient int executePlanVersion;
    private transient List<JobPlanNode> jobPlanNodeList = new ArrayList<JobPlanNode>();

    public int getExecutePlanVersion() {
        return executePlanVersion;
    }

    public void setExecutePlanVersion(int executePlanVersion) {
        this.executePlanVersion = executePlanVersion;
    }

    public List<JobPlanNode> getJobPlanNodeList() {
        return jobPlanNodeList;
    }

    public void addJobPlanNodes(List<JobPlanNode> jobPlanNodeList) {
        this.jobPlanNodeList.addAll(jobPlanNodeList);
    }

    public void addJobPlanNode(JobPlanNode jobPlanNode) {
        this.jobPlanNodeList.add(jobPlanNode);
    }
    /**
     * 获取 job name 规则
     * 
     * @param jobExecutionPlan
     * @return
     */
    public String getQJobName() {
        return this.getClusterID() + "|" + this.getExecutePlanVersionID() + "|" + this.getJepID() + "|"
               + this.getJepName();
    }

    /**
     * 获取Job Group 规则
     * 
     * @param jobExecutionPlan
     * @return
     */
    public String getQJobGroup() {
        return this.getClusterID() + "|" + this.getExecutePlanVersionID() + "|" + this.getJepID() + "|"
               + this.getJepName();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClusterID() {
        return clusterID;
    }

    public void setClusterID(String clusterID) {
        this.clusterID = clusterID;
    }

    public String getJepID() {
        return jepID;
    }

    public void setJepID(String jepID) {
        this.jepID = jepID;
    }

    public String getJepName() {
        return jepName;
    }

    public void setJepName(String jepName) {
        this.jepName = jepName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean isIgnoreError() {
        return ignoreError;
    }

    public void setIgnoreError(boolean ignoreError) {
        this.ignoreError = ignoreError;
    }

    public State getStateEnum() {
        if (StringUtils.isNotBlank(state)) {
            return State.valueOf(state);
        }
        return null;
    }

    public CurrentXjobState getCurrentXjobStateEnum() {
        if (StringUtils.isNotBlank(currentXjobState)) {
            return CurrentXjobState.valueOf(currentXjobState);
        }
        return null;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setStateEnum(State state) {
        this.state = state.name();
    }

    public String getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(String currentNode) {
        this.currentNode = currentNode;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public int getExecutePlanVersionID() {
        return executePlanVersionID;
    }

    public void setExecutePlanVersionID(int executePlanVersionID) {
        this.executePlanVersionID = executePlanVersionID;
    }

    public CronType getCronTypeEnum() {
        if (StringUtils.isNotBlank(cronType)) {
            return CronType.valueOf(cronType);
        }
        return null;
    }

    public String getCronType() {
        return cronType;
    }

    public void setCronType(String cronType) {
        this.cronType = cronType;
    }

    public void setCronTypeEnum(CronType cronType) {
        this.cronType = cronType.name();
    }

    public JobPlanType getJobPlanTypeEnum() {
        if (StringUtils.isNotBlank(jobPlanType)) {
            return JobPlanType.valueOf(jobPlanType);
        }
        return null;
    }

    public String getJobPlanType() {
        return jobPlanType;
    }

    public void setJobPlanType(String jobPlanType) {
        this.jobPlanType = jobPlanType;
    }

    public void setJobPlanTypeEnum(JobPlanType jobPlanType) {
        this.jobPlanType = jobPlanType.name();
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrentXjobState() {
        return currentXjobState;
    }

    public void setCurrentXjobState(String currentXjobState) {
        this.currentXjobState = currentXjobState;
    }

    public String getCurrentXjobDate() {
        return currentXjobDate;
    }

    public void setCurrentXjobDate(String currentXjobDate) {
        this.currentXjobDate = currentXjobDate;
    }

    public String getCurrentXjobDateFormater() {
        if (StringUtils.isNotBlank(this.currentXjobDate)) {
            return DateUtil.parseEnDate(this.currentXjobDate);
        }
        return null;
    }

    @Override
    public String toString() {
        return "JobExecutionPlan [id=" + id + ", executePlanVersionID=" + executePlanVersionID + ", clusterID="
               + clusterID + ", jepID=" + jepID + ", jepName=" + jepName + ", cronExpression=" + cronExpression
               + ", ignoreError=" + ignoreError + ", cronType=" + cronType + ", state=" + state + ", currentNode="
               + currentNode + ", createDate=" + createDate + "]";
    }

    public enum State {
        able("正常"), disable("禁用");

        private final String stateName;

        private State(String stateName){
            this.stateName = stateName;
        }

        public String getStateName() {
            return stateName;
        }
    }

    public enum CurrentXjobState {
        doing("运行中"), success("成功"), error("失败");

        private final String stateName;

        private CurrentXjobState(String stateName){
            this.stateName = stateName;
        }

        public String getStateName() {
            return stateName;
        }
    }
}
