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
# go.sh -- build script like nightly build but for testing stuff without
# fetch in a local workspace
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
mydstamp=`date +'%Y%m%d'`
mytstamp=`date +'%H%M'`
timestamp="${mydstamp}-${mytstamp}"
buildId="${buildType}${timestamp}"
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
command="$command -DdoPublish=true "
command="$command -DforceContextQualifier=${buildId} "
command="$command -DfetchTag=HEAD "
command="$command -DskipFetch "
command="$command -Dmydstamp=${mydstamp} "
command="$command -Dmytstamp=${mytstamp} "
#command="$command -DJ2SE-1.2=../jres/1.2.2/lib/rt.jar "
#command="$command postBuild "

echo "$command"
exec $command
