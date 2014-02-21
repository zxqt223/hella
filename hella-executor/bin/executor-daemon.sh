#!/bin/bash

USG="Usage: $0  start|stop [-debug]"
if [ $# -lt 1 ] ; then
  echo $USG
  exit 1
fi

source ./executor-env.sh

CLASSPATH=.:$JAVA_HOME/lib/tools.jar
LIBPATH=$HELLA_EXECUTOR_HOME/lib

cd $HELLA_EXECUTOR_HOME

for f in `find $LIBPATH -name '*.jar'`
  do
    CLASSPATH=$CLASSPATH:$f
  done
  
CLASSPATH=$CLASSPATH:$HELLA_EXECUTOR_HOME/conf
# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 256m to higher values in case you run out of memory.  **
# ******************************************************************

OPT="$OPT -cp $CLASSPATH"
DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=10005,server=y,suspend=n"
if [ "$2" = "-debug" ] ; then
  OPT="$DEBUG $OPT"
fi 

# ***************
# ** Run...    **
# ***************

pid=$HELLA_EXECUTOR_LOG_DIR/hella-executor.pid
log=$HELLA_EXECUTOR_LOG_DIR/hella-executor.log

if [ "$1" = "start" ] ; then
    echo "start hella-executor , logging to $log"
    ENV="-Dexecutor.log.dir=$HELLA_EXECUTOR_LOG_DIR"
    exec java $ENV $OPT com.zhangyue.hella.executor.ExecutorNode 2>&1 < /dev/null & 
    echo $! > $pid
elif [ "$1" = "stop" ] ; then
    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo "stop hella-executor process ..."
        kill `cat $pid`
      else
        echo "no hella-executor to stop"
      fi
    else
      echo "no hella-executor to stop"
    fi
fi