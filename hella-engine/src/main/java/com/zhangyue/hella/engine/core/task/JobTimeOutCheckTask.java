package com.zhangyue.hella.engine.core.task;

import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.zhangyue.hella.engine.manager.IJobStateManager;

/**
 * Description: 系统监控作业任务：4小时无任何作业执行，则预警<br>
 * Copyright: Copyright (c) 2012 <br>
 * Company: www.renren.com
 * 
 * @author zhuhui{hui.zhu@renren-inc.com} 2012-10-10
 * @version 1.0
 */
/**
 * @Descriptions The class SystemJobTimeOutTask.java's
 *               implementation：系统监控作业任务：4小时无任何作业执行，则预警
 * @author scott
 * @date 2013-8-19 下午2:27:31
 * @version 1.0
 */
public class JobTimeOutCheckTask extends TimerTask {

    private static Logger LOG = Logger.getLogger(JobTimeOutCheckTask.class);
    private IJobStateManager jobStateManager;

    public JobTimeOutCheckTask(IJobStateManager jobStateManager){
        this.jobStateManager = jobStateManager;
    }
    @Override
    public void run() {
        try {
            if(LOG.isDebugEnabled()){
                LOG.debug(" SystemJobTimeOutTask is run，aotu timeOut handle jobProgress ... ");
            }
            jobStateManager.autoTimeOutHandlejobProgress();
        } catch (Exception e) {
            LOG.error("Fail to auto handle job progress timeout,",e);
        }
    }

}
