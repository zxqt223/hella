package com.zhangyue.hella.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.zhangyue.hella.common.exception.ReflectionException;

public class ReflectionUtil {

  public static Object newInstance(String className) throws ReflectionException {
    Constructor<?> constructor = null;
    try {
      Class<?> clazz = Class.forName(className);
      constructor = clazz.getDeclaredConstructor(new Class[] {});
      constructor.setAccessible(true);
    } catch (ClassNotFoundException e) {
      throw new ReflectionException("Fail to find class : " + className, e);
    } catch (SecurityException e) {
      throw new ReflectionException(e);
    } catch (NoSuchMethodException e) {
      throw new ReflectionException(e);
    }
    try {
      return constructor.newInstance();
    } catch (IllegalArgumentException e) {
      throw new ReflectionException(e);
    } catch (InstantiationException e) {
      throw new ReflectionException(e);
    } catch (IllegalAccessException e) {
      throw new ReflectionException(e);
    } catch (InvocationTargetException e) {
      throw new ReflectionException(e);
    }
  }
}
