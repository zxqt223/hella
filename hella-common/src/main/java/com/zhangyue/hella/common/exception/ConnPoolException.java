package com.zhangyue.hella.common.exception;

/**
 * 
 * @Descriptions The class ConnPoolException.java's implementation：TODO described the implementation of class
 * @author scott 2013-8-19 上午11:06:20
 * @version 1.0
 */
public class ConnPoolException extends RuntimeException {
	private static final long serialVersionUID = -2894971157798080669L;

	public ConnPoolException() {
		super();
	}

	public ConnPoolException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ConnPoolException(String arg0) {
		super(arg0);
	}

	public ConnPoolException(Throwable arg0) {
		super(arg0);
	}
}
