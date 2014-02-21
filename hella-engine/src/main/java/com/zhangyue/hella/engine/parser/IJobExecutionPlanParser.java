package com.zhangyue.hella.engine.parser;

import com.zhangyue.hella.engine.db.entity.JobExecutionPlanSubmissionContext;

/**
 * 
 * @Descriptions The class ExecutorPlanParser.java's implementation：jobplan xml配置解析DTO
 * @author scott 2013-8-19 上午11:08:56
 * @version 1.0
 */
public interface IJobExecutionPlanParser {
	/**
	 * 根据XSD验证该XML是否符合规范
	 * @param xsd
	 * @param xml
	 * @return
	 */
	public boolean validateXMLByXSD(String pathXml);

	/**
	 * 解析该xml的作业计划
	 * @param jobplanFileName
	 * @return
	 */
	public JobExecutionPlanSubmissionContext getJobExecutionPlanSubmissionContext(String clusterID, String jobplanFileName) throws Exception;
}
