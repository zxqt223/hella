package com.zhangyue.hella.engine.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.zhangyue.hella.engine.db.entity.SystemLog;

/**
 * 系统运行数据收集器:短信、邮件、日志
 * 
 * @author scott 2013-8-19 下午1:27:43
 * @version 1.0
 */
public class LogCollector {

    private static LogCollector schedCollector = null;
    private static int SYTEM_LOG_QUEUE_MAX_COUNT = 100;
    private List<SystemLog> systemLogList = Collections.synchronizedList(new ArrayList<SystemLog>());

    public void clearAll() {
        systemLogList.clear();
    }

    /** SysLogDTO */
    public void addSystemLog(SystemLog systemLog) {
        if (systemLogList.size() > SYTEM_LOG_QUEUE_MAX_COUNT) {
            systemLogList.clear();
        }
        systemLogList.add(systemLog);
    }

    public List<SystemLog> getSystemLogList() {
        return systemLogList;
    }

    public void removeSystemLogList(List<SystemLog> list) {
        systemLogList.removeAll(list);
    }

    private LogCollector(){
    }

    public static synchronized LogCollector getSchedCollector() {
        if (schedCollector == null) {
            schedCollector = new LogCollector();
        }
        return schedCollector;
    }

}
