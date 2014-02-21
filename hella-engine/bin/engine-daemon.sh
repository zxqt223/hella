#!/bin/bash

USG="Usage: $0  start|stop [-debug]"
if [ $# -lt 1 ] ; then
  echo $USG
  exit 1
fi

source ./engine-env.sh

CLASSPATH=.:$JAVA_HOME/lib/tools.jar
LIBPATH=$HELLA_ENGINE_HOME/lib

cd $HELLA_ENGINE_HOME

for f in `find $LIBPATH -name '*.jar'`
  do
    CLASSPATH=$CLASSPATH:$f
  done
  
CLASSPATH=$CLASSPATH:$HELLA_ENGINE_HOME/conf
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

pid=$HELLA_ENGINE_LOG_DIR/hella-engine.pid
log=$HELLA_ENGINE_LOG_DIR/hella-engine.log

if [ "$1" = "start" ] ; then
    echo "start hella-engine , logging to $log"
    ENV="-Dengine.log.dir=$HELLA_ENGINE_LOG_DIR"
    exec java $ENV $OPT com.zhangyue.hella.engine.EngineNode 2>&1 < /dev/null & 
    echo $! > $pid
elif [ "$1" = "stop" ] ; then
    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo "stop hella-engine process ..."
        kill `cat $pid`
      else
        echo "no hella-engine to stop"
      fi
    else
      echo "no hella-engine to stop"
    fi
fi