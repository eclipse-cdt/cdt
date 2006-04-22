#!/bin/sh
# Shell script to starat RSE communications server
# This script will start the datastore listening on an available socket
export serverpath=.;
export CLASSPATH=.:dstore_extra_server.jar:dstore_core.jar:dstore_miners.jar:clientserver.jar:$CLASSPATH;
if [ $1 ]                     
then java -DA_PLUGIN_PATH=$serverpath -DDSTORE_TRACING_ON=false -Dclient.username=$1 org.eclipse.dstore.core.server.Server 0 60000 &
else java -DA_PLUGIN_PATH=$serverpath -DDSTORE_TRACING_ON=false org.eclipse.dstore.core.server.Server 0 60000 &
fi
