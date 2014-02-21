package com.zhangyue.hella.engine.db.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 作业执行计划提交的上下文
 * @date 2014-1-7
 * @author scott
 */
public class JobExecutionPlanSubmissionContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private String clusterId;
    private int jobPlanVersion;
    private List<JobExecutionPlan> jobExecutionPlans = new ArrayList<JobExecutionPlan>();

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public int getJobPlanVersion() {
        return jobPlanVersion;
    }

    public void setJobPlanVersion(int jobPlanVersion) {
        this.jobPlanVersion = jobPlanVersion;
    }

    public List<JobExecutionPlan> getJobExecutionPlans() {
        return jobExecutionPlans;
    }

    public void addJobExecutionPlan(JobExecutionPlan jobExecutionPlan) {
        jobExecutionPlans.add(jobExecutionPlan);
    }
}
