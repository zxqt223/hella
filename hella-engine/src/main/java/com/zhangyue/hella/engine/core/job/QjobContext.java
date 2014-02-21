package com.zhangyue.hella.engine.core.job;

/**
 * 
 * @Descriptions The class QjobContext.java's implementation：QJOb上下文定义类
 * @author scott 2013-8-19 下午1:30:07
 * @version 1.0
 */
public class QjobContext {

	private String clusterID;
	private int jobExecutionPlanID;
	private int jobPlanVersion;
	private String jepID;
	private String jepName;

	public QjobContext() {

	}

	public QjobContext(String clusterID, int jobExecutionPlanID, String jepID, String jepName, int jobPlanVersion) {
		super();
		this.clusterID = clusterID;
		this.jobExecutionPlanID = jobExecutionPlanID;
		this.jepID = jepID;
		this.jepName = jepName;
		this.jobPlanVersion = jobPlanVersion;
	}

	public String getClusterID() {
		return clusterID;
	}

	public void setClusterID(String clusterID) {
		this.clusterID = clusterID;
	}

	public int getJobExecutionPlanID() {
		return jobExecutionPlanID;
	}

	public void setJobExecutionPlanID(int jobExecutionPlanID) {
		this.jobExecutionPlanID = jobExecutionPlanID;
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

	public int getJobPlanVersion() {
		return jobPlanVersion;
	}

	public void setJobPlanVersion(int jobPlanVersion) {
		this.jobPlanVersion = jobPlanVersion;
	}

  @Override
  public String toString() {
    return "QjobContext [clusterID=" + clusterID + ", jobExecutionPlanID=" + jobExecutionPlanID + ", jobPlanVersion=" + jobPlanVersion + ", jepID=" + jepID
        + ", jepName=" + jepName + "]";
  }

	 
}
