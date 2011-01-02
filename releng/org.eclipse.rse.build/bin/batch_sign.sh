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
#:# Script to sign all ZIP files in a directory
#:#
#:# Usage:
#:#    batch_sign.sh {directory}
#:# Examples:
#:#    batch_sign.sh publish/S-1.0M5-200611100500

#nothing we do should be hidden from the world
umask 22

#Use Java5 on build.eclipse.org
#export PATH=/shared/tools/tm/jdk-1.5/bin:$PATH
export PATH=/shared/tools/tm/jdk-1.5/jre/bin:/shared/tools/tm/jdk-1.5/bin:$PATH
#export PATH=${HOME}/ws2/IBMJava2-ppc-142/bin:$PATH

curdir=`pwd`

#Get parameters
dirToSign=$1
usage=0
if [ "$dirToSign" = "" ]; then
  usage=1
elif [ ! -d "$dirToSign" ]; then
  usage=1
fi
if [ $usage = 1 ]; then
  grep '^#:#' $0 | grep -v grep | sed -e 's,^#:#,,'
  exit 0
fi

#sign the zipfiles
cd "$dirToSign"
RDIR=`pwd`
mkdir -p signed
nameToSign=`basename $RDIR`
ZIPS=`ls *.zip *.jar`
STAGING=/home/data/httpd/download-staging.priv/tools/tm
STDIR=${STAGING}/${nameToSign}
mkdir -p ${STDIR}
cp ${ZIPS} ${STDIR}
cd ${STDIR}
mkdir out
for x in $ZIPS ; do
  sign $x nomail ${STDIR}/out
done
echo "Waiting for signature..."
sleep 300
TRIES=20
MISSING="$ZIPS"
while [ "$MISSING" != "" -a ${TRIES} -gt 0 ]; do
  MISSING_NEW=""
  sleep 60
  for x in $MISSING ; do
    if [ -f ${STDIR}/out/$x ]; then
      echo "Done: TRIES=${TRIES}, $x"
      cp -f ${STDIR}/out/$x ${RDIR}/signed/$x
      chmod ugo+r ${RDIR}/signed/$x
    else
      MISSING_NEW="${MISSING_NEW} $x"
    fi
  done
  echo "Signed: TRIES=${TRIES}, Missing ${MISSING_NEW}"
  MISSING="${MISSING_NEW}"
  TRIES=`expr $TRIES - 1`
done
rm -rf ${STDIR}

cd "$curdir"
if [ "$MISSING" != "" ]; then
  echo "batch_sign failed: ${MISSING}"
  exit 1
fi
echo "batch_sign complete"
exit 0
