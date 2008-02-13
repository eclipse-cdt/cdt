#!perl.exe

use strict;
use warnings;
use IO::Socket;

#####################################################################
# Copyright (c) 2004-2005 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
# 
# Contributors:
#     Bjorn Freeman-Benson - initial API and implementation
#####################################################################
#
# This test is designed to run on Windows:
#
# cd c:\eclipse\workspace\org.eclipse.debug.examples.core
# perl pdavm\tests\vmtests.pl
#
# If the tests fail, they often indicate that by hanging in an
# infinite loop. Additionally, the vm under test often becomes
# a 100% CPU usage zombie. Use the task manager to kill them.
#
my $socket1;
my $socket2;

sub expect_output {
	my $expect = shift;
	my $line = <PROGRAM_OUTPUT>;
	chomp($line);
	return if( $line eq $expect );
	die "expected output: $expect\nSaw output: $line";
}
sub expect_output_eof {
	my $line = <PROGRAM_OUTPUT>;
	return if( !defined $line );
	die "expected: EOF on output";
}
sub send_command {
	my $string = shift;
	my $expect = shift;
	$expect = "ok" if( !defined $expect );
	#print STDERR "SEND: $string\n";
	print $socket1 "$string\n";
	my $result = <$socket1>;
	chomp($result);
	#print STDERR "RESULT: $result\n";
	die "sent: $string\nexpected: $expect\nsaw:      $result" if( !($result eq $expect) );
}
sub expect_event {
	my $string = shift;
	my $event = <$socket2>;
	chomp($event);
	#print STDERR "EVENT: $event\n";
	die "expected event: $string\nsaw event: $event" if( !($string eq $event) );
}
sub setup_sockets {
	#print STDERR "calling socket 12345\n";
	$socket1 = IO::Socket::INET->new(
			Proto    => "tcp",
			PeerAddr => "localhost",
			PeerPort => "12345",
			Timeout  => 10,
		    )
		  or die "cannot connect to debug socket 12345";
	#print STDERR "calling socket 12346\n";
	$socket2 = IO::Socket::INET->new(
			Proto    => "tcp",
			PeerAddr => "localhost",
			PeerPort => "12346",
			Timeout  => 10,
		    )
		  or die "cannot connect to debug socket 12346";
	#print STDERR "done calling sockets\n";
}

sub test2 {
	print "test2 (common debug commands)..\n";
	
	my $kidpid;
	die "can't fork: $!" unless defined($kidpid = fork());
	if( $kidpid ) {	
		#print STDERR "starting program\n";
		open PROGRAM_OUTPUT, "perl pdavm/pda.pl pdavm/tests/vmtest2.pda -debug 12345 12346 |";
		#print STDERR "done starting program\n";
		expect_output("-debug 12345 12346");
		expect_output("debug connection accepted");
		expect_output("10");
		expect_output_eof();
		exit 0;
	} else {
		setup_sockets();
		expect_event("started");
		# test step
		send_command("step");
		expect_event("resumed step");
		expect_event("suspended step");
		# test breakpoint
		send_command("set 4");
		send_command("data", "6|");
		send_command("resume");
		expect_event("resumed client");
		expect_event("suspended breakpoint 4");
		# test data stack
		send_command("data", "6|7|8|9|");
		send_command("popdata");
		send_command("data", "6|7|8|");
		send_command("pushdata 11");
		send_command("data", "6|7|8|11|");
		send_command("setdata 1 2");
		send_command("data", "6|2|8|11|");
		# test call stack
		send_command("set 12");
		send_command("set 19");
		send_command("stepreturn");
		expect_event("resumed client");
		expect_event("suspended breakpoint 12");
		send_command("clear 19");
		send_command("stack", "pdavm\\tests\\vmtest2.pda|6|main#pdavm\\tests\\vmtest2.pda|18|sub1|m|n#pdavm\\tests\\vmtest2.pda|12|sub2" );
		send_command("stepreturn");
		expect_event("resumed client");
		expect_event("suspended step");
		send_command("stack", "pdavm\\tests\\vmtest2.pda|6|main#pdavm\\tests\\vmtest2.pda|18|sub1|m|n#pdavm\\tests\\vmtest2.pda|13|sub2" );
		send_command("stepreturn");
		expect_event("resumed client");
		expect_event("suspended step");
		send_command("stack", "pdavm\\tests\\vmtest2.pda|6|main#pdavm\\tests\\vmtest2.pda|22|sub1|m|n" );
		send_command("set 6");
		send_command("stepreturn");
		expect_event("resumed client");
		expect_event("suspended breakpoint 6");
		# test set and clear
		send_command("set 27");
		send_command("set 29");
		send_command("set 33");
		send_command("resume");
		expect_event("resumed client");
		expect_event("suspended breakpoint 33");
		send_command("resume");
		expect_event("resumed client");
		expect_event("suspended breakpoint 27");
		send_command("clear 33");
		send_command("resume");
		expect_event("resumed client");
		expect_event("suspended breakpoint 29");
		# test var and setvar
		send_command("set 47");
		send_command("resume");
		expect_event("resumed client");
		expect_event("suspended breakpoint 47");
		send_command("var 1 b", "4");
		send_command("var 2 b", "2");
		send_command("var 1 a", "0");
		send_command("setvar 1 a 99");
		send_command("data", "6|2|8|11|27|1|4|");
		send_command("step");
		expect_event("resumed step");
		expect_event("suspended step");
		send_command("var 1 a", "99");
		send_command("step");
		expect_event("resumed step");
		expect_event("suspended step");
		send_command("data", "6|2|8|11|27|1|4|99|");
		# test exit
		send_command("exit");
		expect_event("terminated");
	}
	#print STDERR "waiting for child\n";
	wait();
	#print STDERR "child joined\n";
	close PROGRAM_OUTPUT;
	print "test2..SUCCESS\n";
}

