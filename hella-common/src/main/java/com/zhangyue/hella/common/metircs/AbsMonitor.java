/*
 * Copyright 2014 ireader.com All right reserved. This software is the
 * confidential and proprietary information of ireader.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with ireader.com.
 */
package com.zhangyue.hella.common.metircs;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.exception.InitializationException;
import com.zhangyue.monitor.AlarmManager;
import com.zhangyue.monitor.MetricsManager;
import com.zhangyue.monitor.Monitor;
import com.zhangyue.monitor.alarm.impl.EmailAlarm;
import com.zhangyue.monitor.alarm.impl.MobilePhoneAlarm;
import com.zhangyue.monitor.exception.HandlerConstructException;
import com.zhangyue.monitor.exception.MailException;
import com.zhangyue.monitor.metrics.MetricsContext;

/**
 * Descriptions of the class EngineMonitor.java's implementation：TODO described
 * the implementation of class
 * 
 * @date 2014-1-9
 * @author scott
 */
public abstract class AbsMonitor {

    private final static Logger LOG = Logger.getLogger(AbsMonitor.class);
    
    private boolean isMobilePhoneWarning;
    private boolean isEmailWarning;
    private MetricsManager metricsManager = null;
    private AlarmManager alarmManager = null;
    
    protected abstract List<MetricsContext> getMetricsContext();
    
    public void initialize(Configuration conf) throws InitializationException{
        String adminPhoneNumbers = conf.get("administrator.warning.phone.numbers");
        String adminEmails = conf.get("administrator.warning.emails");
        isMobilePhoneWarning = StringUtils.isNotBlank(adminPhoneNumbers) ? true : false;
        isEmailWarning = StringUtils.isNotBlank(adminEmails) ? true : false;
        
        if (!isMobilePhoneWarning && !isEmailWarning) {
            return ;
        }
        metricsManager = new MetricsManager();
        alarmManager = new AlarmManager();
        if (isMobilePhoneWarning) {
            try {
                alarmManager.addAlarm(new MobilePhoneAlarm(adminPhoneNumbers, true));
            } catch (HandlerConstructException e) {
                throw new InitializationException("Fail to build MobilePhoneAlarm instance!",e);
            }
            LOG.info("Add mobile phone warning to hella.");
        }
        if (isEmailWarning) {
            try {
                alarmManager.addAlarm(new EmailAlarm(adminEmails, true));
            } catch (HandlerConstructException e) {
                throw new InitializationException("Fail to build EmailAlarm instance!",e);
            } catch (MailException e) {
                throw new InitializationException("Fail to build EmailAlarm instance!",e);
            }
            LOG.info("Add Email warning to hella.");
        }
    }
    
    public void start() throws IOException{
        if (!isMobilePhoneWarning && !isEmailWarning) {
            LOG.warn("It does not start monitor,because it does not config warning info.");
            return ;
        }
        List<MetricsContext> metrics = getMetricsContext();
        if(null == metrics || metrics.isEmpty()){
            LOG.warn("There is no avaliable metrics.It does not support warning to hella.");
            return ;
        }
        for(MetricsContext metric : metrics){
            metricsManager.addMetricsContext(metric);
        }
        try {
            Monitor.getMonitor().initialize(metricsManager, alarmManager);
        } catch (Exception e) {
            throw new IOException("Fail to initialize monitor!" ,e);
        }
    }
    public boolean judgeMobilePhoneWarning(){
        return this.isMobilePhoneWarning;
    }
    
    public boolean judgeEmailWarning(){
        return this.isEmailWarning;
    }
    
    public void close(){
        if(null != metricsManager){
            metricsManager.close();
        }
        if(null != alarmManager){
            alarmManager.close();
        }
    }
}
