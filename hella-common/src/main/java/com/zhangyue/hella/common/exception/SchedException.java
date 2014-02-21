package com.zhangyue.hella.common.exception;

 
public class SchedException extends Exception {
	private static final long serialVersionUID = -5365630128856068164L;
	
    public SchedException() {
    	
	super();
    }

    public SchedException( String message ) {
	super( message );
    }

    public SchedException( String message, Throwable cause ) {
        super(message, cause);
    }
 
    public SchedException(Throwable cause) {
        super(cause);
    }
    
}

