package com.zhangyue.hella.executor.manager.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zhangyue.hella.common.protocol.JobProgress;
import com.zhangyue.hella.common.util.Constant;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.executor.client.impl.XjobRunner;
import com.zhangyue.hella.executor.manager.IProgressCollector;
import com.zhangyue.hella.executor.manager.IProgressManager;

/**
 * @Descriptions The class ProgressCollector.java's implementation：进度收集器
 * @author scott
 * @date 2013-8-19 下午3:25:52
 * @version 1.0
 */
public class DefaultProgressCollector implements IProgressCollector {

    private IProgressManager progessManager = null;
    private Map<String, JobProgress> jobProgressMap;  //需要收集的进度信息，key:事件ID，value:进度信息对象

    public DefaultProgressCollector(IProgressManager progessManager){
        this.progessManager = progessManager;
        this.jobProgressMap = new ConcurrentHashMap<String, JobProgress>();
    }

    /**
     * 根据不同规则过滤可汇报进度
     * 
     * @return
     */
    public synchronized List<JobProgress> getJobProgressList() {
        if(jobProgressMap.isEmpty()){
            return null;
        }
        
        List<JobProgress> jps = new ArrayList<JobProgress>();
        JobProgress jobProgress;
        JobPlanNodeState jobPlanNodeState;
        for (String key : jobProgressMap.keySet()) {
            jobPlanNodeState = progessManager.getJobPlanNodeState(key);
            if (null ==  jobPlanNodeState || jobPlanNodeState.isRunningState()) {  //判断作业状态是否已经结束
                continue;
            }
            jobProgress = jobProgressMap.get(key);
            jobProgress.jobPlanNodeState = progessManager.getJobPlanNodeState(key).name();
            jobProgress.progress = progessManager.getJobProgress(key);
            jobProgress.runTime = DateUtil.dateFormaterBySeconds(progessManager.getJobRunDate(key));
            jobProgress.runInfo = progessManager.getRunInfo(key);
            
            jps.add(jobProgress);
        }
        return Collections.unmodifiableList(jps);
    }

    /**
     * 注册到收集器 进行轮询收集
     * 
     * @param runXjob
     */
    public void add(XjobRunner xjobRunner) {
        JobProgress jobProgress = new JobProgress();
        jobProgress.runInfo = "";
        jobProgress.runTime = DateUtil.dateFormaterBySeconds(new Date());
        jobProgress.progress = Constant.PROGRESS_INIT;
        jobProgress.executorType = xjobRunner.getEvent().xjobExecutorType;
        jobProgress.eventID = xjobRunner.getEventID();
        
        jobProgressMap.put(xjobRunner.getEventID(), jobProgress);
    }

    /**
     * 发送进度成功后 终结状态 则移除
     * 
     * @param jps
     */
    public synchronized void clear(List<JobProgress> jps) {
        if (null == jps || jps.isEmpty()) {
            return;
        }
        for (JobProgress jp : jps) {
            // 全部完成 [成功/错误] 时进行清理
            if (!JobPlanNodeState.DISPATCH_SUCCESS.name().equals(jp.jobPlanNodeState)) {
                progessManager.removeXjobRunner(jp.eventID);
                jobProgressMap.remove(jp.eventID);
            }
        }
    }

    public void clear(String eventKey) {
        progessManager.removeXjobRunner(eventKey);
        jobProgressMap.remove(eventKey);
    }

}
