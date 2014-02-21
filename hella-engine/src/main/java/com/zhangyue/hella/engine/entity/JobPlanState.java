package com.zhangyue.hella.engine.entity;

import java.io.Serializable;

/**
 * @Descriptions The class JobPlanStateDTO.java's implementation：JobPlanStateDTO
 *               用于联合查询 或者 页面显示 所需数据封装
 * @author scott
 * @date 2013-8-19 下午2:41:39
 * @version 1.0
 */
public class JobPlanState implements Serializable {

    private static final long serialVersionUID = 1L;
    private String clusterID;
    private String jepID;
    private String jepName;
    private String currentXjobDate;
    private String currentXjobState;

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

    public String getCurrentXjobDate() {
        return currentXjobDate;
    }

    public void setCurrentXjobDate(String currentXjobDate) {
        this.currentXjobDate = currentXjobDate;
    }

    public String getCurrentXjobState() {
        return currentXjobState;
    }

    public void setCurrentXjobState(String currentXjobState) {
        this.currentXjobState = currentXjobState;
    }

}
