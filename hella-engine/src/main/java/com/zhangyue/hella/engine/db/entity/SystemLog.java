package com.zhangyue.hella.engine.db.entity;

import java.io.Serializable;

import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.SystemLogType;

/**
 * @Descriptions The class SystemLog.java's implementation：系统日志
 * @author scott
 * @date 2013-8-19 下午2:45:39
 * @version 1.0
 */
public class SystemLog implements Serializable {

    private static final long serialVersionUID = -102151628615401793L;

    public static final String TABLE_NAME = com.zhangyue.hella.engine.util.EngineConstant.SCHED_TABLE_PREFIX + "SYSTEM_LOG";

    private int id;
    private String operatorName;
    private String ip;
    private String logType;
    private String logContent;
    private String createDate;

    public SystemLog(){

    }

    public SystemLog(String operatorName, String ip, String logType, String logContent, String createDate){
        super();
        this.operatorName = operatorName;
        this.ip = ip;
        this.logType = logType;
        this.logContent = logContent;
        this.createDate = createDate;
    }
    
    public String getLogTypeMsg() {
        SystemLogType type = SystemLogType.valueOf(this.logType);
        return null != type ? type.getLogType() : null;
    }

    public String getCreateDateFormater() {
        if (null != this.createDate && !"".equals(createDate)) {
            return DateUtil.parseCnDate(this.createDate);
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String toString() {
        return "operatorName:" + operatorName + ",ip:" + ip + ",logType:" + logType + ",createDate:" + createDate
               + ",logContent:" + logContent;
    }
}
