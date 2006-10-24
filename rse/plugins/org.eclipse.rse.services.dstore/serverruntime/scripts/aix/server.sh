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
#*******************************************************************************
# Shell script to start an RSE communications server
# This script will start the datastore server listening on an available socket

serverpath=.;
PATH=/usr/java131/jre/bin:$PATH
CLASSPATH=/usr/java131/jre/lib/rt.jar:/usr/jdk_base/lib/classes.zip:$CLASSPATH
CLASSPATH=.:dstore_extra_server.jar:dstore_core.jar:dstore_miners.jar:clientserver.jar:$CLASSPATH;
export serverpath PATH CLASSPATH
java -DA_PLUGIN_PATH=$serverpath -DDSTORE_TRACING_ON=false -DDSTORE_SPIRIT_ON=true org.eclipse.dstore.core.server.Server 0 60000 &
