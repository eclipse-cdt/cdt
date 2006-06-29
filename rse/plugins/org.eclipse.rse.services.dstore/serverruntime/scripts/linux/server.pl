#!/usr/bin/perl

$port = $ARGV[0];
$timeout = $ARGV[1];
$packaged_as = $ARGV[2];
$clientUserID = $ARGV[3];


$dir= $ENV{PWD};
#print("path $dir");

#$plugins_dir=substr($dir,0,rindex($dir,"/"));
$plugins_dir=$dir;

$ENV{A_PLUGIN_PATH}="$plugins_dir/";

$oldClasspath = $ENV{CLASSPATH};


if ($packaged_as eq "jar")
{
    $ENV{"CLASSPATH"}="$plugins_dir:$plugins_dir/dstore_extra_server.jar:$plugins_dir/dstore_core.jar:$plugins_dir/dstore_miners.jar:$plugins_dir/clientserver.jar:$oldClasspath";
}
if ($packaged_as eq "src")
{
    $ENV{"CLASSPATH"}="$plugins_dir:$oldClasspath";
}
if (!defined($packaged_as))
{
    $ENV{"CLASSPATH"}="$plugins_dir:$plugins_dir/dstore_extra_server.jar:$plugins_dir/dstore_core.jar:$plugins_dir/dstore_miners.jar:$plugins_dir/clientserver.jar:$oldClasspath";
}

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