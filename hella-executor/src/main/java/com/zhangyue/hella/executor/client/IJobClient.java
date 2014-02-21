/*
 * Copyright 2014 ireader.com All right reserved. This software is the
 * confidential and proprietary information of ireader.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with ireader.com.
 */
package com.zhangyue.hella.executor.client;

import java.io.IOException;

/**
 * Descriptions of the class IJobClient.java's implementation：TODO described the implementation of class
 * @date 2014-1-21
 * @author scott
 */
public interface IJobClient {

    /**
     * 初始化job client
     * @throws IOException
     */
    public void initialize() throws IOException;
    /**
     * 向engine node注册当前executor node
     * @throws IOException 当网络通讯失败时抛出该异常
     */
    public void doRegister() throws IOException;
    /**
     * 关闭job client
     */
    public void close();
}
