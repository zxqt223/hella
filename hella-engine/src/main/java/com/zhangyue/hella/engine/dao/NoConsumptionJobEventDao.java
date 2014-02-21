package com.zhangyue.hella.engine.dao;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhangyue.hella.common.exception.DaoException;
import com.zhangyue.hella.engine.db.entity.NoConsumptionJobEvent;

public class NoConsumptionJobEventDao extends BaseDao {

    private static Logger log =
            LoggerFactory.getLogger(NoConsumptionJobEventDao.class);

    public void save(NoConsumptionJobEvent noConsumptionJobEvent)
        throws DaoException {
        log.debug("SQL:"
                  + SqlTemplate.SQL_INSERT_NO_CONSUMPTION_JOB_EVENT.toString());
        try {
            super.insert(SqlTemplate.SQL_INSERT_NO_CONSUMPTION_JOB_EVENT,
                new Object[] { noConsumptionJobEvent.getName(),
                              noConsumptionJobEvent.getCreateDate() });
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage());
        } finally {
            super.closeConnection();
        }
    }

    public List<NoConsumptionJobEvent> findAll() throws DaoException {
        try {
            return super.find(NoConsumptionJobEvent.class,
                SqlTemplate.SQL_QUERY_NO_CONSUMPTION_JOB_EVENT);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage());
        } finally {
            super.closeConnection();
        }
    }

    public void delAllNoConsumptionJobEvent() throws DaoException {
        try {
            super.update(SqlTemplate.SQL_DEL_NO_CONSUMPTION_JOB_EVENT);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e.getCause());
        } finally {
            super.closeConnection();
        }
    }

}
