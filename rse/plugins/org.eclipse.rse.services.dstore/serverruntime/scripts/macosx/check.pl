#!/usr/bin/perl -w
#*******************************************************************************
# Copyright (c) 2005, 2006 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
# IBM Corporation - initial API and implementation
#*******************************************************************************

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
