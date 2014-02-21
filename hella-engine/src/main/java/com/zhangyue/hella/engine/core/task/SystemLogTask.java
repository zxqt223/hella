package com.zhangyue.hella.engine.core.task;

import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.common.metircs.AbsMonitor;
import com.zhangyue.hella.common.util.SystemLogType;
import com.zhangyue.hella.engine.core.LogCollector;
import com.zhangyue.hella.engine.dao.SystemLogDao;
import com.zhangyue.hella.engine.db.entity.SystemLog;
import com.zhangyue.hella.engine.metrics.AlarmMessageManager;

/**
 * 系统日志异步任务，主要干两件事情：1.定时存储系统日志到数据库；2.对异常系统日志进行报警
 * 
 * @date 2013-8-19 下午2:28:32
 * @author scott
 */
public class SystemLogTask extends TimerTask {

    private SystemLogDao systemLogDao;
    private static Logger LOG = Logger.getLogger(SystemLogTask.class);

    public SystemLogTask(AbsMonitor monitor){
        systemLogDao = new SystemLogDao();
    }

    @Override
    public void run() {
        AlarmMessageManager alarmMessageManager;
        LogCollector schedCollector = LogCollector.getSchedCollector();
        List<SystemLog> systemLogList = schedCollector.getSystemLogList();
        if (null == systemLogList || systemLogList.isEmpty()) {
            return;
        }
        alarmMessageManager = AlarmMessageManager.getAlarmMessageManager();
        // 调用工具类 进行 持久化
        for (SystemLog systemLog : systemLogList) {
            try {
                systemLogDao.save(systemLog);
            } catch (DaoException e) {
                LOG.error("Fail to save system log to database!" + systemLog.toString(), e);
            }

            /** 如果为系统异常日志 则短信通知管理员 */
            if (SystemLogType.sysException.name().equals(systemLog.getLogType())) {
                alarmMessageManager.addAdminAlarmMessage(systemLog.getLogType(), systemLog.getLogContent());
            }
        }
        schedCollector.removeSystemLogList(systemLogList);
    }
}
