package com.zhangyue.hella.common.entity;

/**
 * RPC地址实体类
 * 
 * @author scott 2013-8-19 上午11:05:17
 * @version 1.0
 */
public class NodeAddress {

    private String host;
    private int port;

    public NodeAddress(String host, int port){
        this.host = host;
        this.port = port;
    }

    public NodeAddress(String address){
        super();
        if (null == address) {
            return;
        }
        String[] addressAttr = address.split(":");
        if (null == addressAttr || addressAttr.length != 2) {
            return;
        }
        host = addressAttr[0];
        port = Integer.valueOf(addressAttr[1]);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

}
