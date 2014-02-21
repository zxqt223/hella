# hella #
Version:2.0-SNAPSHOT

#### A distribute job scheduler system. ####

### Version Compatability ###
This code is built with the following assumptions.You may get mixed results if you deviate from these versions.
* thrift 0.9.1
* mysql
* zookeeper 3.3.4


Setup
* 1.mvn install -Dmaven.test.skip
* 2.install hella database for mysql
  
    You can find sql file at hella-engine/doc directory.

* 3.start engine node and start executor node.
