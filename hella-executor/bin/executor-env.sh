#!/bin/bash

#The home directory for hella executor.
export HELLA_EXECUTOR_HOME=${PWD%/*}

# The java implementation to use.  Required
#export JAVA_HOME=

#The maximum amount of heap to use, in MB. Default is 2000.
export HELLA_EXECUTOR_HEAPSIZE=2000

#JVM parameter configuration
export OPT="-Xmx${HELLA_EXECUTOR_HEAPSIZE}m -Xss256k -XX:+UseParallelGC -XX:ParallelGCThreads=20"

# Where log files are stored.  $HELLA_EXECUTOR_HOME/logs by default.
export HELLA_EXECUTOR_LOG_DIR=$HELLA_EXECUTOR_HOME/logs
