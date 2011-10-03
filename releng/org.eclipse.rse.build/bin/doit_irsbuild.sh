#!/bin/sh
#*******************************************************************************
# Copyright (c) 2006, 2011 Wind River Systems, Inc.
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
#export PATH=/shared/tools/tm/jdk-1.5/bin:$PATH
export PATH=/shared/tools/tm/jdk-1.5/jre/bin:/shared/tools/tm/jdk-1.5/bin:$PATH
#export PATH=/shared/tools/tm/jdk-1.6/jre/bin:/shared/tools/tm/jdk-1.6/bin:$PATH
#export PATH=${HOME}/ws2/IBMJava2-ppc-142/bin:$PATH


#Get parameters
mapTag=HEAD
buildType=$1
buildId=$2
case x$buildType in
  xP|xN|xI|xS|xR) ok=1 ;;
  xH) mapTag=R3_3_maintenance ; ok=1 ;;
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
stamp=`date +'%Y%m%d-%H%M'`

echo "Updating builder from CVS..."
cd org.eclipse.tm.releng
CHANGEMAPS=`cvs -nq update -r ${mapTag} | head -1`
cd ../org.eclipse.rse.build
CHANGES=`cvs -nq update -r ${mapTag} | head -1`
if [ "${CHANGEMAPS}" = "" -a "${CHANGES}" = "" ]; then
  echo "Build ${buildType}${buildId} : ${mapTag} : ${stamp}"
  echo "Build canceled, no mapfile or config changed"
  echo "in org.eclipse.rse.build and org.eclipse.tm.releng."
  exit 0
fi

log=$HOME/ws2/log-${buildType}$stamp.txt
touch $log
cd ../org.eclipse.tm.releng
cvs -q update -r ${mapTag} -RPd >> $log 2>&1
cd ../org.eclipse.rse.build
cvs -q update -r ${mapTag} -RPd >> $log 2>&1

echo "Running the builder..."
daystamp=`date +'%Y%m%d*%H'`
./nightly.sh ${mapTag} ${buildType} ${buildId} >> $log 2>&1
tail -30 $log

#update the main download and archive pages: build.eclipse.org only
if [ -d /home/data/httpd/archive.eclipse.org/tm/downloads ]; then
  cd /home/data/httpd/archive.eclipse.org/tm/downloads
  cvs -q update -RPd >> $log 2>&1
  chgrp tools.tm * CVS/* 2>/dev/null
  cd /home/data/httpd/download.eclipse.org/tm/downloads
  cvs -q update -RPd >> $log 2>&1
  chgrp tools.tm * CVS/*

  #Fixup permissions and group id on download.eclpse.org (just to be safe)
  echo "Fixup: chgrp -R tools.tm drops/${buildType}*${daystamp}*"
  chgrp -R tools.tm drops/${buildType}*${daystamp}*
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
  realstamp=`echo $FILES | sed -e 's,RSE-SDK-,,g' -e 's,.zip,,g'`
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
    chgrp tools.tm ../N.latest/*.zip
    chmod g+w ../N.latest/*.zip
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
      
      echo "Creating TM-repo-${realstamp}.zip"
      cd ..
      ISITE=3.4interim
      if [ -d ${ISITE} ]; then
        rm -rf ${ISITE}
      fi
      FILES=`ls`
      tar cf - ${FILES} | (mkdir ${ISITE} ; cd ${ISITE} ; tar xf -)
      if [ -d ${ISITE} ]; then
         cd ${ISITE}
         rm -rf plugins/*.pack.gz features/*.pack.gz
         cd bin
         ./mkTestUpdates.sh
         cd ..
         rm -rf bin CVS .cvsignore web/CVS
         rm ../TM-repo-*.zip
         zip -r ../TM-repo-${realstamp}.zip .
         cd ..
         rm -rf ${ISITE}
         cd $HOME/ws2/publish
         cd $DIRS
         cp $HOME/downloads-tm/signedUpdates/TM-repo-${realstamp}.zip .
         echo "Successfully created TM-repo-${realstamp}.zip" 
         if [ -f package.count ]; then
           count=`cat package.count`
           count=`expr $count + 1`
           rm package.count
           echo $count > package.count
         fi

         echo "Making signed..."
         UPDATE_SITE=$HOME/downloads-tm/signedUpdates
         export UPDATE_SITE
         $HOME/ws2/org.eclipse.rse.build/bin/make_signed.sh -go
         echo "Made signed."
      fi
  fi
  
  cd "$curdir"
else
  echo "package.count missing, release seems failed"
fi
chgrp tools.tm $log
