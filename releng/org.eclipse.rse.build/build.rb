#!/usr/bin/ruby
# Build script for Remote System Explorer
# Author: Dave Dykstal, Kushal Munir
# Prerequisites:
# written in ruby
# java and cvs have to be in the path

require "ftools"

def ask(question, default)
	message = "#{question} (default is #{default}): " 
	STDERR.print message
	answer = readline().strip
	answer = answer.empty? ? default : answer
	return answer
end

# "eclipse" is the location of the basic PDE and plugins to compile against
# This should include the org.eclipse.pde.build project
eclipse	= "c:/work/targets/OpenRSE-1.0/eclipse" 

# "builder" is the location of the custom build scripts customTargets.xml and build.properties
# (i.e. the contents of org.eclipse.rse.build)
builder	= "."

# "working" is where the build is actually done, does not need to exist
working = "c:/temp/build"

# make these absolute paths
eclipse = File.expand_path(eclipse)
builder = File.expand_path(builder)
working = File.expand_path(working)

# Find the base build scripts: genericTargets.xml and build.xml
candidates = Dir["#{eclipse}/plugins/org.eclipse.pde.build*"]
if (candidates.size == 0) then 
	raise("PDE Build was not found.")
end
if (candidates.size > 1) then
	raise("Too many versions of PDE Build were found.")
end
baseBuilder = candidates[0]

buildDirectory = "#{working}/build"
packageDirectory = "#{working}/package"
publishDirectory = "#{working}/publish"

tag = ask("Enter tag to fetch from CVS", "HEAD")
buildType = ask("Enter build type (N=Nightly, I=Integration, M=Milestone)", "N")
buildId = ask("Enter the build id", Time.now.strftime("%Y%m%d-%H%M"))

command = "java -cp #{eclipse}/startup.jar org.eclipse.core.launcher.Main "
command += "-application org.eclipse.ant.core.antRunner "
command += "-buildfile #{baseBuilder}/scripts/build.xml "
command += "-DbuildDirectory=#{buildDirectory} "
command += "-DpackageDirectory=#{packageDirectory} "
command += "-DpublishDirectory=#{publishDirectory} "
command += "-Dbuilder=#{builder} "
command += "-DbaseLocation=#{eclipse} "
command += "-DbuildType=#{buildType} "
command += "-DbuildId=#{buildId} "
command += "-DmapVersionTag=#{tag} "

puts(command)

system(command)