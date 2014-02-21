package com.zhangyue.hella.engine.db.entity;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.engine.core.job.event.Event;

/**
 * @Descriptions The class NoConsumptionJobEvent.java's implementation：
 * @author scott
 * @date 2013-8-19 下午2:44:51
 * @version 1.0
 */
public class NoConsumptionJobEvent implements Serializable {

    private static final long serialVersionUID = -102151628615401793L;

    public static final String TABLE_NAME =
            com.zhangyue.hella.engine.util.EngineConstant.SCHED_TABLE_PREFIX
                    + "NO_CONSUMPTION_JOB_EVENT";

    public NoConsumptionJobEvent(){

    }

    public NoConsumptionJobEvent(String name){
        super();
        this.name = name;
        this.createDate = DateUtil.dateFormaterBySeconds(new Date());
    }

    private int id;
    private String name;
    private String createDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public Event getEvent() {
        return StringUtils.isNotBlank(this.getName()) ? new Event(
            this.getName()) : null;
    }

}
