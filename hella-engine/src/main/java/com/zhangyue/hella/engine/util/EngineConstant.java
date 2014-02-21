package com.zhangyue.hella.engine.util;

/**
 * 引擎模块中的常量定义类
 * 
 * @date 2013-8-19 下午12:27:14
 * @author scott
 * @version 1.0
 */
public interface EngineConstant {

    /** 系统数据表名称前缀 */
    public static final String SCHED_TABLE_PREFIX = "SCH_";

    public static final String RPC_PORT = "rpc.port";
    public static final String HTTP_PORT = "webserver.http.port";

    public static final int DEFAULT_HTTP_PORT = 9090;
    public static final int DEFAULT_RPC_TIMEOUT = 6000;
    public static final int DEFAULT_RPC_PORT = 9091;

    /** 系统quartz配置文件 路径 */
    public static final String QUARTZ_PROPERTIES = "quartz.properties";

    public static final String JDBC_PROPERTIES = "jdbc.properties";

    public static final String ENGINE_PROPERTIES = "engine.properties";

    /** 系统作业预警，1小时无任何作业执行，则预警 */
    public static final int JOBMONITOR_HOUR = 1;

    public static final int JOB_KILL_TIMES = 5;

    /** web系统 session存储用户信息key */
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";

    public static final int PAGE_SIZE = 10;
    public static final int IGNORE_ERROR_HOUR_AGO = 12;

    /** 系统任务配置参数 */
    public static final String JOB_MONITOR_TASK = "job.monitor.task";
    public static final boolean DEFAULT_JOB_MONITOR_TASK = false;
    public static final String JOB_MONITOR_TASK_INTERVAL = "job.monitor.task.interval";
    public static final String JOB_TIMEOUT_CHECK_TASK_INTERVAL = "job.timeout.check.task.interval";
    public static final int DEFAULT_JOB_TIMEOUT_CHECK_TASK_INTERVAL = 2; // 默认60分钟，单位：分钟
    public static final int DEFAULT_JOB_MONITOR_TASK_INTERVAL = 60; // 默认60分钟，单位：分钟
    public static final String JOB_NO_RUNNING_MAX_TIME_INTERVAL = "job.no.running.max.time.interval";
    public static final int DEFAULT_JOB_NO_RUNNING_MAX_TIME_INTERVAL = 60; // 默认60分钟，单位：分钟
    public static final String LOG_TASK_INTERVAL = "log.task.interval";
    public static final int DEFAULT_LOG_TASK_INTERVAL = 30; // 默认30秒，单位：秒
    public static final String HEARTBEAT_MAX_TIMEOUT = "heartbeat.max.timeout";
    public static final int DEFAULT_HEARTBEAT_MAX_TIMEOUT = 5; // 默认心跳超时时间5min，单位：分钟
    public static final String XJOB_TIMEOUT = "xjob.timeout";
    public static final int DEFAULT_XJOB_TIMEOUT = 360;  //默认xjob运行超时时间360min，单位：分钟
    
    public static final String EXECUTOR_DOWN_CHECK_INTERVAL = "executor.down.check.interval"; 
    public static final int DEFAULT_EXECUTOR_DOWN_CHECK_INTERVAL = 3; // 默认执行器宕机检测时间周期3min，单位：分钟
}
