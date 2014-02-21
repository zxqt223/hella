package com.zhangyue.hella.common.exception;

import java.io.IOException;

 
public class InitializationException extends IOException {
	private static final long serialVersionUID = -5365630128856068164L;
	
    public InitializationException() {
    	
	super();
    }
 
    public InitializationException( String message ) {
	super( message );
    }

    public InitializationException( String message, Throwable cause ) {
        super(message, cause);
    }
 
    public InitializationException(Throwable cause) {
        super(cause);
    }
    
}

