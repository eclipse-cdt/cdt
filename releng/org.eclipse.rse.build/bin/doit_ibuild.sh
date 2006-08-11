#!/bin/sh
#Bootstrapping script to perform I-builds on build.eclipse.org

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
sg dsdp-tm-rse -c "touch $log"
sg dsdp-tm-rse -c "cvs -q update -d"
daystamp=`date +'%Y%m%d-%H'`
echo "Enter tag to fetch from CVS (default is HEAD):"
echo "Enter build type (P,N,I,S):"
echo "Enter the build id (default is x$stamp):"
sg dsdp-tm-rse -c "perl ./build.pl >> $log 2>&1"
#tail -50 $log

#Fixup permissions and group id on download.eclpse.org (just to be safe)
chmod -R g+w $HOME/ws/working/package/*${daystamp}*

#Publish
echo "sg dsdp-tm-rse -c \"cp -R $HOME/ws/working/package/*${daystamp}* $HOME/ws/publish\""
sg dsdp-tm-rse -c "cp $HOME/ws/working/package/*${daystamp}*/* $HOME/ws/publish"
sg dsdp-tm-rse -c "rm -rf $HOME/ws/publish/*${daystamp}*/updates"
echo "chmod -R g+w $HOME/ws/publish/*${daystamp}*"
chmod -R g+w $HOME/ws/publish/*${daystamp}*

