#!/bin/sh
#*******************************************************************************
# Copyright (c) 2005, 2006 IBM Corporation, Wind River Systems, Inc. and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
# IBM Corporation - initial API and implementation
# Martin Oberhuber (Wind River) - Fix for solaris (Bourne Shell export statement)
# Patrick Juhl - Fix for unix (Parameters for port selection)
#*******************************************************************************
# Shell script to start an RSE communications server
# This script will start the datastore server listening on an available socket
serverpath=.;
CLASSPATH=.:dstore_extra_server.jar:dstore_core.jar:dstore_miners.jar:clientserver.jar:$CLASSPATH;
export serverpath CLASSPATH
if [ $1 ]                     
then java -DA_PLUGIN_PATH=$serverpath -DDSTORE_TRACING_ON=false -Dclient.username=$1 -DDSTORE_SPIRIT_ON=true org.eclipse.dstore.core.server.Server $1 &
else java -DA_PLUGIN_PATH=$serverpath -DDSTORE_TRACING_ON=false -DDSTORE_SPIRIT_ON=true org.eclipse.dstore.core.server.Server 0 60000 &
fi
