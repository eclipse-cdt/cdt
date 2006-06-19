#!/usr/bin/perl -w

# Assumes the $CLASSPATH environment variable is set.
# Assumes password is supplied on STDIN.
# Sets return code of 0 on failure, 1 on success.

$user=`whoami`; chomp($user); 
if ($user ne "root") {
	print "The root user must run the authorization script.\n";
	exit 0;
}

$argc = @ARGV;
if ($argc < 5 || $argc > 6) {
	print("command usage:\n");
	print("auth.pl USER PATH PORT TIMEOUT TICKET [JAVA_HOME]\n");
	exit 0;
}

$user = $ARGV[0];
$plugin_path = $ARGV[1];
$port = $ARGV[2];
$timeout = $ARGV[3];
$ticket = $ARGV[4];
if ($argc == 6) {
	$java = $ARGV[5]."/bin/java";
} else {
	$java = "java";
}

$password = <STDIN>; chomp($password);
open(CHECK, "su $user -c 'perl check.pl $password'|");
$rc = <CHECK>; chomp($rc);
close(CHECK);
if ($rc > 0) {
	printf("Invalid password.\n");
	exit 0;
}
print "success\n";

$classpath = $ENV{CLASSPATH};
$server = "org.eclipse.dstore.core.server.Server";
$inner_command = "$java -cp $classpath -DA_PLUGIN_PATH=$plugin_path -DDSTORE_SPIRIT_ON=true $server $port $timeout $ticket";
$outer_command = "su $user -c '$inner_command'";
system($outer_command);
exit 1;
