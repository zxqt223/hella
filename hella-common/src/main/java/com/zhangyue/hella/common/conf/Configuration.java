package com.zhangyue.hella.common.conf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.util.PropertiesUtil;

/**
 * @Descriptions The class SchedConfiguration.java's
 *               implementation：系统参数配置：通过指定路径 加载Properties初始化
 * @author scott 2013-8-19 上午10:55:56
 * @version 1.0
 */
public class Configuration {

    private static Logger LOG = LoggerFactory.getLogger(Configuration.class);

    /** 系统参数 */
    private Properties props = new Properties();

    // /** 系统参数分类 key:文件名 value:Properties */
    // private Map<String, Properties> propMap = new ConcurrentHashMap<String,
    // Properties>();
    //
    // public Map<String, Properties> getPropMap() {
    // return Collections.unmodifiableMap(propMap);
    // }
    //
    // public Properties getPropMapByFileName(String fileName) {
    // return propMap.get(fileName);
    // }

    // public Map<String, String> getSysconf() {
    // return Collections.unmodifiableMap(sysconf);
    // }

    public String get(String key) {
        return props.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        String value = props.getProperty(key);
        if (null != value && !"".equals(value.trim())) {
            return value;
        }
        return defaultValue;
    }

    public int getInt(String name, int defaultValue) {
        String value = get(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOG.warn(e.getMessage());
        }
        return defaultValue;
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        String valueString = get(name);
        if (null == valueString || "".equals(valueString)) {
            return defaultValue;
        }
        return Boolean.valueOf(valueString);
    }

    public void set(String key, String value) {
        props.put(key, value);
    }

    /**
     * 获取逗号分隔的字符串数组
     * @param paramName
     * @return 逗号分隔的字符串数组
     */
    public String[] getStringArr(String paramName) {
        String value = get(paramName);
        if (null == value || "".equals(value)) {
            return null;
        }
        return value.split(",");
    }

    /**
     * 配置初始化
     * 
     * @param propFiles
     * @throws IOException
     */
    public void initialize(String[] propFiles) throws IOException {
        if (propFiles == null) {
            return;
        }
        Properties prop;
        for (String propFile : propFiles) {
            prop = PropertiesUtil.loadPropertyFile(propFile);
            if(prop != null){
                props.putAll(prop);
            }
        }
    }

    /**
     * 获取所有的配置参数
     * @return 所有参数构成的Map，如果没有参数，则返回非空map
     */
    public Map<String,String> getAllParameters(){
        Map<String,String> parameters = new HashMap<String,String>();
        Set<String> propertyNames = props.stringPropertyNames();
        for(String propertyName : propertyNames){
            parameters.put(propertyName, props.getProperty(propertyName));
        }
        return parameters;
    }
    
    private static ReentrantLock lock = new ReentrantLock();

    private static Configuration schedConfiguration = null;

    public static Configuration getInstance() {

        if (null == schedConfiguration) {
            lock.lock();
            if (null == schedConfiguration) {
                schedConfiguration = new Configuration();
            }
            lock.unlock();
        }

        return schedConfiguration;
    }

}
