#!/bin/sh
# Shell script to start an RSE communications server
# This script will start the datastore listening on an available socket

export serverpath=.;
export PATH=/usr/java131/jre/bin:$PATH
export CLASSPATH=/usr/java131/jre/lib/rt.jar:/usr/jdk_base/lib/classes.zip:$CLASSPATH
export CLASSPATH=.:dstore_extra_server.jar:dstore_core.jar:dstore_miners.jar:clientserver.jar:$CLASSPATH;
java -DA_PLUGIN_PATH=$serverpath -DDSTORE_TRACING_ON=false com.ibm.etools.systems.dstore.core.server.Server 0 60000 &