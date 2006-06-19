#!/usr/bin/perl -w

# Arguments:
# ARGV[0] is the port number for the daemon to listen on. Default is 4035.
# ARGV[1] is the directory that contains RSE Dstore server jar files and 
# installation supplied miner class files. The default is the working 
# directory.
#
# Environment Variables:
# CLASSPATH - used to form the tail of the classpath for the daemon and server execution.
# DSTORE_TRACING_ON - 0 means to not trace, 1 means to trace.
# DSTORE_DEBUG_ON - 0 means to start in normal mode, 1 means to start in debug mode.
#
# Results:
# Returns 1 if there is a startup error of some sort.
# Does not return if the daemon starts successfully. Terminate the
# daemon with a signal such as TERM or INT.

$port = $ARGV[0] || "4035";
$plugin_dir = $ARGV[1] || $ENV{PWD};
$trace = $ENV{DSTORE_TRACING_ON} || "0";
$debug = $ENV{DSTORE_DEBUG_ON} || "0";

$user=`whoami`; chomp($user); 
if ($user ne "root") {
	print "The root user must run the RSE DStore server daemon.\n";
	exit 1;
}

if ($debug) {
	$debug_options = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000";
} else {
	$debug_options = "";
}

$classpath_old = $ENV{CLASSPATH};
$classpath = "$plugin_dir";
$classpath .= ":$plugin_dir/dstore_extra_server.jar";
$classpath .= ":$plugin_dir/dstore_core.jar";
$classpath .= ":$plugin_dir/dstore_miners.jar";
$classpath .= ":$plugin_dir/clientserver.jar";
if ($classpath_old) {
	$classpath .= ":$classpath_old";
}


$ENV{CLASSPATH} = $classpath;
$launcher = "org.eclipse.dstore.core.server.ServerLauncher";
$command = "java $debug_options -DA_PLUGIN_PATH=$plugin_dir -DDSTORE_TRACING_ON=$trace $launcher $port";
print "$command\n";
system($command);
