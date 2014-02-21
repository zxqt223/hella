/* 
 * Copyright 2005 - 2009 Terracotta, Inc. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */

package com.zhangyue.hella.engine;


import org.apache.log4j.PropertyConfigurator;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Example will demonstrate all of the basics of scheduling capabilities of
 * Quartz using Cron Triggers.
 * 
 * @author Bill Kratzer
 */
public class CronTriggerExample {


    public void run() throws Exception {
        Logger log = LoggerFactory.getLogger(CronTriggerExample.class);

        log.info("------- Initializing -------------------");

        // First we must get a reference to a scheduler
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        log.info("------- Initialization Complete --------");

        log.info("------- Scheduling Jobs ----------------");

        // jobs can be scheduled before sched.start() has been called

        // job 1 will run every 20 seconds
        
//         for(int i=5;i<33;i++){
//          
//        
//        JobDetail job = newJob(SimpleJob.class)
//            .withIdentity("job1"+i, "group1"+i)
//            .build();
//        
//        CronTrigger trigger = newTrigger()
//            .withIdentity("trigger1"+i, "group1"+i)
//            .withSchedule(cronSchedule("0 */1 * * * ?"))
//            .build();
//
//        Date ft = sched.scheduleJob(job, trigger);
//        log.info(job.getKey() + " has been scheduled to run at: " + ft
//                + " and repeat based on expression: "
//                + trigger.getCronExpression());
//        } 

        log.info("------- Starting Scheduler ----------------");

 
        sched.start();
        try {
            Thread.sleep(300L * 100000L);
        } catch (Exception e) {
        }
 
        sched.shutdown(true);
    }

    public static void main(String[] args) throws Exception {
        CronTriggerExample example = new CronTriggerExample();
        example.run();
    }

}
