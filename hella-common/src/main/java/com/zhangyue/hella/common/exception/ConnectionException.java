package com.zhangyue.hella.common.exception;

import java.sql.SQLException;

public class ConnectionException extends SQLException{
	private static final long serialVersionUID = 1L;

	public ConnectionException(){
		super();
	}
	
	public ConnectionException(String msg){
		super(msg);
	}
	
	public ConnectionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	
	public ConnectionException(Exception e){
		super(e);
	}
}
