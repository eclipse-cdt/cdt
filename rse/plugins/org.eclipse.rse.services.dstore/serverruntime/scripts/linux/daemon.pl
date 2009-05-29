#!/usr/bin/perl -w
#*******************************************************************************
# Copyright (c) 2005, 2007 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
# IBM Corporation - initial API and implementation
#*******************************************************************************
 
$port = "4075";
$helpFlag = "-h"; 
$dir = ".";
 
if (defined($ARGV[0]))
{	
	$port = $ARGV[0];
}


$isHelp = $helpFlag cmp $port; 
if ($isHelp == 0)
{
   print("command usage:\n");
   print("daemon.linux [<port> | <low port>-<high port>] [ <low server port>-<high server port>]\n");    
 0;
}
else
{
	$user=`whoami`; chomp($user); 
	$match = $user cmp "root";

	if ($match != 0)
	{
	    print("WARNING: To run the server daemon, you must have root authority\n");
	}

	$trace="false";
	if (defined($ENV{DSTORE_TRACING_ON}))
	{
		$trace = $ENV{DSTORE_TRACING_ON};
	}
	
	    $dir= $ENV{PWD};
	    $plugins_dir=$dir;
		
	    $ENV{A_PLUGIN_PATH}="$plugins_dir/";
    
    
	    $oldClasspath = $ENV{CLASSPATH};
	    

		$ENV{CLASSPATH}="$plugins_dir:$plugins_dir/dstore_extra_server.jar:$plugins_dir/dstore_core.jar:$plugins_dir/dstore_miners.jar:$plugins_dir/clientserver.jar";
		if (defined ($oldClasspath))
		{
		  $ENV{CLASSPATH}="$ENV{CLASSPATH}:$oldClasspath";
		}

		if (defined($ARGV[1]))
		{
			system("java -DA_PLUGIN_PATH=\$A_PLUGIN_PATH -DDSTORE_TRACING_ON=$trace org.eclipse.dstore.core.server.ServerLauncher $port $ARGV[1]");
		}
		else
		{
		    system("java -DA_PLUGIN_PATH=\$A_PLUGIN_PATH -DDSTORE_TRACING_ON=$trace org.eclipse.dstore.core.server.ServerLauncher $port");
		}

}
