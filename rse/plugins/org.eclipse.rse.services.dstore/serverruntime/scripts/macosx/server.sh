#!/bin/sh
#*******************************************************************************
# Copyright (c) 2005, 2008 IBM Corporation, Wind River Systems, Inc. and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
# IBM Corporation - initial API and implementation
# Martin Oberhuber (Wind River) - Fix for solaris (Bourne Shell export statement)
# Patrick Juhl - Fix for unix (Parameters for port selection)
# David McKnight (IBM) - Fix to handle timeout and clientUserID parameters
#*******************************************************************************
# Shell script to start an RSE communications server
# This script will start the datastore server listening on an available socket
serverpath=.;

CLASSPATH=.:dstore_extra_server.jar:dstore_core.jar:dstore_miners.jar:clientserver.jar:$CLASSPATH;
export serverpath CLASSPATH


port=0;
timeout=60000;
clientUserID=$USER;

if [ $# -gt 2 ]; then
  clientUserID=$3
fi 
if [ $# -gt 1 ]; then 
  timeout=$2
fi
if [ $# -gt 0 ]; then
  port=$1
fi

case x$port in
  x-h*) echo "Usage: server.sh [port] [timeout] [clientUserId]"
        exit 0
        ;;
esac

java -DA_PLUGIN_PATH=$serverpath -DDSTORE_TRACING_ON=false -Dclient.username=$clientUserID -DDSTORE_SPIRIT_ON=true org.eclipse.dstore.core.server.Server $port $timeout &

