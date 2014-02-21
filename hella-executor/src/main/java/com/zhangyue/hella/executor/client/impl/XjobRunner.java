package com.zhangyue.hella.executor.client.impl;

import com.zhangyue.hella.common.protocol.JobEvent;
import com.zhangyue.hella.common.util.XjobMode;
import com.zhangyue.hella.common.xjob.Xjob;
import com.zhangyue.hella.executor.util.ExecutorConstant;

/**
 * @Descriptions The class RunXjob.java's implementation：进度执行线程
 * @author scott
 * @date 2013-8-19 下午3:26:17
 * @version 1.0
 */
public class XjobRunner implements Runnable {

    private JobEvent event;
    private Xjob xjob;

    public Xjob getXjob() {
        return xjob;
    }

    public JobEvent getEvent() {
        return event;
    }

    protected XjobRunner(Xjob xjob, JobEvent event){
        this.xjob = xjob;
        this.event = event;
    }

    @Override
    public void run() {
        if (null == xjob || null == event) {
            return;
        }

        /** 设置路径：把计划配置中到相对路径 修改为可以执行的绝对路径 */
        if ((XjobMode.script == XjobMode.valueOf(event.mode)) && (!event.executionContent.startsWith("/"))) {
            event.executionContent =
                    System.getProperty("user.dir") + "/" + ExecutorConstant.EXECUTOR_XJOB_PATH
                            + event.executionContent.trim();
        }
        xjob.execute(event);
    }

    public String getEventID() {
        return event.getEventID();
    }
}
