#!/bin/sh
#*******************************************************************************
# Copyright (c) 2007 Wind River Systems, Inc.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html 
# 
# Contributors: 
# Martin Oberhuber - initial API and implementation 
#*******************************************************************************
# Sign a list of files on build.eclipse.org
#
# Usage:
# sign.sh a.jar featurs/b.jar `ls plugins/*.jar`
#
# Prerequisites: 
# - Eclipse 3.3Mx installed in $HOME/ws2/eclipse
# - Java5 in the PATH or in /shared/tools/tm/jdk-1.5

curdir=`pwd`
cd `dirname $0`
mydir=`pwd`

umask 022

#Use Java5 on build.eclipse.org - need JRE for pack200
export PATH=/shared/tools/tm/jdk-1.5/jre/bin:/shared/tools/tm/jdk-1.5/bin:$PATH
basebuilder=${HOME}/ws2/org.eclipse.releng.basebuilder

FILES=$*

# Work on update site
cd ..
SITE=`pwd`

STAGING=/home/data/httpd/download-staging.priv/dsdp/tm
stamp=`date +'%Y%m%d-%H%M'`
WORKDIR=${STAGING}/${stamp}
mkdir -p ${WORKDIR}
REALFILES=""
NAMES=""
echo "Bock ma's"
for file in ${FILES}; do
  echo "file: ${file}"
  cd ${SITE}
  if [ -f ${file} ]; then
    name=`basename ${file}`
    echo "signing: ${name}"
    NAMES="${NAMES} ${name}"
    REALFILES="${REALFILES} ${file}"
    cp ${file} ${WORKDIR}
    cd ${WORKDIR}
    sign ${name} nomail >/dev/null
  fi
done
mkdir ${WORKDIR}/done
cd ${WORKDIR}
TRIES=10
while [ $TRIES -gt 0 ]; do
  sleep 30
  anyleft=0
  echo "TRIES to go: ${TRIES}"
  for x in ${NAMES} ; do
    if [ -f ${x} ]; then
      anyleft=1
      result=`jarsigner -verify ${x}`
      if [ "$result" = "jar verified." ]; then
        echo "${result}: ${x}"
        mv ${x} ${WORKDIR}/done/${x}
      else
        echo "-pending- ${x} : ${result}" | head -1
        sleep 30
      fi
    fi
  done
  if [ "${anyleft}" = "0" ]; then
     TRIES=0
     ok=1
  else
     echo "--> left:"
     ls
     TRIES=`expr $TRIES - 1`
     ok=0
  fi
done
if [ "$ok" = "1" ]; then
  cd ${SITE}
  echo "Signed, ok, packing: ${REALFILES}"
  for x in ${REALFILES} ; do
    name=`basename ${x}`
    #cp -f ${WORKDIR}/done/${name} ${x}
    echo "signed, packing: ${x}"
    java -jar ${basebuilder}/plugins/org.eclipse.equinox.launcher.jar \
      -application org.eclipse.update.core.siteOptimizer \
      -jarProcessor -outputDir `dirname ${x}` \
      -pack ${WORKDIR}/done/${name}
  done
  rm -rf ${WORKDIR}
else
  echo "Something went wrong during staging and signing."
  echo "Keeping existing update site intact."
  rm -rf ${WORKDIR}
  exit 1
fi

#Create the digest
echo "Creating digest..."
#java -jar $HOME/ws2/eclipse/startup.jar \
java -jar ${basebuilder}/plugins/org.eclipse.equinox.launcher.jar \
    -application org.eclipse.update.core.siteOptimizer \
    -digestBuilder -digestOutputDir=$SITE \
    -siteXML=$SITE/site.xml

cd $SITE
chgrp -R dsdp-tmadmin .
chmod -R g+w .
chmod -R a+r .
cd $curdir
