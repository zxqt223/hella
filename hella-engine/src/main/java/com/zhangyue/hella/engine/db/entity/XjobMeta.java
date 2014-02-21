package com.zhangyue.hella.engine.db.entity;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.zhangyue.hella.common.util.XjobMode;
import com.zhangyue.hella.engine.util.EngineConstant;

/**
 * @Descriptions The class XjobMeta.java's implementation：结点job元数据
 * @author scott
 * @date 2013-8-19 下午2:46:00
 * @version 1.0
 */
public class XjobMeta implements Serializable {

    private static final long serialVersionUID = -1021518615401793L;
    public static final String TABLE_NAME = EngineConstant.SCHED_TABLE_PREFIX
                                            + "XJOB_META";

    private int id;
    private int jobExecutionPlanID;
    private int jobPlanNodeID;
    private String mode;
    private String executeUser;
    private String executionContent;
    private String jobClassName;
    private String args;
    private String description;

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

    public XjobMode getModeEnum() {
        if (StringUtils.isNotBlank(mode)) {
            return XjobMode.valueOf(mode);
        }
        return null;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setModeEnum(XjobMode mode) {
        this.mode = mode.name();
    }

    public String getExecutionContent() {
        return executionContent;
    }

    public void setExecutionContent(String executionContent) {
        this.executionContent = executionContent;
    }

    public String getJobClassName() {
        return jobClassName;
    }

    public void setJobClassName(String jobClassName) {
        this.jobClassName = jobClassName;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getJobExecutionPlanID() {
        return jobExecutionPlanID;
    }

    public void setJobExecutionPlanID(int jobExecutionPlanID) {
        this.jobExecutionPlanID = jobExecutionPlanID;
    }

    public String getExecuteUser() {
        return executeUser;
    }

    public void setExecuteUser(String executeUser) {
        this.executeUser = executeUser;
    }
}
