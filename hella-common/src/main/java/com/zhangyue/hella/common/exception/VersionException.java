package com.zhangyue.hella.common.exception;

import java.io.IOException;

public class VersionException extends IOException {
	private static final long serialVersionUID = -5365630128856068164L;
	
    public VersionException() {
    	
	super();
    }

    public VersionException( String message ) {
	super( message );
    }

    public VersionException( String message, Throwable cause ) {
        super(message, cause);
    }
 
    public VersionException(Throwable cause) {
        super(cause);
    }
    
}

