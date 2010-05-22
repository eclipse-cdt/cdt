#!/bin/sh
#*******************************************************************************
# Copyright (c) 2005, 2009 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
# IBM Corporation - initial API and implementation
# Martin Oberhuber (Wind River) - Adapt for TM / RSE
#*******************************************************************************
# User specific environment and startup programs
umask 002

BASE_PATH=.:/bin:/usr/bin:/usr/bin/X11:/usr/local/bin:/usr/bin:/usr/X11R6/bin
LD_LIBRARY_PATH=.
BASH_ENV=$HOME/.bashrc
USERNAME=`whoami`
xhost +$HOSTNAME
DISPLAY=:0.0
CVS_RSH=ssh
ulimit -c unlimited
export CVS_RSH USERNAME BASH_ENV LD_LIBRARY_PATH DISPLAY

proc=$$

curdir=`pwd`
cd `dirname $0`
mydir=`pwd`

#notification list
recipients=

#sets skip.performance.tests Ant property
skipPerf=""

#sets skip.tests Ant property
skipTest=""

#sets sign Ant property
sign=""

tagMaps=""

#sets fetchTag="HEAD" for nightly builds if required
tag=""

#Basebuilder for Eclipse 3.2.1 maintenance
#buildProjectTags=v20060529
buildProjectTags=r321_v20060830

#updateSite property setting
updateSite=""

#flag indicating whether or not mail should be sent to indicate build has started
mail=""

#flag used to build based on changes in map files
compareMaps=""

#buildId - build name
buildId=""

#buildLabel - name parsed in php scripts <buildType>-<buildId>-<datestamp>
buildLabel=""

# tag for build contribution project containing .map files
mapVersionTag=HEAD

# directory in which to export builder projects
#builderDir=/builds/eclipsebuilder
builderDir=$mydir/../working/build

# buildtype determines whether map file tags are used as entered or are replaced with HEAD
buildType=N

# directory where to copy build
#postingDirectory=/builds/transfer/files/master/downloads/drops
postingDirectory=$mydir/../publish


# flag to indicate if test build
testBuild=""

# path to javadoc executable
javadoc=""

# value used in buildLabel and for text replacement in index.php template file
builddate=`date +%Y%m%d`
buildtime=`date +%H%M`
timestamp=$builddate$buildtime


# process command line arguments
usage="usage: $0 [-notify emailaddresses][-test][-buildDirectory directory][-buildId name][-buildLabel directory name][-tagMapFiles][-mapVersionTag tag][-builderTag tag][-bootclasspath path][-compareMaps][-skipPerf] [-skipTest][-updateSite site][-sign] M|N|I|S|R"

if [ $# -lt 1 ]
then
		 echo >&2 "$usage"
		 exit 1
fi

while [ $# -gt 0 ]
do
		 case "$1" in
		 		 -buildId) buildId="$2"; shift;;
		 		 -buildLabel) buildLabel="$2"; shift;;
		 		 -mapVersionTag) mapVersionTag="$2"; shift;;
		 		 -tagMapFiles) tagMaps="-DtagMaps=true";;
		 		 -skipPerf) skipPerf="-Dskip.performance.tests=true";;
		 		 -skipTest) skipTest="-Dskip.tests=true";;
		 		 -buildDirectory) builderDir="$2"; shift;;
		 		 -notify) recipients="$2"; shift;;
		 		 -test) postingDirectory="/builds/transfer/files/bogus/downloads/drops";testBuild="-Dtest=true";;
		 		 -builderTag) buildProjectTags="$2"; shift;;
		 		 -compareMaps) compareMaps="-DcompareMaps=true";;
		 		 -updateSite) updateSite="-DupdateSite=$2";shift;;
		 		 -sign) sign="-Dsign=true";;
		 		 -*)
		 		 		 echo >&2 $usage
		 		 		 exit 1;;
		 		 *) break;;		 # terminate while loop
		 esac
		 shift
done

# After the above the build type is left in $1.
buildType=$1

# Set default buildId and buildLabel if none explicitly set
if [ "$buildId" = "" ]
then
		 buildId=$buildType$builddate-$buildtime
fi

if [ "$buildLabel" = "" ]
then
		 buildLabel=$buildId
fi

#Set the tag to HEAD for Nightly builds
if [ "$buildType" = "N" ]
then
        tag="-DfetchTag=HEAD"
        versionQualifier="-DforceContextQualifier=$buildId"
fi

# tag for eclipseInternalBuildTools on ottcvs1
internalToolsTag=$buildProjectTags

