#!/usr/bin/perl
#*******************************************************************************
# Copyright (c) 2006, 2010 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
# David Dykstal (IBM) - initial API and implementation
# Martin Oberhuber (Wind River) - ongoing maintenance
#*******************************************************************************
use warnings;

print STDERR "Which tag do you want to fetch? (default is HEAD): ";
$answer = <STDIN>;
chomp($answer);
$tag = $answer ? $answer : "HEAD";

$incantation = "cvs ";
$incantation .= '-d :pserver:anonymous:none@dev.eclipse.org:/cvsroot/tools ';
$incantation .= "checkout ";
$incantation .= "-r ${tag} ";
$incantation .= "-d builder ";
$incantation .= "org.eclipse.tm.rse/releng/org.eclipse.rse.build ";

print($incantation);
system($incantation);

print("\n");
print("Builder has been fetched and is in the builder subdirectory\n");
