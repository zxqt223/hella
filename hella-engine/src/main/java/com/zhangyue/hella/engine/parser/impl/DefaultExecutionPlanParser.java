package com.zhangyue.hella.engine.parser.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.zhangyue.hella.common.exception.VersionException;
import com.zhangyue.hella.common.util.DateUtil;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlan;
import com.zhangyue.hella.engine.db.entity.JobExecutionPlanSubmissionContext;
import com.zhangyue.hella.engine.db.entity.JobPlanNode;
import com.zhangyue.hella.engine.db.entity.XjobMeta;
import com.zhangyue.hella.engine.db.entity.JobPlanNode.DelayType;
import com.zhangyue.hella.engine.parser.IJobExecutionPlanParser;

/**
 * jobplan xml配置文件解析器
 * @author scott 2013-8-19 上午11:08:19
 * @version 1.0
 */
public class DefaultExecutionPlanParser implements IJobExecutionPlanParser {

    private static Logger LOG = LoggerFactory.getLogger(DefaultExecutionPlanParser.class);
    private static final String XML_SCHEMA="http://www.w3.org/2001/XMLSchema";

    public boolean validateXMLByXSD(String pathXml) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XML_SCHEMA);

        // 利用schema工厂，接收验证文档文件对象生成Schema对象
        try {
            URL url = DefaultExecutionPlanParser.class.getClassLoader().getResource("job_executor_jobplan.xsd");
            Schema schema = schemaFactory.newSchema(url);

            // 通过Schema产生针对于此Schema的验证器，利用schenaFile进行验证
            Validator validator = schema.newValidator();

            // 得到验证的数据源
            Source source = new StreamSource(new File(pathXml));
            validator.validate(source);

            return true;
        } catch (Exception e) {
            LOG.error("Fail to validate xml by xsd file.fileName:" + pathXml, e);
            return false;
        }
    }

    @Override
    public JobExecutionPlanSubmissionContext getJobExecutionPlanSubmissionContext(String clusterID,
        String jobplanFileName) throws IOException {
        Document document = getDocument(jobplanFileName);
        Element root = document.getDocumentElement();
        NodeList jobPlanNodeList = root.getChildNodes();

        String createDate = DateUtil.dateFormaterBySeconds(new Date());
        int clusterNum = jobPlanNodeList.getLength();
        Map<String, String> temp = new HashMap<String, String>();

        JobExecutionPlanSubmissionContext jepSubmissionContext = new JobExecutionPlanSubmissionContext();
        jepSubmissionContext.setClusterId(clusterID);
        jepSubmissionContext.setJobPlanVersion(0);

        for (int i = 0; i < clusterNum; i++) {
            Node jobPlanNode = jobPlanNodeList.item(i);
            if (jobPlanNode.getNodeType() == Node.ELEMENT_NODE && jobPlanNode.getNodeName().equals("jobPlan")) {
                buildExecutionJobPlan(jepSubmissionContext, jobPlanNode, temp, createDate);
            }
        }
        temp.clear();

        return jepSubmissionContext;
    }

    private Document getDocument(String jobplanFileName) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        Document document = null;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new File(jobplanFileName));
            return document;
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    private void buildExecutionJobPlan(JobExecutionPlanSubmissionContext jepSubmissionContext, Node jobPlanNode,
        Map<String, String> temp, String createDate) throws IOException {
        String nodeName;
        boolean ignoreError = getBooleanProperty(jobPlanNode, "ignoreError", true);
        String jepID = getProperty(jobPlanNode, "id");
        String jepName = getProperty(jobPlanNode, "name");
        String type = getProperty(jobPlanNode, "type");
        String cronType = getProperty(jobPlanNode, "cronType");
        String cronExpression = getProperty(jobPlanNode, "cronExpression");
        String event = getProperty(jobPlanNode, "event");
        String defaultExecutorClusterID = jepSubmissionContext.getClusterId();

        if (temp.containsKey(jepID)) {
            throw new VersionException("this jepID " + jepID + "is repeat.");
        } else if (!"".equals(jepID)) {
            temp.put(jepID, jepName);
        }
        if ("".equals(cronType)) {
            throw new IOException("It does't find available cron type.job execution plan:" + jepName);
        }

        JobExecutionPlan jobExecutionPlan = new JobExecutionPlan();
        jobExecutionPlan.setClusterID(defaultExecutorClusterID);
        jobExecutionPlan.setCreateDate(createDate);
        jobExecutionPlan.setJobPlanType(type);
        jobExecutionPlan.setJepID(jepID);
        jobExecutionPlan.setJepName(jepName);
        jobExecutionPlan.setCronType(cronType);
        jobExecutionPlan.setCronExpression(cronExpression);
        jobExecutionPlan.setEvent(event);
        jobExecutionPlan.setIgnoreError(ignoreError);

        // 解析节点
        for (Node jobNode = jobPlanNode.getFirstChild(); jobNode != null; jobNode = jobNode.getNextSibling()) {
            nodeName = jobNode.getNodeName();
            if (nodeName.equals("start")) { // 处理开始节点
                buildStartNode(jobNode, jobExecutionPlan, defaultExecutorClusterID);
                continue;
            }
            if (nodeName.equals("fork")) { // 处理分支节点
                buildForkNode(jobNode, jobExecutionPlan, defaultExecutorClusterID);
                continue;
            }

            if (nodeName.equals("join")) { // 处理合并节点
                buildJoinNode(jobNode, jobExecutionPlan, defaultExecutorClusterID);
                continue;
            }
            if (nodeName.equals("action")) { // 处理作业节点
                buildActionNode(jobNode, jobExecutionPlan, defaultExecutorClusterID);
                continue;
            }

            if (nodeName.equals("fail")) { // 处理失败纠错节点
                buildFailNode(jobNode, jobExecutionPlan, defaultExecutorClusterID);
                continue;
            }

            if (nodeName.equals("end")) { // 处理结束节点
                buildEndNode(jobNode, jobExecutionPlan, defaultExecutorClusterID);
                continue;
            }
        }
        jepSubmissionContext.addJobExecutionPlan(jobExecutionPlan);
    }

    /**
     * 获取字符串类型的属性值
     * 
     * @param node 节点对象
     * @param property 属性名称
     * @return
     */
    private String getProperty(Node node, String property) {
        Node tmpNode = node.getAttributes().getNamedItem(property);
        return (null == tmpNode) ? "" : tmpNode.getNodeValue().trim();
    }
    /**
     * 获取字符串类型的属性值
     * 
     * @param node 节点对象
     * @param property 属性名称
     * @param defaultValue 默认值
     * @return
     */
    private String getProperty(Node node, String property, String defaultValue) {
        Node tmpNode = node.getAttributes().getNamedItem(property);
        String value = (null == tmpNode) ? "" : tmpNode.getNodeValue().trim();
        //需要考虑节点的值本身就是双引号的空值
        return "".equals(value) ? defaultValue : value;
    }
    /**
     * 获取布尔型的属性值
     * 
     * @param node 节点对象
     * @param property 属性名称
     * @param defaultValue 默认值
     * @return
     */
    private boolean getBooleanProperty(Node node, String property, boolean defaultValue) {
        Node tmpNode = node.getAttributes().getNamedItem(property);
        return (null == tmpNode) ? defaultValue : Boolean.valueOf(tmpNode.getNodeValue().trim());
    }

    /**
     * 获取整数型的属性值
     * 
     * @param node 节点对象
     * @param property 属性名称
     * @param defaultValue 默认值
     * @return
     */
    private int getIntegerProperty(Node node, String property, int defaultValue) {
        Node tmpNode = node.getAttributes().getNamedItem(property);
        return (null == tmpNode) ? defaultValue : Integer.valueOf(tmpNode.getNodeValue().trim());
    }

    private void buildForkNode(Node jobNode, JobExecutionPlan jobExecutionPlan, String defaultExecutorClusterID) {
        int flag = 0;
        String name = jobNode.getAttributes().getNamedItem("name").getNodeValue().trim();
        String joinName = jobNode.getAttributes().getNamedItem("joinName").getNodeValue().trim();
        NodeList forkProperties = jobNode.getChildNodes();
        StringBuffer forkName = new StringBuffer();
        JobPlanNode jobPlanNodeTemp = new JobPlanNode();
        // 遍历所有的path节点
        for (int m = 0; m < forkProperties.getLength(); m++) {
            Node forkPropertie = forkProperties.item(m);
            if (forkPropertie.getNodeType() == Node.ELEMENT_NODE && forkPropertie.getNodeName().equals("path")) {
                if (flag == 0) { // 判断是否为第一个元素，如果是第一个元素修改标识，否则添加逗号分隔符
                    flag = 1;
                } else {
                    forkName.append(",");
                }
                forkName.append(forkPropertie.getAttributes().getNamedItem("to").getNodeValue().trim());
            }
        }
        jobPlanNodeTemp.setExecutorClusterID(defaultExecutorClusterID);
        jobPlanNodeTemp.setToNode(forkName.toString());
        jobPlanNodeTemp.setJoinName(joinName);
        jobPlanNodeTemp.setName(name);
        jobPlanNodeTemp.setType("fork");

        jobExecutionPlan.addJobPlanNode(jobPlanNodeTemp);
    }

    private void buildStartNode(Node jobNode, JobExecutionPlan jobExecutionPlan, String defaultExecutorClusterID) {
        JobPlanNode jobPlanNodeTemp = new JobPlanNode();
        String toNode = jobNode.getAttributes().getNamedItem("to").getNodeValue().trim();
        jobPlanNodeTemp.setToNode(toNode);
        jobPlanNodeTemp.setType("start");
        jobPlanNodeTemp.setName("start");
        jobPlanNodeTemp.setExecutorClusterID(defaultExecutorClusterID);

        jobExecutionPlan.addJobPlanNode(jobPlanNodeTemp);
    }

    private void buildJoinNode(Node jobNode, JobExecutionPlan jobExecutionPlan, String defaultExecutorClusterID) {
        JobPlanNode jobPlanNodeTemp = new JobPlanNode();
        String name = jobNode.getAttributes().getNamedItem("name").getNodeValue().trim();
        String to = jobNode.getAttributes().getNamedItem("to").getNodeValue().trim();
        String forkName = jobNode.getAttributes().getNamedItem("forkName").getNodeValue().trim();

        jobPlanNodeTemp.setForkName(forkName);
        jobPlanNodeTemp.setName(name);
        jobPlanNodeTemp.setType("join");
        jobPlanNodeTemp.setToNode(to);
        jobPlanNodeTemp.setExecutorClusterID(defaultExecutorClusterID);

        jobExecutionPlan.addJobPlanNode(jobPlanNodeTemp);
    }

    private void buildActionNode(Node jobNode, JobExecutionPlan jobExecutionPlan, String defaultExecutorClusterID) {
        XjobMeta xjobMeta;  //作业元数据临时变量
        String nodeName;    //节点名称临时变量
        String name = getProperty(jobNode, "name");
        String delayType = getProperty(jobNode, "delayType");
        int delayTime = getIntegerProperty(jobNode,"delayTime",0);
        int errorMaxRedoTimes = getIntegerProperty(jobNode,"errorMaxRedoTimes",0);
        int errorRedoPeriod = getIntegerProperty(jobNode,"errorRedoPeriod",0);
        String executorClusterID=getProperty(jobNode, "executorClusterID", defaultExecutorClusterID);
        
        JobPlanNode jobPlanNodeTemp = new JobPlanNode();
        if (StringUtils.isNotBlank(delayType)) {  //设置作业延迟类型，将字符串转化成枚举类型
            jobPlanNodeTemp.setDelayTypeEnum(DelayType.valueOf(delayType));
        }
        jobPlanNodeTemp.setExecutorClusterID(executorClusterID);
        jobPlanNodeTemp.setDelayTime(delayTime);
        jobPlanNodeTemp.setDelayType(delayType);
        jobPlanNodeTemp.setName(name);
        jobPlanNodeTemp.setType("action");
        jobPlanNodeTemp.setErrorMaxRedoTimes(errorMaxRedoTimes);
        jobPlanNodeTemp.setErrorRedoPeriod(errorRedoPeriod);

        NodeList xjobProperties = jobNode.getChildNodes();
        // 解析jobType,description
        for (int j = 0; j < xjobProperties.getLength(); j++) {
            Node properties = xjobProperties.item(j);
            nodeName = properties.getNodeName();
            if (properties.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (nodeName.equals("job")) {
                xjobMeta = getXjobMeta(properties);
                jobPlanNodeTemp.setXjobMeta(xjobMeta);
                continue;
            }
            if (nodeName.equals("ok")) {
                jobPlanNodeTemp.setOkNode(properties.getAttributes().getNamedItem("to").getNodeValue().trim());
                continue;
            }
            if (nodeName.equals("error")) {
                jobPlanNodeTemp.setErrorNode(properties.getAttributes().getNamedItem("to").getNodeValue().trim());
                continue;
            }
        }
        jobExecutionPlan.addJobPlanNode(jobPlanNodeTemp);
    }

    private void buildFailNode(Node jobNode, JobExecutionPlan jobExecutionPlan, String defaultExecutorClusterID) {
        String executorClusterID=getProperty(jobNode, "executorClusterID", defaultExecutorClusterID);
        String name = getProperty(jobNode, "name");
        NodeList xjobProperties = jobNode.getChildNodes();

        JobPlanNode jobPlanNodeTemp = new JobPlanNode();
        jobPlanNodeTemp.setName(name);
        jobPlanNodeTemp.setType("fail");
        jobPlanNodeTemp.setExecutorClusterID(executorClusterID);

        for (int j = 0; j < xjobProperties.getLength(); j++) {
            Node properties = xjobProperties.item(j);
            if (properties.getNodeType() == Node.ELEMENT_NODE && properties.getNodeName().equals("job")) {
                jobPlanNodeTemp.setXjobMeta(getXjobMeta(properties));
            }
        }
        jobExecutionPlan.addJobPlanNode(jobPlanNodeTemp);
    }

    private void buildEndNode(Node jobNode, JobExecutionPlan jobExecutionPlan, String defaultExecutorClusterID) {
        String executorClusterID=getProperty(jobNode, "executorClusterID", defaultExecutorClusterID);
        String name = getProperty(jobNode, "name");
        NodeList endProperties = jobNode.getChildNodes();

        JobPlanNode jobPlanNodeTemp = new JobPlanNode();
        jobPlanNodeTemp.setName(name);
        jobPlanNodeTemp.setType("end");
        jobPlanNodeTemp.setExecutorClusterID(executorClusterID);
        
        // 解析jobType,description
        for (int j = 0; j < endProperties.getLength(); j++) {
            Node properties = endProperties.item(j);
            if (properties.getNodeType() == Node.ELEMENT_NODE && properties.getNodeName().equals("job")) {
                jobPlanNodeTemp.setXjobMeta(getXjobMeta(properties));
            }
        }
        jobExecutionPlan.addJobPlanNode(jobPlanNodeTemp);
    }

    /***
     * 解析出xjob的元数据信息
     * 
     * @param properties xjob节点
     * @return
     */
    private XjobMeta getXjobMeta(Node properties) {
        String executeUser = getProperty(properties, "executeUser");
        String mode = getProperty(properties, "mode");
        NodeList jobProperties;
        String nodeName;

        XjobMeta xjobMeta = new XjobMeta();
        xjobMeta.setExecuteUser(executeUser);
        xjobMeta.setMode(mode);

        jobProperties = properties.getChildNodes();
        for (int k = 0; k < jobProperties.getLength(); k++) {
            Node jobPropertie = jobProperties.item(k);
            nodeName = jobPropertie.getNodeName();

            if (jobPropertie.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (nodeName.equals("executionContent")) {
                xjobMeta.setExecutionContent(jobPropertie.getTextContent());
                continue;
            }
            if (nodeName.equals("jobClassName")) {
                xjobMeta.setJobClassName(jobPropertie.getTextContent());
                continue;
            }
            if (nodeName.equals("args")) {
                xjobMeta.setArgs(jobPropertie.getTextContent());
                continue;
            }
            if (nodeName.equals("description")) {
                xjobMeta.setDescription(jobPropertie.getTextContent());
                continue;
            }
        }
        return xjobMeta;
    }
}
