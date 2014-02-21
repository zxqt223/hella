package com.zhangyue.hella.engine.manager.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.exception.InitializationException;
import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.engine.dao.JobPlanSubscribeDao;
import com.zhangyue.hella.engine.dao.SqlTemplate;
import com.zhangyue.hella.engine.db.entity.JobPlanSubscribe;
import com.zhangyue.hella.engine.manager.IJobPlanSubscribeManager;
import com.zhangyue.hella.engine.metrics.AlarmMessageManager;

public class DefaultJobPlanSubscribeManager implements IJobPlanSubscribeManager {

    private static Logger LOG = LoggerFactory.getLogger(DefaultJobPlanSubscribeManager.class);
    private JobPlanSubscribeDao jobPlanSubscribeDao;

    public void initialize() throws InitializationException {
        jobPlanSubscribeDao = new JobPlanSubscribeDao();
    }

    @Override
    public void add(JobPlanSubscribe jobPlanSubscribe) throws Exception {
        jobPlanSubscribeDao.save(jobPlanSubscribe);
    }

    @Override
    public boolean deleteByID(int[] ids) {
        if (null == ids || ids.length == 0) {
            return false;
        }
        int rs = 0;
        try {
            rs = jobPlanSubscribeDao.delete(ids);
            return rs == ids.length ? true : false;
        } catch (DaoException e) {
            LOG.error("Fail to delete job plan subscribe.",e);
            return false;
        }
    }

    public List<JobPlanSubscribe> queryJobPlanSubscribe(String clusterID) throws Exception {
        StringBuffer sql = new StringBuffer(SqlTemplate.SQL_QUERY_JOBPLAN_SUBSCRIBE + " WHERE 1=1  AND state=1  AND ");
        sql.append(" clusterID='" + clusterID + "'");
        List<JobPlanSubscribe> list = null;
        try {
            list = jobPlanSubscribeDao.query(sql.toString());
        } catch (DaoException e) {
            throw new Exception(e.getMessage());
        }
        return list;
    }

    @Override
    public void sendSubscribe(String clusterID, String jobPlanNodeName, String runTime, String runInfo, int type)
        throws Exception {
        List<JobPlanSubscribe> list = null;
        try {
            list = this.queryJobPlanSubscribe(clusterID);
        } catch (DaoException e) {
            throw new Exception(e.getMessage());
        }
        if (null == list || list.isEmpty()) {
            return;
        }
        sendJobPlanSubscribeList(clusterID, jobPlanNodeName, runTime, runInfo, list, type);
    }

    private void sendJobPlanSubscribeList(String clusterID, String jobPlanNodeName, String runTime, String runInfo,
        List<JobPlanSubscribe> list, int type) {
        AlarmMessageManager alarmMessageManager = AlarmMessageManager.getAlarmMessageManager();
        for (JobPlanSubscribe jobPlanSubscribe : list) {
            /** 发送邮件 */
            if (StringUtils.isNotBlank(jobPlanSubscribe.getUserEmail())) {
                alarmMessageManager.addEmailAlarmMessage(jobPlanSubscribe.getUserEmails(), buildSubject(type),
                    "executor:" + jobPlanSubscribe.getClusterID() + ",job plan : " + jobPlanNodeName + ",run time:"
                            + runTime + " ,log:" + runInfo);
            }
            /** 发送短信 */

            if (StringUtils.isNotBlank(jobPlanSubscribe.getUserPhoneNumber())) {
                alarmMessageManager.addMobilePhoneAlarmMessage(jobPlanSubscribe.getUserPhoneNumbers(),
                    buildSubject(type), "executor:" + jobPlanSubscribe.getClusterID() + ",job plan : "
                                        + jobPlanNodeName + ",run time:" + runTime + " ,log:" + runInfo);
            }

        }

    }

    /**
     * 计算类型
     * @param type
     * @return
     */
    private String buildSubject(int type) {
        switch (type) {
            case 0:
                return "executor state:error";
            case 1:
                return "executor state:timeout,waiting for result";
            case 2:
                return "executor state:timeout,return correct";
            default:
                return "executor state:failure";
        }
    }
}
