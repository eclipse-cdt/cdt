#!/bin/sh
#*******************************************************************************
# Copyright (c) 2006, 2009 Wind River Systems, Inc.
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
#:#    doit_irsbuild.sh {buildType} [buildId] [maptag]
#:# Examples:
#:#    doit_irsbuild.sh R 1.0
#:#    doit_irsbuild.sh S 1.0M5 S1_0M5
#:#    doit_irsbuild.sh I

#nothing we do should be hidden from the world
umask 22

curdir=`pwd`
cd `dirname $0`
mydir=`pwd`
echo ${mydir}

#Use Java5 on build.eclipse.org
#export PATH=/shared/dsdp/tm/jdk-1.5/bin:$PATH
export PATH=/shared/dsdp/tm/jdk-1.5/jre/bin:/shared/dsdp/tm/jdk-1.5/bin:$PATH
#export PATH=${HOME}/ws2/IBMJava2-ppc-142/bin:$PATH


#Get parameters
mapTag=HEAD
buildType=$1
buildId=$2
case x$buildType in
  xP|xN|xI|xS|xR) ok=1 ;;
  xM) mapTag=R3_2_maintenance ; ok=1 ;;
  xJ) mapTag=R3_1_maintenance ; ok=1 ;;
  xK|xL) mapTag=R3_0_maintenance ; ok=1 ;;
  *) ok=0 ;;
esac
if [ "$3" != "" ]; then
  mapTag=$3
fi
if [ $ok != 1 ]; then
  grep '^#:#' $0 | grep -v grep | sed -e 's,^#:#,,'
  cd ${curdir}
  exit 0
fi

#Remove old logs and builds
echo "Removing old logs and builds..."
cd $HOME/ws2
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
CHANGES=`cvs -nq update -r ${mapTag} | head -1`
if [ "${CHANGES}" = "" ]; then
  echo "Build ${buildType}${buildId} : ${mapTag} : ${stamp}"
  echo "Build canceled, no mapfile or config changed in org.eclipse.rse.build."
  exit 0
fi
log=$HOME/ws2/log-${buildType}$stamp.txt
touch $log
#cvs -q update -RPd >> $log 2>&1
cvs -q update -r ${mapTag} -RPd >> $log 2>&1
daystamp=`date +'%Y%m%d*%H'`

echo "Running the builder..."
./nightly.sh ${mapTag} ${buildType} ${buildId} >> $log 2>&1
tail -30 $log

#update the main download and archive pages: build.eclipse.org only
if [ -d /home/data/httpd/archive.eclipse.org/dsdp/tm/downloads ]; then
  cd /home/data/httpd/archive.eclipse.org/dsdp/tm/downloads
  cvs -q update -RPd >> $log 2>&1
  chgrp dsdp-tmadmin * CVS/* 2>/dev/null
  cd /home/data/httpd/download.eclipse.org/dsdp/tm/downloads
  cvs -q update -RPd >> $log 2>&1
  chgrp dsdp-tmadmin * CVS/*

  #Fixup permissions and group id on download.eclpse.org (just to be safe)
  echo "Fixup: chgrp -R dsdp-tmadmin drops/${buildType}*${daystamp}*"
  chgrp -R dsdp-tmadmin drops/${buildType}*${daystamp}*
  chmod -R g+w drops/${buildType}*${daystamp}*
fi

#Check the publishing
cd $HOME/ws2/publish
DIRS=`ls -dt ${buildType}*${daystamp}* | head -1 2>/dev/null`
cd ${DIRS}
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
  
  #if [ "$DO_SIGN" = "1" ]; then
    #sign the zipfiles
    #${mydir}/batch_sign.sh `pwd`
  #fi

  if [ ${buildType} = N -a -d ../N.latest ]; then
    #update the doc server
    rm -f ../N.latest/RSE-*.zip
    rm -f ../N.latest/TM-*.zip
    cp -f RSE-SDK-*.zip ../N.latest/RSE-SDK-latest.zip
    TERM=`ls TM-terminal-*.zip | grep -v local`
    if [ x${TERM} != x ]; then
      cp -f ${TERM} ../N.latest/TM-terminal-latest.zip
    else
      echo "ERROR: missing TM-terminal-*.zip"
    fi
    #cp -f TM-discovery-*.zip ../N.latest/TM-discovery-latest.zip
    chgrp dsdp-tmadmin ../N.latest/*.zip
    chmod g+w ../N.latest/*.zip
    if [ -d /shared/dsdp/public_html/tm/downloads/drops/N.latest ]; then
      cp -f ../N.latest/* /shared/dsdp/public_html/tm/downloads/drops/N.latest/
      chmod -R g+w /shared/dsdp/public_html/tm/downloads/drops
    fi
  fi

  if [ ${buildType} != N ]; then
      #Update the testUpdates site
      echo "Refreshing update site"
      cd $HOME/downloads-tm/testUpdates/bin
      cvs update
      ./mkTestUpdates.sh
      #Update the signedUpdates site
      echo "Refreshing signedUpdates site"
      cd $HOME/downloads-tm/signedUpdates/bin
      cvs update
      ./mkTestUpdates.sh
  fi
  
  cd "$curdir"
else
  echo "package.count missing, release seems failed"
fi
chgrp dsdp-tm-rse $log
