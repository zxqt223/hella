package com.zhangyue.hella.engine.metrics;

import java.util.ArrayList;
import java.util.List;

import com.zhangyue.monitor.alarm.AlarmMessage;
import com.zhangyue.monitor.metrics.MetricsContext;
/**
 * 
 * 引擎节点metrics采集器
 * @date 2014-1-9
 * @author scott
 */
public class EngineMetrics implements MetricsContext {

    private List<AlarmMessage> messages = new ArrayList<AlarmMessage>();      // 待发送的报警消息
    private AlarmMessageManager alarmMessageManager;
    
    public EngineMetrics(AlarmMessageManager alarmMessageManager){
        this.alarmMessageManager = alarmMessageManager;
    }
    
    @Override
    public void doUpdates() {
        if(!messages.isEmpty()){
            messages.clear();
        }
        if(!alarmMessageManager.isHasAlarmMessage()){
            messages.addAll(alarmMessageManager.getAlarmMessages());
            alarmMessageManager.clear();
        }
    }

    @Override
    public boolean isMetricsValueException() {
        return messages.isEmpty() ? false : true;
    }

    @Override
    public List<AlarmMessage> getAlarmMessage() {
        return messages;
    }
}
