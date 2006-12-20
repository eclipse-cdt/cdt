#!/bin/sh
#*******************************************************************************
# Copyright (c) 2006 Wind River Systems, Inc.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html 
# 
# Contributors: 
# Martin Oberhuber - initial API and implementation 
#*******************************************************************************
#:#
#:# Bootstrapping script to perform S-builds and R-builds on build.eclipse.org
#:# Will build based on HEAD of all mapfiles, and update the testUpdates as well
#:#
#:# Usage:
#:#    doit_irsbuild.sh {buildType} [buildId]
#:# Examples:
#:#    doit_irsbuild.sh R 1.0
#:#    doit_irsbuild.sh S 1.0M5
#:#    doit_irsbuild.sh I

#nothing we do should be hidden from the world
umask 22

curdir=`pwd`
cd `dirname $0`
mydir=`pwd`
echo ${mydir}

#Use Java5 on build.eclipse.org
#export PATH=/shared/common/ibm-java2-ppc64-50/bin:$PATH
#export PATH=/shared/webtools/apps/IBMJava2-ppc64-142/bin:$PATH
#export PATH=/shared/webtools/apps/IBMJava2-ppc-142/bin:$PATH
export PATH=${HOME}/ws/IBMJava2-ppc-142/bin:$PATH

#Get parameters
buildType=$1
buildId=$2
case x$buildType in
  xP|xN|xI|xS|xR|xM) ok=1 ;;
  *) ok=0 ;;
esac
if [ $ok != 1 ]; then
  grep '^#:#' $0 | grep -v grep | sed -e 's,^#:#,,'
  cd ${curdir}
  exit 0
fi

#Remove old logs and builds
echo "Removing old logs and builds..."
cd $HOME/ws
#rm log-*.txt
if [ -d working/build ]; then
  rm -rf working/build
fi
if [ -d working/package ]; then
  rm -rf working/package
fi

#Do the main job
echo "Updating builder from CVS..."
cd org.eclipse.rse.build
stamp=`date +'%Y%m%d-%H%M'`
log=$HOME/ws/log-${buildType}$stamp.txt
touch $log
cvs -q update -RPd >> $log 2>&1
daystamp=`date +'%Y%m%d*%H'`

echo "Running the builder..."
./nightly.sh HEAD ${buildType} ${buildId} >> $log 2>&1
tail -50 $log

#update the main download and archive pages
cd /home/data/httpd/download.eclipse.org/dsdp/tm/downloads
cvs -q update -RPd >> $log 2>&1
cd /home/data/httpd/archive.eclipse.org/dsdp/tm/downloads
cvs -q update -RPd >> $log 2>&1

#Fixup permissions and group id on download.eclpse.org (just to be safe)
#chmod -R g+w $HOME/ws/publish/${buildType}*${daystamp}*

#Check the publishing
cd $HOME/ws/publish
cd ${buildType}*${daystamp}*
FILES=`ls RSE-SDK-*.zip 2>/dev/null`
echo "FILES=$FILES"
if [ -f package.count -a "$FILES" != "" ]; then
  echo "package.count found, release seems ok"
  if [ ${buildType} = S -o ${buildType} = R ]; then
    #hide the release for now until it is tested
    #mirrors will still pick it up
    mv package.count package.count.orig
    #DO_SIGN=1
  fi
  
  if [ "$DO_SIGN" = "1" ]; then
    #sign the zipfiles
    ${mydir}/batch_sign.sh `pwd`
  fi

  #update the doc server
  rm -f ../N.latest/RSE-SDK-*.zip
  cp -f RSE-SDK-*.zip ../N.latest/RSE-SDK-latest.zip
  #chmod g+w ../N.latest/RSE-SDK-latest.zip
  #chgrp dsdp-tmadmin ../N.latest/RSE-SDK-latest.zip
  
  #Update the testUpdates sites
  echo "Refreshing update site"
  cd $HOME/downloads-tm/testUpdates/bin
  ./mkTestUpdates.sh
  echo "Refreshing signedUpdates site"
  cd $HOME/downloads-tm/signedUpdates/bin
  ./mkTestUpdates.sh

  cd "$curdir"
else
  echo "package.count missing, release seems failed"
fi
