package com.zhangyue.hella.engine.manager.impl;

import com.zhangyue.hella.engine.manager.IJobPlanManager;

public class JobPlanManagerFactory {

    private static IJobPlanManager jobPlanManager = null;

    public synchronized static IJobPlanManager getJobPlanManager() {
        if (null == jobPlanManager) {
            jobPlanManager = new DefaultJobPlanManager();
        }
        return jobPlanManager;
    }
}
