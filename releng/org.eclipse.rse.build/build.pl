#!/usr/bin/perl

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

# $builder is the location of the custom build scripts customTargets.xml and build.properties
# (i.e. the contents of org.eclipse.rse.build)
$builder = ".";

# $working is where the build is actually done, does not need to exist
$working = "../working";

# make these absolute paths
$eclipse = makeAbsolute($eclipse);
$builder = makeAbsolute($builder);
$working = makeAbsolute($working);
$plugins = File::Spec->catdir($eclipse, "plugins");
$baseBuilderGlob = File::Spec->catdir($plugins, "org.eclipse.pde.build*");

# Find the base build scripts: genericTargets.xml and build.xml
@candidates = glob($baseBuilderGlob);
$n = @candidates;
if ($n == 0) {
	die("PDE Build was not found.");
}
if ($n > 1) {
	die("Too many versions of PDE Build were found.");
}
$baseBuilder = $candidates[0];

$buildDirectory = "$working/build";
$packageDirectory = "$working/package";
$publishDirectory = "$working/publish";

$tag = ask("Enter tag to fetch from CVS", "HEAD");
$buildType = ask("Enter build type (P=Personal, N=Nightly, I=Integration, S=Stable)", "P");
($sec, $minute, $hour, $mday, $mon, $year) = localtime();
$timeStamp = sprintf("%4.4d%2.2d%2.2d-%2.2d%2.2d", $year + 1900, ($mon + 1), $mday, $hour, $minute, $sec);
$buildId = $buildType . $timeStamp;
$buildId = ask("Enter the build id", $buildType . $timeStamp);

$incantation = "java -cp ${eclipse}/startup.jar org.eclipse.core.launcher.Main ";
$incantation .= "-application org.eclipse.ant.core.antRunner ";
$incantation .= "-buildfile ${baseBuilder}/scripts/build.xml ";
$incantation .= "-DbuildDirectory=${buildDirectory} ";
$incantation .= "-DpackageDirectory=${packageDirectory} ";
$incantation .= "-DpublishDirectory=${publishDirectory} ";
$incantation .= "-Dbuilder=${builder} ";
$incantation .= "-DbaseLocation=${eclipse} ";
$incantation .= "-DbuildType=${buildType} ";
$incantation .= "-DbuildId=${buildId} ";
$incantation .= "-DmapVersionTag=${tag} ";
if ($buildType =~ "N") {
	$incantation .= "-DforceContextQualifier=${buildId} ";

}

print("${incantation}\n");

system($incantation);