#!/bin/sh
#nightly build for RSE - to be executed on build.eclipse.org
#author: martin oberhuber

curdir=`pwd`
cd `dirname $0`
mydir=`pwd`

# pathes: see build.rb for reference
cd "$mydir/../eclipse" ; eclipse=`pwd`
cd "$mydir/../working" ; working=`pwd`
cd "$mydir/../publish" ; publishDirectory=`pwd`
cd "$mydir" ; builder=`pwd`

# Find the base build scripts: genericTargets.xml and build.xml
baseBuilder="${eclipse}/plugins/org.eclipse.pde.build_3.2.0.v20060603"
buildDirectory="${working}/build"
packageDirectory="${working}/package"

tag="HEAD"
buildType="N"
timestamp=`date +'%Y%m%d-%H%M'`
buildId="${buildType}${timestamp}"
rm -rf "${buildDirectory}"

command="java -cp ${eclipse}/startup.jar org.eclipse.core.launcher.Main "
command="$command -application org.eclipse.ant.core.antRunner "
command="$command -buildfile ${baseBuilder}/scripts/build.xml "
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
