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
#Bootstrapping script to perform S-builds on build.eclipse.org
#Will ask the user for label and build ID

#nothing we do should be hidden from the world
umask 22

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
log=$HOME/ws/log-$stamp.txt
touch $log
cvs -q update -RPd
daystamp=`date +'%Y%m%d*%H'`
echo "Enter tag to fetch from CVS (default is HEAD):"
echo "Enter build type (P,N,I,S,R,M):"
echo "Enter the build id (default is x$stamp):"
perl ./build.pl >> $log 2>&1
#tail -50 $log

#Fixup permissions and group id on download.eclpse.org (just to be safe)
#chmod -R g+w $HOME/ws/working/package/*${daystamp}*

#Publish
echo "cp -R $HOME/ws/working/package/*${daystamp}* $HOME/ws/publish"
cp -R $HOME/ws/working/package/*${daystamp}* $HOME/ws/publish
rm -rf $HOME/ws/publish/*${daystamp}*/updates
#echo "chmod -R g+w $HOME/ws/publish/*${daystamp}*"
#chmod -R g+w $HOME/ws/publish/*${daystamp}*

#Check the publishing
cd $HOME/ws/publish
cd *${daystamp}*
if [ -f package.count ]; then
  #hide the release for now until it is tested
  #mirrors will still pick it up
  mv package.count package.count.orig
  
  #Update the testUpdates sites
  echo "Refreshing update site"
  cd $HOME/downloads-tm/testUpdates/bin
  ./mkTestUpdates.sh
  echo "Refreshing signedUpdates site"
  cd $HOME/downloads-tm/signedUpdates/bin
  ./mkTestUpdates.sh
  cd "$curdir"
fi
