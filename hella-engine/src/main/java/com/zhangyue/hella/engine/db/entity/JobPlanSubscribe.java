package com.zhangyue.hella.engine.db.entity;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * @Descriptions The class JobPlanSubscribe.java's implementation:作业计划异常订阅
 * @author scott
 * @date 2013-8-19 下午2:44:12
 * @version 1.0
 */
public class JobPlanSubscribe implements Serializable {

    private static final long serialVersionUID = -102151628615401793L;
    public static final String TABLE_NAME = com.zhangyue.hella.engine.util.EngineConstant.SCHED_TABLE_PREFIX + "JOBPLAN_SUBSCRIBE";
    private int id;
    private String clusterID;
    private String userEmail;
    private String userPhoneNumber;
    private boolean state = true;

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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String[] getUserPhoneNumbers() {
        if (StringUtils.isNotBlank(userPhoneNumber)) {
            return userPhoneNumber.split(",");
        }
        return null;
    }

    public String[] getUserEmails() {
        if (StringUtils.isNotBlank(userEmail)) {
            return userEmail.split(",");
        }
        return null;
    }

}
