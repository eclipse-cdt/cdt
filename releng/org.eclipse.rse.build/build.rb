#!/usr/bin/ruby
# Build script for Remote System Explorer
# Author: Dave Dykstal, Kushal Munir
# Prerequisites:
# java and CVS have to be in the path

require "ftools"

# "eclipse" is the location of the basic PDE and plugins to compile against
# "builder" is the location of the build scripts (i.e. the contents of org.eclipse.rse.build)
# "working" is where the build is actually done, does not need to exist
eclipse	= "c:/work/targets/OpenRSE-1.0/eclipse" 
builder	= "." 
working = "c:/temp/build"

command = "java -cp #{eclipse}/startup.jar org.eclipse.core.launcher.Main "
command += "-application org.eclipse.ant.core.antRunner "
command += "-buildfile #{builder}/build.xml "
command += "-DbuildDirectory=#{working}/build "
command += "-DpackageDirectory=#{working}/package "
command += "-DpublishDirectory=#{working}/publish "
command += "-Dbuilder=#{builder} "
command += "-DbaseLocation=#{eclipse} "
command += "-Dbld_do_extract=yes "
command += "-Dbld_do_build=yes "
command += "-Dbld_do_package=yes "
command += "-Dbld_do_publish=yes "

puts(command)

system(command)