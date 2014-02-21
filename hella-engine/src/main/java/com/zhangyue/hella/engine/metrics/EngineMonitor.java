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
import com.zhangyue.hella.common.exception.InitializationException;
import com.zhangyue.hella.common.metircs.AbsMonitor;
import com.zhangyue.monitor.metrics.MetricsContext;
import com.zhangyue.monitor.metrics.impl.DiskMetrics;
import com.zhangyue.monitor.metrics.impl.JVMMetrics;

/**
 * Descriptions of the class EngineMonitor.java's implementation：TODO described
 * the implementation of class
 * 
 * @date 2014-1-9
 * @author scott
 */
public class EngineMonitor extends AbsMonitor {

    private List<MetricsContext> metrics;

    public void initialize(Configuration conf) throws InitializationException {
        super.initialize(conf);
      //初始化报警信息管理器
        AlarmMessageManager alarmMessageManager = AlarmMessageManager.getAlarmMessageManager();
        alarmMessageManager.initialize(this, conf);
        
        metrics = new ArrayList<MetricsContext>();
        String[] diskDirs = conf.getStringArr("disk.monitor.dirs");

        if (null != diskDirs) {
            metrics.add(new DiskMetrics(diskDirs));
        }

        if (conf.getBoolean("engine.metrics.start", false)) {
            metrics.add(new EngineMetrics(alarmMessageManager));
        }
        
        if (conf.getBoolean("jvm.metrics.start", false)) {
            metrics.add(new JVMMetrics());
        }
        
    }

    /*
     * (non-Javadoc)
     * @see com.zhangyue.hella.common.metircs.AbsMonitor#getMetricsContext()
     */
    @Override
    protected List<MetricsContext> getMetricsContext() {
        return metrics;
    }
}
