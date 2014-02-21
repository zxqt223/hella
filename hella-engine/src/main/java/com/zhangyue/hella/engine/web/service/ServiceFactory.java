/*
 * Copyright 2014 ireader.com All right reserved. This software is the
 * confidential and proprietary information of ireader.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with ireader.com.
 */
package com.zhangyue.hella.engine.web.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Descriptions of the class ServiceFactory.java's implementation：TODO described the implementation of class
 * @date 2014-1-14
 * @author scott
 */
public class ServiceFactory {

    private static Map<String, Object> servicesMap = new ConcurrentHashMap<String, Object>();
    /**
     * 获取实现服务接口的实例
     * @param interfaceClass  接口class对象
     * @return 服务实现实例，如果输入为空，则返回空，如果输入不识别的接口，则直接抛出运行时异常
     */
    @SuppressWarnings("unchecked")
    public static <T> T getServiceInstance(Class<T> interfaceClass){
        if(null == interfaceClass){
            return null;
        }
        String className = interfaceClass.getName();
        if(className.equals(JobPlanService.class.getName())){
            if(servicesMap.containsKey(className)){
                return (T)servicesMap.get(className);
            }
            JobPlanService jobPlanService = new JobPlanServiceImpl();
            servicesMap.put(className, jobPlanService);
            return (T)jobPlanService;
        }else if(className.equals(SysService.class.getName())){
            if(servicesMap.containsKey(className)){
                return (T)servicesMap.get(className);
            }
            SysService sysService = new SysServiceImpl();
            servicesMap.put(className, sysService);
            return (T)sysService;
        }else if(className.equals(JobStateService.class.getName())){
            if(servicesMap.containsKey(className)){
                return (T)servicesMap.get(className);
            }
            JobStateService jobStateService = new JobStateServiceImpl();
            servicesMap.put(className, jobStateService);
            return (T)jobStateService;
        }else{
            throw new RuntimeException("It doesn't support this service.serviceClassName:"+className);
        }
    }

}
