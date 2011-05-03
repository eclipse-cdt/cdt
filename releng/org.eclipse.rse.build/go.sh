#!/bin/sh
#*******************************************************************************
# Copyright (c) 2006, 2010 Wind River Systems, Inc.
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

# make pathes absolute
cd "$mydir/../eclipse" ; eclipse=`pwd`
cd "$mydir/../org.eclipse.releng.basebuilder" ; basebuilder=`pwd`
cd "$mydir/../working" ; working=`pwd`
cd "$mydir/../publish" ; publishDirectory=`pwd`
cd "$mydir/../org.eclipse.tm.releng" ; mapRoot=`pwd`
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

# default value of the bootclasspath attribute used in ant javac calls.
# these pathes are valid on build.eclipse.org  
bootclasspath_14="/shared/tools/tm/JDKs/win32/j2sdk1.4.2_19/jre/lib/rt.jar:/shared/tools/tm/JDKs/win32/j2sdk1.4.2_19/jre/lib/jsse.jar"
bootclasspath_15="/shared/common/jdk-1.5.0-22.x86_64/jre/lib/rt.jar"
#bootclasspath_16="$builderDir/jdk/win32_16/jdk6/jre/lib/rt.jar"
bootclasspath_16="/shared/tools/tm/jdk-1.6/jre/lib/rt.jar"
#bootclasspath_foundation="/shared/common/Java_ME_platform_SDK_3.0_EA/runtimes/cdc-hi/lib/rt.jar"
bootclasspath_foundation11="/shared/tools/tm/JDKs/win32/j9_cdc11/lib/jclFoundation11/classes.zip"
bootclasspath=${bootclasspath_14}

command="java -cp ${basebuilder}/plugins/org.eclipse.equinox.launcher.jar org.eclipse.core.launcher.Main "
command="$command -application org.eclipse.ant.core.antRunner "
command="$command -buildfile ${pdeBuild}/scripts/build.xml "
command="$command -DbuildDirectory=${buildDirectory} "
command="$command -DpackageDirectory=${packageDirectory} "
command="$command -DpublishDirectory=${publishDirectory} "
command="$command -Dbuilder=${builder} "
command="$command -DmapRoot=${mapRoot} "
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
#command="$command -Dbootclasspath=${bootclasspath} "
#command="$command -DJ2SE-1.4=${bootclasspath_14} "
#command="$command -DJ2SE-1.5=${bootclasspath_15} "
#command="$command -DCDC-1.1/Foundation-1.1=${bootclasspath_foundation11} "
#command="$command -DJ2SE-1.2=../jres/1.2.2/lib/rt.jar "
#command="$command postBuild "

which java
echo "$command"
exec $command
