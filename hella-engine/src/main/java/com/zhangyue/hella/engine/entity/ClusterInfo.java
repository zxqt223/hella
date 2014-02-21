package com.zhangyue.hella.engine.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ClusterInfo {

    private String currentAppAddress;
    private List<NodeInfo> engineList;

    private Map<String, List<NodeInfo>> executorMap;

    public Map<String, List<NodeInfo>> getExecutorMap() {
        return executorMap;
    }

    public void setExecutorMap(Map<String, List<NodeInfo>> executorMap) {
        this.executorMap = executorMap;
    }

    public List<NodeInfo> getEngineList() {
        return engineList;
    }

    public void setEngineList(List<NodeInfo> engineList) {
        this.engineList = engineList;
    }

    public void setExecutorMap(List<NodeInfo> executorList) {
        if (null == executorMap) {
            executorMap = new TreeMap<String, List<NodeInfo>>();
        }
        for (NodeInfo nodeInfo : executorList) {
            List<NodeInfo> list = executorMap.get(nodeInfo.getClusterID());
            if (null == list) {
                list = new ArrayList<NodeInfo>();
            }
            list.add(nodeInfo);
            executorMap.put(nodeInfo.getClusterID(), list);
        }
    }

    public void addEngineList(NodeInfo nodenfo) {
        if (null == engineList) {
            engineList = new ArrayList<NodeInfo>();
        }
        engineList.add(nodenfo);
    }

    public void addExecutorMap(NodeInfo nodenfo) {
        if (null == executorMap) {
            executorMap = new HashMap<String, List<NodeInfo>>();
        }
        List<NodeInfo> list = executorMap.get(nodenfo.getClusterID());
        if (null == list) {
            list = new ArrayList<NodeInfo>();
        }
        list.add(nodenfo);
        executorMap.put(nodenfo.getClusterID(), list);
    }

    public String getCurrentAppAddress() {
        return currentAppAddress;
    }

    public void setCurrentAppAddress(String currentAppAddress) {
        this.currentAppAddress = currentAppAddress;
    }

}
