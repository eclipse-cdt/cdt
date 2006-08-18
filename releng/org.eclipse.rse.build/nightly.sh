#!/bin/sh
#nightly build for RSE - to be executed on build.eclipse.org
#
# Prerequisites:
# - Eclipse 3.2 installed or linked from ../eclipse
# - org.eclipse.releng.basebuilder checked out to ../org.eclipse.releng.basebuilder
#
#author: martin oberhuber

curdir=`pwd`
cd `dirname $0`
mydir=`pwd`

# pathes: see build.rb for reference
cd "$mydir/../eclipse" ; eclipse=`pwd`
cd "$mydir/../org.eclipse.releng.basebuilder" ; basebuilder=`pwd`
cd "$mydir/../working" ; working=`pwd`
cd "$mydir/../publish" ; publishDirectory=`pwd`
cd "$mydir" ; builder=`pwd`

# Find the base build scripts: genericTargets.xml and build.xml
pdeBuild="${basebuilder}/plugins/org.eclipse.pde.build"
buildDirectory="${working}/build"
packageDirectory="${working}/package"

tag="HEAD"
buildType="N"
timestamp=`date +'%Y%m%d-%H%M'`
buildId="${buildType}${timestamp}"
rm -rf "${buildDirectory}"

command="java -cp ${basebuilder}/startup.jar org.eclipse.core.launcher.Main "
command="$command -application org.eclipse.ant.core.antRunner "
command="$command -buildfile ${pdeBuild}/scripts/build.xml "
command="$command -DbuildDirectory=${buildDirectory} "
command="$command -DpackageDirectory=${packageDirectory} "
command="$command -DpublishDirectory=${publishDirectory} "
command="$command -Dbuilder=${builder} "
command="$command -DbaseLocation=${eclipse} "
command="$command -DbuildType=${buildType} "
command="$command -DbuildId=${buildId} "
command="$command -DmapVersionTag=${tag} "
command="$command -DdoPublish=true "
command="$command -DforceContextQualifier=${buildId} "
#command="$command postBuild "

echo "$command"
exec $command
