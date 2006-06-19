#!/usr/bin/perl -w

# On MacOS X the password check must be run under the user's uid.
# If the password is OK, prints "0\n" to STDOUT.
# If not OK, prints "1\n" on STDOUT.
# Password prompt and errors are sent to the bit bucket.

$password = $ARGV[0];
$user = `whoami`; chomp($user);
$rc = system ("echo $password | su $user -c 'echo 0' 2> /dev/null");
if ($rc > 0) {
	print "1\n";
}
