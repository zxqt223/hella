package com.zhangyue.hella.common.util;

public interface Constant {

    public static final String SERVER_HOST = "server.host";
    public static final int MS_IN_RATE = 1000;  //毫秒与秒的转换进率
    public static final int MIN_IN_RATE = MS_IN_RATE * 60;  //分钟与毫秒的转换进率
    
	/** 调度系统zookeeper中使用常量 */
    public static final String ZK_ROOT_PATH = "/hella";
	public static final String ENGINE_ZK_ROOT_PATH = ZK_ROOT_PATH+"/engine";
	public static final String ENGINE_MASTER_ZNODE_PATH = ENGINE_ZK_ROOT_PATH+"/master";
	public static final String ENGINE_SLAVE_ZNODE_PATH = ENGINE_ZK_ROOT_PATH+"/slave";
	public static final String EXECUTOR_ZK_ROOT_PATH = ZK_ROOT_PATH+"/executors";
	public static final String HELLA_ZOOKEEPER_QUORUM = "hella.zookeeper.quorum";
	public static final String HELLA_ZOOKEEPER_TIMEOUT = "hella.zookeeper.timeout";
	public static final int DEFAULT_HELLA_ZOOKEEPER_TIMEOUT = 15;  //  ZK超时时间，默认是15秒,单位：秒
	public static final int ZOOKEEPER_RECONNECT_INTERVAL = 12;  //  ZK连接重连时间，默认是12秒,单位：秒

	/** 初始版本号 */
	public static final String EMPTY_PLAN_VERSION = "000";
	
	/** xjob shell执行结果 收集 开始标志  如果有该标志则收集 */
	public static final String JOB_LOG_START = "SCHED_JOB=";

	/** xjob Max进度  成功 */
	public static final int PROGRESS_SUCCESS = 100;
	/** xjob Max进度  失败 */
	public static final int PROGRESS_ERROR = -1;
	
	/** xjob Max进度  初始化 */
	public static final int PROGRESS_INIT = 0;
	public static final int XJOB_LOG_MAX_LENGTH = 2000;

	/** Executor RPC通讯模块中的公共常量 */
	public static final int EXECUTOR_UNREGISTER = 0;  //执行节点未注册状态
	public static final int EXECUTOR_HEARTBEAT_TIMEOUT = 1;  //执行节点心跳超时
	public static final int EXECUTOR_NORMAL = 2;  //执行节点正常状态
	public static final int EXECUTE_JOB_EVENT = 0;  //执行节点正常状态
	public static final int KILL_JOB_EVENT = 1;  //执行节点正常状态
}
