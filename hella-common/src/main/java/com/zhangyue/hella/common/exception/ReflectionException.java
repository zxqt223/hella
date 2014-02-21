package com.zhangyue.hella.common.exception;

public class ReflectionException extends Exception{
  
  private static final long serialVersionUID = 1L;

  public ReflectionException(Exception e) {
    super(e);
  }

  public ReflectionException(String msg) {
    super(msg);
  }

  public ReflectionException(String msg, Exception e) {
    super(msg, e);
  }
}
