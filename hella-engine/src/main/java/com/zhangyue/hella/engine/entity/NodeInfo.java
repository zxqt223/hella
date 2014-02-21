package com.zhangyue.hella.engine.entity;

import org.apache.commons.lang.StringUtils;

import com.zhangyue.hella.common.util.DateUtil;

public class NodeInfo {

    private String name;
    private String type;
    private String group;
    private String clusterID;
    private String address;

    private String createTime;

    private String state;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getClusterID() {
        return clusterID;
    }

    public void setClusterID(String clusterID) {
        this.clusterID = clusterID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreateTimeDisplay() {
        if (StringUtils.isNotBlank(createTime)) {
            return DateUtil.parseCnDate(createTime);
        }
        return null;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
