package com.zhangyue.hella.engine.core.workflow;

/**
 * @Descriptions The class WorkflowException.java's implementation：工作流异常
 * @author scott
 * @date 2013-8-19 下午2:30:31
 * @version 1.0
 */
public class WorkflowException extends Exception {

    private static final long serialVersionUID = 1L;

    public WorkflowException(Exception cause){
        super(cause);
    }

    public WorkflowException(String cause){
        super(cause);
    }

    public WorkflowException(String message, Throwable cause){
        super(message, cause);
    }

}
