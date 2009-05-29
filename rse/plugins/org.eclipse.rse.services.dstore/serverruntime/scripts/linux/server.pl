#!/usr/bin/perl
#*******************************************************************************
# Copyright (c) 2005, 2008 IBM Corporation, Wind River Systems, Inc. and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
# IBM Corporation - initial API and implementation
# Martin Oberhuber (Wind River) - Fix bug 149129 - Perl String compare with eq
#*******************************************************************************

$port = $ARGV[0];
$timeout = $ARGV[1];
$clientUserID = $ARGV[2];

$dir= $ENV{PWD};
#print("path $dir");

#$plugins_dir=substr($dir,0,rindex($dir,"/"));
$plugins_dir=$dir;

$ENV{A_PLUGIN_PATH}="$plugins_dir/";

$oldClasspath = $ENV{CLASSPATH};


$ENV{"CLASSPATH"}="$plugins_dir:$plugins_dir/dstore_extra_server.jar:$plugins_dir/dstore_core.jar:$plugins_dir/dstore_miners.jar:$plugins_dir/clientserver.jar:$oldClasspath";

if (!defined($timeout))
{
	system("java -DA_PLUGIN_PATH=\$A_PLUGIN_PATH -DDSTORE_SPIRIT_ON=true org.eclipse.dstore.core.server.Server $port");
}
else
{
	if (!defined($clientUserID))
	{
		system("java -DA_PLUGIN_PATH=\$A_PLUGIN_PATH -DDSTORE_SPIRIT_ON=true org.eclipse.dstore.core.server.Server $port $timeout");
	}
	else
	{
		system("java -DA_PLUGIN_PATH=\$A_PLUGIN_PATH -Dclient.username=$clientUserID -DDSTORE_SPIRIT_ON=true org.eclipse.dstore.core.server.Server $port $timeout");
	}
}

$ENV{CLASSPATH}=$oldClasspath;