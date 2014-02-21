package com.zhangyue.hella.engine.core.job.event;

/**
 * @Descriptions The class Event.java's implementation： 作业事件
 * 
 * @author scott
 * @date 2013-8-19 下午2:03:30
 * @version 1.0
 */
public class Event {

    private String name;

    public Event(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Event [name=" + name + "]";
    }
}
