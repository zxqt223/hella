/*
 * Copyright 2014 ireader.com All right reserved. This software is the
 * confidential and proprietary information of ireader.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with ireader.com.
 */
package com.zhangyue.hella.engine.metrics;

import java.util.ArrayList;
import java.util.List;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.metircs.AbsMonitor;
import com.zhangyue.monitor.alarm.AlarmMessage;

/**
 * 报警信息管理器，主要作用：1.引擎节点的metric调用该管理器获取报警信息； 2.各个应用点调用该管理器发送报警信息
 * 
 * @date 2014-1-9
 * @author scott
 */
public class AlarmMessageManager {

    private static AlarmMessageManager alarmMessageManager = null;
    private static AbsMonitor monitor;
    String[] adminEmails;
    String[] adminMobilePhones;
    private List<AlarmMessage> messages = new ArrayList<AlarmMessage>(); // 新产生的报警消息

    private AlarmMessageManager(){
    }

    public void initialize(AbsMonitor monitor,Configuration conf){
        AlarmMessageManager.monitor = monitor;
        adminEmails = conf.getStringArr("administrator.warning.emails");
        adminMobilePhones = conf.getStringArr("administrator.warning.phone.numbers");
    }
    
    public static AlarmMessageManager getAlarmMessageManager() {
        if (null == alarmMessageManager) {
            synchronized (AlarmMessageManager.class) {
                if (alarmMessageManager == null) {
                    alarmMessageManager = new AlarmMessageManager();
                }
            }
        }
        return alarmMessageManager;
    }

    public void addEmailAlarmMessage(String[] emailAddresses, String subject, String content) {
        if(!monitor.judgeEmailWarning()){
            return ;
        }
        if(emailAddresses == null){
            emailAddresses=adminEmails;
        }
        this.messages.add(new AlarmMessage(null, emailAddresses, subject, content));
    }

    public void addMobilePhoneAlarmMessage(String[] mobilePhoneNumbers, String title, String content) {
        if(!monitor.judgeMobilePhoneWarning()){
            return ;
        }
        if(mobilePhoneNumbers==null){
            mobilePhoneNumbers=adminMobilePhones;
        }
        this.messages.add(new AlarmMessage(mobilePhoneNumbers, null, title, content));
    }
    
    public void addAdminAlarmMessage(String exceptionType, String msg){
        addEmailAlarmMessage(null, exceptionType, msg);
        addMobilePhoneAlarmMessage(null,exceptionType, msg);
    }
    
    public List<AlarmMessage> getAlarmMessages() {
        return messages;
    }

    public boolean isHasAlarmMessage() {
        return !messages.isEmpty();
    }

    public void clear() {
        messages.clear();
    }
}