# tag for exporting org.eclipse.releng.basebuilder
baseBuilderTag=$buildProjectTags

# tag for exporting the custom builder
customBuilderTag=$buildProjectTags

if [ -e $builderDir ]
then
		 builderDir=$builderDir$timestamp
fi

# directory where features and plugins will be compiled
buildDirectory=$builderDir/src

mkdir -p $builderDir
cd $builderDir

#check out org.eclipse.releng.basebuilder
cvs -d :pserver:anonymous@dev.eclipse.org:/cvsroot/eclipse co -r $baseBuilderTag org.eclipse.releng.basebuilder

#check out org.eclipse.rse.build
cvs -d :pserver:anonymous@dev.eclipse.org:/cvsroot/dsdp co -r $customBuilderTag org.eclipse.tm.rse/releng/org.eclipse.rse.build
if [ "$tagMaps" == "-DtagMaps=true" ]; then  
  cvs -d moberhuber@dev.eclipse.org:/cvsroot/dsdp rtag -r $customBuilderTag v$buildId org.eclipse.tm.rse/releng/org.eclipse.rse.build
fi

if [ "$HOSTNAME" == "utils" ]
then
	#Running on build.eclipse.org
	java15_home=/shared/dsdp/tm/ibm-java2-ppc64-50
	#java14_home=/shared/webtools/apps/IBMJava2-ppc64-142
	java14_home=/shared/webtools/apps/IBMJava2-ppc-142
    javadoc="-Djavadoc15=$java15_home/bin/javadoc"
    PATH=$java15_home/jre/bin:$PATH;export PATH
elif [ "$HOSTNAME" == "parser.wrs.com" ]
then
	#Running on parser
	java15_home=/opt/jdk1.5.0_06
	java14_home=/opt/j2sdk1.4.2_12
    javadoc="-Djavadoc15=$java15_home/bin/javadoc"
    PATH=$java15_home/jre/bin:$PATH;export PATH
fi

mkdir -p $postingDirectory/$buildLabel
chmod -R 755 $builderDir

#default value of the bootclasspath attribute used in ant javac calls.  
bootclasspath="$java14_home/jre/lib/rt.jar:$java14_home/jre/lib/jsse.jar"
bootclasspath_15="$java15_home/jre/lib/rt.jar"

cd $builderDir/org.eclipse.rse.build

echo buildId=$buildId >> monitor.properties 
echo timestamp=$timestamp >> monitor.properties 
echo buildLabel=$buildLabel >> monitor.properties 
echo recipients=$recipients >> monitor.properties
echo log=$postingDirectory/$buildLabel/index.php >> monitor.properties

#the base command used to run AntRunner headless
antRunner="`which java` -Xmx500m -jar ../org.eclipse.releng.basebuilder/plugins/org.eclipse.equinox.launcher.jar -Dosgi.os=linux -Dosgi.ws=gtk -Dosgi.arch=ppc -application org.eclipse.ant.core.antRunner"

#clean drop directories
#	$antRunner -buildfile eclipse/helper.xml cleanSites

echo recipients=$recipients
echo postingDirectory=$postingDirectory
echo builderTag=$buildProjectTags
echo buildDirectory=$buildDirectory
echo bootclasspath=$bootclasspath
echo bootclasspath_15=$bootclasspath_15

#full command with args
buildCommand="$antRunner -q -buildfile buildAll.xml $mail $testBuild $compareMaps -DmapVersionTag=$mapVersionTag -DpostingDirectory=$postingDirectory -Dbootclasspath=$bootclasspath -DbuildType=$buildType -D$buildType=true -DbuildId=$buildId -Dbuildid=$buildId -DbuildLabel=$buildLabel -Dtimestamp=$timestamp -DmapCvsRoot=:ext:sdimitro@dev.eclipse.org:/cvsroot/eclipse $skipPerf $skipTest $tagMaps -DJ2SE-1.5=$bootclasspath_15 -DJ2SE-1.4=$bootclasspath -DCDC-1.0/Foundation-1.0=$bootclasspath_foundation -DlogExtension=.xml $javadoc $updateSite $sign -DgenerateFeatureVersionSuffix=true -Djava15-home=$builderDir/jdk/linuxppc/ibm-java2-ppc-50/jre -listener org.eclipse.releng.build.listeners.EclipseBuildListener"

#capture command used to run the build
echo $buildCommand>command.txt

#run the build
$buildCommand
retCode=$?

if [ $retCode != 0 ]
then
        echo "Build failed (error code $retCode)."
        exit -1
fi

#clean up
rm -rf $builderDir


