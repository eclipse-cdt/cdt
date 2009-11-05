#!/usr/bin/perl
#*******************************************************************************
# Copyright (c) 2006, 2009 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
# David Dykstal (IBM) - initial API and implementation
# Martin Oberhuber (Wind River) - ongoing maintenance
#*******************************************************************************

# Build script for Remote System Explorer
# Authors: Dave Dykstal, Kushal Munir
# java and cvs have to be in the path

use warnings;
use File::Spec;

sub ask($$) {
	my ($question, $default, $message, $ans); 
	($question, $default) = @_;
	$message = "${question} (default is ${default}): "; 
	print STDERR $message;
	$ans = <STDIN>;
	chomp $ans;
	$ans = $ans ? $ans : $default;
	return $ans;
}

sub makeAbsolute($) {
	my $path = File::Spec->canonpath($_[0]);
	if (!File::Spec->file_name_is_absolute($path)) {
		$current = `pwd`;
		chomp($current);
		$path = File::Spec->catdir($current, $path);
		$path = File::Spec->canonpath($path);
	}
	return $path;
}

# $eclipse is the location of the basic PDE and plugins to compile against
# This should include the org.eclipse.pde.build project
$eclipse = "../eclipse"; 

# $basebuilder" is the location of the Eclipse Releng basebuilder
# This can also be set to ${eclipse}
$basebuilder = "../org.eclipse.releng.basebuilder";

# $builder is the location of the custom build scripts customTargets.xml and build.properties
# (i.e. the contents of org.eclipse.rse.build)
$builder = ".";

# $working is where the build is actually done, does not need to exist
$working = "../working";

# make these absolute paths
$eclipse = makeAbsolute($eclipse);
$basebuilder = makeAbsolute($basebuilder);
$builder = makeAbsolute($builder);
$working = makeAbsolute($working);
$plugins = File::Spec->catdir($basebuilder, "plugins");
$pdeBuildGlob = File::Spec->catdir($plugins, "org.eclipse.pde.build*");

# Find the base build scripts: genericTargets.xml and build.xml
@candidates = glob($pdeBuildGlob);
$n = @candidates;
if ($n == 0) {
	die("PDE Build was not found.");
}
if ($n > 1) {
	die("Too many versions of PDE Build were found.");
}
$pdeBuild = $candidates[0];

$buildDirectory = "$working/build";
$packageDirectory = "$working/package";
$publishDirectory = "$working/publish";

$tag = ask("Enter tag to fetch from CVS", "HEAD");
$buildType = ask("Enter build type (P=Personal, N=Nightly, I=Integration, S=Stable, J/M=Maintenance, K/L=Legacy)", "P");
($sec, $minute, $hour, $mday, $mon, $year) = localtime();
$mydstamp = sprintf("%4.4d%2.2d%2.2d", $year + 1900, ($mon + 1), $mday);
$mytstamp = sprintf("%2.2d%2.2d", $hour, $minute, $sec);
$timeStamp = "${mydstamp}-${mytstamp}";
$buildId = $buildType . $timeStamp;
$buildId = ask("Enter the build id", $buildType . $timeStamp);

# default value of the bootclasspath attribute used in ant javac calls.
# these pathes are valid on build.eclipse.org  
$bootclasspath = "/shared/dsdp/JDKs/win32/j2sdk1.4.2_19/jre/lib/rt.jar:/shared/dsdp/JDKs/win32/j2sdk1.4.2_19/jre/lib/jsse.jar";
$bootclasspath_15 = "/shared/common/jdk-1.5.0_16/jre/lib/rt.jar";
#$bootclasspath_16 = "$builderDir/jdk/win32_16/jdk6/jre/lib/rt.jar";
#$bootclasspath_foundation = "/shared/common/Java_ME_platform_SDK_3.0_EA/runtimes/cdc-hi/lib/rt.jar";
$bootclasspath_foundation11 = "/shared/dsdp/JDKs/win32/j9_cdc11/lib/jclFoundation11/classes.zip";

$incantation = "java -cp ${basebuilder}/plugins/org.eclipse.equinox.launcher.jar org.eclipse.core.launcher.Main ";
$incantation .= "-application org.eclipse.ant.core.antRunner ";
$incantation .= "-buildfile ${pdeBuild}/scripts/build.xml ";
$incantation .= "-DbuildDirectory=${buildDirectory} ";
$incantation .= "-DpackageDirectory=${packageDirectory} ";
$incantation .= "-DpublishDirectory=${publishDirectory} ";
$incantation .= "-Dbuilder=${builder} ";
$incantation .= "-DbaseLocation=${eclipse} ";
$incantation .= "-DbuildType=${buildType} ";
$incantation .= "-DbuildId=${buildId} ";
$incantation .= "-DmapVersionTag=${tag} ";
$incantation .= "-Dmydstamp=${mydstamp} ";
$incantation .= "-Dmytstamp=${mytstamp} ";
if ($buildType =~ "N") {
	$incantation .= "-DforceContextQualifier=${buildId} ";
	$incantation .= "-DfetchTag=HEAD ";
}
$incantation .= "-DdoPublish=true ";
$incantation .= "-Dbootclasspath=${bootclasspath} ";
$incantation .= "-DJ2SE-1.4=${bootclasspath} ";
$incantation .= "-DJ2SE-1.5=${bootclasspath_15} ";
$incantation .= "-DCDC-1.1/Foundation-1.1=${bootclasspath_foundation11} ";
#$incantation .= "postBuild ";


print("${incantation}\n");

system($incantation);