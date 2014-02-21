package com.zhangyue.hella.executor.util;

/**
 * 
 * @Descriptions The class Constant.java's implementation：系统常量类
 *
 * @author scott 
 * @date 2013-8-19 下午3:26:39
 * @version 1.0
 */
public final class ExecutorConstant {

	/** 集群ID */
	public static final String CLUSTER_ID = "cluster.id";
	public static final String EXECUTOR_PROPERTIES = "executor.properties";
	public static final String JOBPLAN_VERSION = "jobplan.version";
	public static final String EXECUTOR_XJOB_PATH = "xjob/";

	/** 系统根配置路径 */
	//public static final String BASECONFPATH = "conf/";

	public static final String JOB_EXECUTOR_JOBPLAN = "job_executor_jobplan.xml";

	public static final String JOB_JOBPLAN_XSD = "job_executor_jobplan.xsd";

	/** 执行器-引擎 心跳_线程 执行周期 */
	public static final String EXECUTOR_NODE_HEARTBEAT_INTERVAL = "executor.node.heartbeat.interval";
	public static final int DEFAULT_EXECUTOR_NODE_HEARTBEAT_INTERVAL = 3; //  默认执行器心跳周期，单位：秒
	
	public static final String RPC_TIMEOUT = "rpc.timeout";
    public static final int DEFAULT_RPC_TIMEOUT = 10; //  默认RPC超时时间，单位：秒
    
	public static final String EXECUTOR_NODE_EHREAD_POOL_CAPACITY = "executor.node.thread.pool.capacity";  
	public static final int DEFAULT_EXECUTOR_NODE_EHREAD_POOL_CAPACITY = 50;  //  默认执行器节点线程池容量
}
