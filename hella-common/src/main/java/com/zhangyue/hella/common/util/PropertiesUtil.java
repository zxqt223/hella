/*
 * Copyright 2014 ireader.com All right reserved. This software is the
 * confidential and proprietary information of ireader.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with ireader.com.
 */
package com.zhangyue.hella.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * 属性文件工具类
 * @date 2014-1-11
 * @author scott
 */
public class PropertiesUtil {

    public static Properties loadPropertyFile(String propFile) throws IOException {
        Properties prop = null;
        if(propFile == null){
            return null;
        }
        prop = new Properties();
        InputStream is = PropertiesUtil.class.getClassLoader().getResourceAsStream(propFile);
        prop.load(is);
        try {
            is.close();
        } catch (IOException e) {
        }
        return prop;
    }
}