sub test3 {
	print "test3 (uncaught events)..\n";
	
	my $kidpid;
	die "can't fork: $!" unless defined($kidpid = fork());
	if( $kidpid ) {	
		#print STDERR "starting program\n";
		open PROGRAM_OUTPUT, "perl pdavm\\pda.pl pdavm\\tests\\vmtest3.pda -debug 12345 12346 |";
		#print STDERR "done starting program\n";
		expect_output("-debug 12345 12346");
		expect_output("debug connection accepted");
		expect_output("10");
		expect_output_eof();
		exit 0;
	} else {
		setup_sockets();
		expect_event("started");
		send_command("resume");
		expect_event("resumed client");
		expect_event("unimplemented instruction foobar");
		expect_event("no such label zippy");
		expect_event("terminated");
	}
	#print STDERR "waiting for child\n";
	wait();
	#print STDERR "child joined\n";
	close PROGRAM_OUTPUT;
	print "test3..SUCCESS\n";
}
sub test4 {
	print "test4 (caught events)..\n";
	
	my $kidpid;
	die "can't fork: $!" unless defined($kidpid = fork());
	if( $kidpid ) {	
		#print STDERR "starting program\n";
		open PROGRAM_OUTPUT, "perl pdavm\\pda.pl pdavm\\tests\\vmtest3.pda -debug 12345 12346 |";
		#print STDERR "done starting program\n";
		expect_output("-debug 12345 12346");
		expect_output("debug connection accepted");
		expect_output("10");
		expect_output_eof();
		exit 0;
	} else {
		setup_sockets();
		expect_event("started");
		send_command("eventstop unimpinstr 1");
		send_command("resume");
		expect_event("resumed client");
		expect_event("unimplemented instruction foobar");
		expect_event("suspended event unimpinstr");
		send_command("eventstop unimpinstr 0");
		send_command("resume");
		expect_event("resumed client");
		expect_event("unimplemented instruction foobar");
		expect_event("no such label zippy");
		expect_event("terminated");
	}
	#print STDERR "waiting for child\n";
	wait();
	#print STDERR "child joined\n";
	close PROGRAM_OUTPUT;
	print "test4..SUCCESS\n";
}
sub test5 {
	print "test5 (caught events)..\n";
	
	my $kidpid;
	die "can't fork: $!" unless defined($kidpid = fork());
	if( $kidpid ) {	
		#print STDERR "starting program\n";
		open PROGRAM_OUTPUT, "perl pdavm\\pda.pl pdavm\\tests\\vmtest3.pda -debug 12345 12346 |";
		#print STDERR "done starting program\n";
		expect_output("-debug 12345 12346");
		expect_output("debug connection accepted");
		expect_output("10");
		expect_output_eof();
		exit 0;
	} else {
		setup_sockets();
		expect_event("started");
		send_command("eventstop nosuchlabel 1");
		send_command("resume");
		expect_event("resumed client");
		expect_event("unimplemented instruction foobar");
		expect_event("no such label zippy");
		expect_event("suspended event nosuchlabel");
		send_command("eventstop nosuchlabel 0");
		send_command("resume");
		expect_event("resumed client");
		expect_event("no such label zippy");
		expect_event("terminated");
	}
	#print STDERR "waiting for child\n";
	wait();
	#print STDERR "child joined\n";
	close PROGRAM_OUTPUT;
	print "test5..SUCCESS\n";
}
sub test6 {
	print "test6 (watch points)..\n";
	
	my $kidpid;
	die "can't fork: $!" unless defined($kidpid = fork());
	if( $kidpid ) {	
		#print STDERR "starting program\n";
		open PROGRAM_OUTPUT, "perl pdavm\\pda.pl pdavm\\tests\\vmtest6.pda -debug 12345 12346 |";
		#print STDERR "done starting program\n";
		expect_output("-debug 12345 12346");
		expect_output("debug connection accepted");
		expect_output("8");
		expect_output_eof();
		exit 0;
	} else {
		setup_sockets();
		expect_event("started");
		send_command("watch inner::a 1");
		send_command("watch main::a 2");
		send_command("resume");
		expect_event("resumed client");
		expect_event("suspended watch write main::a");
		send_command("stack", "pdavm\\tests\\vmtest6.pda|4|main|a|b");
		send_command("resume");
		expect_event("resumed client");
		expect_event("suspended watch read inner::a");
		send_command("stack", "pdavm\\tests\\vmtest6.pda|10|main|a|b#pdavm\\tests\\vmtest6.pda|25|inner|a|c");
		send_command("watch inner::a 0");
		send_command("resume");
		expect_event("resumed client");
		expect_event("terminated");
	}
	#print STDERR "waiting for child\n";
	wait();
	#print STDERR "child joined\n";
	close PROGRAM_OUTPUT;
	print "test6..SUCCESS\n";
}
sub test7 {
	print "test7 (eval)..\n";
	
	my $kidpid;
	die "can't fork: $!" unless defined($kidpid = fork());
	if( $kidpid ) {	
		#print STDERR "starting program\n";
		open PROGRAM_OUTPUT, "perl pdavm\\pda.pl pdavm\\tests\\vmtest6.pda -debug 12345 12346 |";
		#print STDERR "done starting program\n";
		expect_output("-debug 12345 12346");
		expect_output("debug connection accepted");
		expect_output("8");
		expect_output_eof();
		exit 0;
	} else {
		setup_sockets();
		expect_event("started");
		send_command("set 25");
		send_command("resume");
		expect_event("resumed client");
		expect_event("suspended breakpoint 25");
		#
		send_command("eval push%204|push%205|add");
		expect_event("resumed client");
		expect_event("evalresult 9");
		expect_event("suspended eval");
		#
		send_command("step");
		expect_event("resumed step");
		expect_event("suspended step");
		send_command("stack", "pdavm\\tests\\vmtest6.pda|10|main|a|b#pdavm\\tests\\vmtest6.pda|26|inner|a|c");
 		send_command("data", "4|4|");
 		send_command("eval call%20other");
		expect_event("resumed client");
		expect_event("evalresult 15");
		expect_event("suspended eval");
		send_command("stack", "pdavm\\tests\\vmtest6.pda|10|main|a|b#pdavm\\tests\\vmtest6.pda|26|inner|a|c");
 		send_command("data", "4|4|");
		send_command("resume");
		expect_event("resumed client");
		expect_event("terminated");
	}
	#print STDERR "waiting for child\n";
	wait();
	#print STDERR "child joined\n";
	close PROGRAM_OUTPUT;
	print "test7..SUCCESS\n";
}
sub test1 {
	print "test1 (normal run mode)..\n";
	open PROGRAM_OUTPUT, "perl pdavm/pda.pl samples/example.pda |" or die $!;
	expect_output("\"hello\"");
	expect_output("\"barfoo\"");
	expect_output("\"first\"");
	expect_output("\"second\"");
	expect_output("12");
	expect_output("11");
	expect_output("10");
	expect_output("\"barfoo\"");
	expect_output("\"first\"");
	expect_output("\"second\"");
	expect_output("\"end\"");
	expect_output_eof();
	print "test1..SUCCESS\n";
}
sub test8 {
	print "test8 (drop to frame)..\n";
	
	my $kidpid;
	die "can't fork: $!" unless defined($kidpid = fork());
	if( $kidpid ) {	
		#print STDERR "starting program\n";
		open PROGRAM_OUTPUT, "perl pdavm\\pda.pl pdavm\\tests\\vmtest8.pda -debug 12345 12346 |";
		#print STDERR "done starting program\n";
		expect_output("-debug 12345 12346");
		expect_output("debug connection accepted");
		expect_output("1");
		expect_output_eof();
		exit 0;
	} else {
		setup_sockets();
		expect_event("started");
		send_command("step");
		expect_event("resumed step");
		expect_event("suspended step");
		send_command("step");
		expect_event("resumed step");
		expect_event("suspended step");
		send_command("step");
		expect_event("resumed step");
		expect_event("suspended step");
		send_command("step");
		expect_event("resumed step");
		expect_event("suspended step");
		send_command("step");
		expect_event("resumed step");
		expect_event("suspended step");
		send_command("step");
		expect_event("resumed step");
		expect_event("suspended step");
		send_command("step");
		expect_event("resumed step");
		expect_event("suspended step");
		send_command("stack", "pdavm\\tests\\vmtest8.pda|2|main|a#pdavm\\tests\\vmtest8.pda|8|inner|b#pdavm\\tests\\vmtest8.pda|12|inner2|c");
		send_command("drop");
		expect_event("suspended drop");
		send_command("stack", "pdavm\\tests\\vmtest8.pda|2|main|a#pdavm\\tests\\vmtest8.pda|7|inner|b");
		send_command("step");
		expect_event("resumed step");
		expect_event("suspended step");
		send_command("stack", "pdavm\\tests\\vmtest8.pda|2|main|a#pdavm\\tests\\vmtest8.pda|8|inner|b#pdavm\\tests\\vmtest8.pda|10|inner2");
		send_command("resume");
		expect_event("resumed client");
		expect_event("terminated");
	}
	#print STDERR "waiting for child\n";
	wait();
	#print STDERR "child joined\n";
	close PROGRAM_OUTPUT;
	print "test8..SUCCESS\n";
}

#
# Run the tests
#
test1();
test2();
test3();
test4();
test5();
test6();
test7();
test8();
print "All tests complete\n";
