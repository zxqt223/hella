namespace java com.zhangyue.hella.common.protocol

struct JobProgress {
1: required string executorType;
4: required string jobPlanNodeState; 
5: required i32 progress;
6: required string eventID;
7: optional string runInfo;
8: optional string runTime;
9: required i32 xjobStateID;
}

struct JobEvent {
1: required i32 eventType;
2: required string eventID;
3: required string executorClusterID;
4: required string xjobExecutorType;
5: required string mode;
6: required string executionContent;
7: required string jobClassName;
8: required string jobPlanNodeName;
9: optional string executeUser;
10: optional string argsValue;
}

struct HeartbeatResponse {
1:required i32 executorStatus;
2:optional list<JobEvent> jobEvents;
}

service EngineNodeProtocol {
  void doRegister(1:string clusterID, 2:i64 registerTimestamp, 3:string executorStartDate);
  HeartbeatResponse sendHeartbeat(1:string executorClusterID, 2:list<JobProgress> progressList, 3:i64 lastSeen);
  bool isEngineAlive();
}
