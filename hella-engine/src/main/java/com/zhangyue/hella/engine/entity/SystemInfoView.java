package com.zhangyue.hella.engine.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SystemInfoView implements Serializable {

    private static final long serialVersionUID = 1L;
    private Map<String, String> conf = new HashMap<String, String>();

    private ClusterInfo clusterInfo;
    private String startDate;
    private String zkAddress;
    private String memoryUsed;
    private String state;
    
    public String getStartDate() {
        return startDate;
    }

    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    
    public String getZkAddress() {
        return zkAddress;
    }

    
    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    
    public String getMemoryUsed() {
        return memoryUsed;
    }
    
    public void setMemoryUsed(String memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public Map<String, String> getConf() {
        return conf;
    }

    public void setConf(Map<String, String> conf) {
        Map<String, String> map = new HashMap<String, String>();
        for (String key : conf.keySet()) {
            map.put(key, conf.get(key));
        }
        this.conf = map;
    }

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
}
