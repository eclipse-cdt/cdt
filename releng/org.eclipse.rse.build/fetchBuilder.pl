#!/usr/bin/perl
use warnings;

print STDERR "Which tag do you want to fetch? (default is HEAD): ";
$answer = <STDIN>;
chomp($answer);
$tag = $answer ? $answer : "HEAD";

$incantation = "cvs ";
$incantation .= '-d :pserver:anonymous:none@dev.eclipse.org:/cvsroot/dsdp ';
$incantation .= "checkout ";
$incantation .= "-r ${tag} ";
$incantation .= "-d builder ";
$incantation .= "org.eclipse.tm.rse/releng/org.eclipse.rse.build ";

print($incantation);
system($incantation);

print("\n");
print("Builder has been fetched and is in the builder subdirectory\n");
