package com.zhangyue.hella.engine.web.service;

import org.apache.commons.lang.StringUtils;

import com.zhangyue.hella.common.util.JobPlanNodeState;
import com.zhangyue.hella.engine.dao.Page;
import com.zhangyue.hella.engine.dao.XjobStateDao;

public class JobStateServiceImpl implements JobStateService {

    private XjobStateDao xjobStateDao;

    protected JobStateServiceImpl(){
        
    }
    
    public void initialize(){
        xjobStateDao = new XjobStateDao();
    }
    
    public Page queryJobStates(Page page, String executorClusterID,
        String jepID, String jepName, String jobPlanNodeName,
        JobPlanNodeState[] jobPlanNodeStateList, String jobRunDateBegin,
        String jobRunDateEnd, Integer finishedPercent, String currentExecutorKey)
        throws Exception {
        StringBuffer SQL_QUERY_JOBSTATE = new StringBuffer();
        if (StringUtils.isNotBlank(executorClusterID)) {
            SQL_QUERY_JOBSTATE.append(" AND n.executorClusterID  LIKE '%"
                                      + executorClusterID + "%'");
        }
        if (StringUtils.isNotBlank(jepID)) {
            SQL_QUERY_JOBSTATE.append(" AND p.jepID = '" + jepID + "'");
        }
        if (StringUtils.isNotBlank(jepName)) {
            SQL_QUERY_JOBSTATE.append(" AND p.jepName LIKE '%" + jepName + "%'");
        }
        if (StringUtils.isNotBlank(jobPlanNodeName)) {
            SQL_QUERY_JOBSTATE.append(" AND n.name LIKE '%" + jobPlanNodeName
                                      + "%'");
        }
        if (StringUtils.isNotBlank(jobRunDateBegin)) {
            SQL_QUERY_JOBSTATE.append(" AND s.runTime >='" + jobRunDateBegin
                                      + "'");
        }
        if (StringUtils.isNotBlank(jobRunDateEnd)) {
            SQL_QUERY_JOBSTATE.append(" AND s.runTime <='" + jobRunDateEnd
                                      + "'");
        }
        if (null != jobPlanNodeStateList) {
            StringBuffer inState = new StringBuffer();
            for (JobPlanNodeState jobPlanNodeState : jobPlanNodeStateList) {
                inState.append("'" + jobPlanNodeState + "',");
            }
            inState.deleteCharAt(inState.length() - 1);
            SQL_QUERY_JOBSTATE.append(" AND s.jobPlanNodeState in("
                                      + inState.toString() + ") ");
        }
        if (StringUtils.isNotBlank(currentExecutorKey)) {
            SQL_QUERY_JOBSTATE.append(" AND s.currentExecutorKey ='"
                                      + currentExecutorKey + "'");
        }
        if (null != finishedPercent) {
            SQL_QUERY_JOBSTATE.append(" AND s.finishedPercent ="
                                      + finishedPercent);
        }

        page = xjobStateDao.findPage(page, SQL_QUERY_JOBSTATE.toString());
        return page;
    }

}
