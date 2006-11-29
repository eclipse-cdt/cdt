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
#Bootstrapping script to perform I-builds on build.eclipse.org
#Will build based on HEAD of all mapfiles, and update the testUpdates as well

#nothing we do should be hidden from the world
##newgrp dsdp-tm-rse # newgrp doesnt work from shellscripts -- use sg instead
umask 2

#Use Java5 on build.eclipse.org
#export PATH=/shared/common/ibm-java2-ppc64-50/bin:$PATH
#export PATH=/shared/webtools/apps/IBMJava2-ppc64-142/bin:$PATH
export PATH=/shared/webtools/apps/IBMJava2-ppc-142/bin:$PATH

curdir=`pwd`

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
log=$HOME/ws/log-I$stamp.txt
sg dsdp-tm-rse -c "touch $log"
sg dsdp-tm-rse -c "cvs -q update -RPd >> $log 2>&1"
daystamp=`date +'%Y%m%d-%H'`

echo "Running the builder..."
sg dsdp-tm-rse -c "./nightly.sh HEAD I >> $log 2>&1"
tail -50 $log

#Fixup permissions and group id on download.eclpse.org (just to be safe)
chmod -R g+w $HOME/ws/publish/I${daystamp}*

#Copy latest SDK in order to give access to DOC server
cd $HOME/ws/publish
FILES=`ls I${daystamp}*/RSE-SDK-I${daystamp}*.zip 2>/dev/null`
echo "FILES=$FILES"
if [ "$FILES" != "" ]; then
  rm N.latest/RSE-SDK-I*.zip
  cp I${daystamp}*/RSE-SDK-I${daystamp}*.zip N.latest
  cd N.latest
  mv -f RSE-SDK-I${daystamp}*.zip RSE-SDK-latest.zip
fi

#Update the testUpdates sites
if [ "$FILES" != "" ]; then
  sg dsdp-tm-rse -c "echo \"Refreshing update site\" "
  cd $HOME/downloads-tm/testUpdates/bin
  ./mkTestUpdates.sh
  sg dsdp-tm-rse -c "echo \"Refreshing signedUpdates site\" "
  cd $HOME/downloads-tm/signedUpdates/bin
  ./mkTestUpdates.sh
  cd "$curdir"
fi
