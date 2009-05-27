#!/bin/sh
#*******************************************************************************
# Copyright (c) 2006, 2007 Wind River Systems, Inc.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html 
# 
# Contributors: 
# Martin Oberhuber - initial API and implementation 
#*******************************************************************************
#nightly build for RSE - to be executed on build.eclipse.org
#
# Usage:
#   nightly.sh [mapVersionTag] [buildType] [buildId]
# Examples:
#   nightly.sh HEAD I
#   nightly.sh HEAD S 1.0RC3
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
cd "${basebuilder}/plugins"
pdeBuild=`ls -d org.eclipse.pde.build* | sort | tail -1`
cd "${builder}"
pdeBuild="${basebuilder}/plugins/${pdeBuild}"
buildDirectory="${working}/build"
packageDirectory="${working}/package"

tag="HEAD"
if [ "$1" != "" ]; then
  tag="$1"
fi
buildType="N"
if [ "$2" != "" ]; then
  buildType="$2"
fi
mydstamp=`date +'%Y%m%d'`
mytstamp=`date +'%H%M'`
timestamp="${mydstamp}-${mytstamp}"
buildId="${buildType}${timestamp}"
if [ "$3" != "" ]; then
  buildId="$3"
fi
rm -rf "${buildDirectory}"

command="java -cp ${basebuilder}/plugins/org.eclipse.equinox.launcher.jar org.eclipse.core.launcher.Main "
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
command="$command -Dmydstamp=${mydstamp} "
command="$command -Dmytstamp=${mytstamp} "
if [ "$buildType" = "N" ]; then
  command="$command -DforceContextQualifier=${buildId} "
  command="$command -DfetchTag=HEAD "
fi
command="$command -DdoPublish=true "
#command="$command postBuild "

echo "$command"
exec $command
