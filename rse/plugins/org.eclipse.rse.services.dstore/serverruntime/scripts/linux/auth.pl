#!/usr/bin/perl -w
#*******************************************************************************
# Copyright (c) 2005, 2010 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
# IBM Corporation - initial API and implementation
# David McKnight   (IBM)   - [254785] [dstore] RSE Server assumes home directory on target machine
# David McKnight   (IBM)   - [262013] [dstore][unix] RSE Daemon fails to start server on HP-UX
# David McKnight   (IBM)   - [270833] Unify rseserver auth.pl to not use "su -p" on any Platform
#*******************************************************************************

use Shell;

if (!defined($ARGV[0]) || !defined($ARGV[1]) || !defined($ARGV[2]) || !defined($ARGV[3]) || !defined($ARGV[4]))
{
    print("command usage:\n");
   print("auth.pl USER, PATH, PORT, TIMEOUT, TICKET\n");
}
else
{
  $userIN    = $ARGV[0];
  $pathIN    = $ARGV[1];
  $portIN    = $ARGV[2];
  $timeoutIN = $ARGV[3];
  $ticketIN  = $ARGV[4];

 if (defined($ARGV[5]))
  {
    $javaHome = $ARGV[5];
    $javaExe = "$javaHome/bin/java";
  }
  else
  {
    $javaExe = "java"
  }

  $pwdIN = <STDIN>;
  chomp($pwdIN);


   @passwdStruct = getpwnam($userIN);

  if (@passwdStruct == 0)
  {
     print("invalid user name\n");
     0;
  }
  else
  {
    $passwd=$passwdStruct[1];
    $dir=$passwdStruct[7]; # get the user's home dir
    #$passwd = $pass;
    
    $encryptedPWD = crypt($pwdIN, $passwd);
    $classpath=$ENV{CLASSPATH};
    $suOptions="-";

    if ($passwd eq $encryptedPWD)
    {
		print("success\n");

		# check for the existence of a home directory
		$homeDir=$dir;
		if (!(-e $homeDir))
	    {
	      $homeDir="/tmp/" . $userIN;
	    }	 
		
		system("su $suOptions $userIN -c '$javaExe -Duser.home=$homeDir -cp $classpath -DA_PLUGIN_PATH=$pathIN -DDSTORE_SPIRIT_ON=true org.eclipse.dstore.core.server.Server $portIN $timeoutIN $ticketIN'");
		1;
    }
    else
    {
		print("incorrect password\n");
    	0;
    }
  }
}
