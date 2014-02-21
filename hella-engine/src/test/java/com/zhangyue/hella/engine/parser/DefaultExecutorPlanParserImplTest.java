package com.zhangyue.hella.engine.parser;

import com.zhangyue.hella.engine.parser.impl.DefaultExecutionPlanParser;

import junit.framework.TestCase;

public class DefaultExecutorPlanParserImplTest extends TestCase{

    private IJobExecutionPlanParser parser;
    private static final String path="src/test/resources/job_executor_jobplan.xml";
    
    public void setUp(){
        parser=new DefaultExecutionPlanParser();
    }
    
    public void testValidateXMLByXSD(){
        boolean res=parser.validateXMLByXSD(path);
        assertEquals(true,res);
    }
}
