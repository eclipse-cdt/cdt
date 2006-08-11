#!/bin/sh
#Bootstrapping script to perform N-builds on build.eclipse.org

#nothing we do should be hidden from the world
##newgrp dsdp-tm-rse # newgrp doesnt work from shellscripts -- use sg instead
umask 2

#Use Java5 on build.eclipse.org
#export PATH=/shared/common/ibm-java2-ppc64-50/bin:$PATH
export PATH=/shared/webtools/apps/IBMJava2-ppc64-142/bin:$PATH
#export PATH=/shared/webtools/apps/IBMJava2-ppc-142/bin:$PATH

curdir=`pwd`

#Remove old logs and builds
echo "Removing old logs and builds..."
cd $HOME/ws
rm log-*.txt
if [ -d working/package ]; then
  rm -rf working/package
fi

#Do the main job
cd org.eclipse.rse.build
stamp=`date +'%Y%m%d-%H%M'`
log=$HOME/ws/log-$stamp.txt
sg dsdp-tm-rse -c "touch $log"
sg dsdp-tm-rse -c "cvs -q update -d >> $log 2>&1"
daystamp=`date +'%Y%m%d-%H'`
sg dsdp-tm-rse -c "./nightly.sh >> $log 2>&1"
tail -50 $log

#Fixup permissions and group id on download.eclpse.org (just to be safe)
chmod -R g+w $HOME/ws/publish/N${daystamp}*

#Copy latest SDK in order to give access to DOC server
cd $HOME/ws/publish
FILES=`ls N${daystamp}*/RSE-SDK-N${daystamp}*.zip 2>/dev/null`
echo "FILES=$FILES"
if [ "$FILES" != "" ]; then
  rm N.latest/RSE-SDK-N*.zip
  cp N${daystamp}*/RSE-SDK-N${daystamp}*.zip N.latest
  cd N.latest
  mv -f RSE-SDK-N${daystamp}*.zip RSE-SDK-latest.zip
fi

#Cleanup old nightly builds (leave only last 5 in place)
cd $HOME/ws/publish
ls -d N200* | sort | head -n-5 | xargs rm -rf

