#!/bin/sh
#*******************************************************************************
# Copyright (c) 2009 Wind River Systems, Inc. and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
# Wind River Systems, Inc - initial API and implementation
#*******************************************************************************
#
## Rename a build for release
## usage:
##     cd drops/I20090916-0905
##     rename_build.sh R 3.1.1 200909160905
##
## Creates ../R-3.1.1-200909160905 and prepares renamed build there
#
case x$1 in
  xR) tgtBuild=Release ;;
  xM) tgtBuild=Maintenance ;;
  xI) tgtBuild=Integration ;;
  xS) tgtBuild=Stable ;;
  *) grep '^##' $0
        exit 0
        ;;
esac
tgtType=$1
tgtVer=$2
tgtDate=$3
tgtDir=${tgtType}-${tgtVer}-${tgtDate}

if [ ! -f package.count ]; then
  echo ERROR: package.count not found. Please cd to source build before running.
  exit 1
fi
if [ -d ../$tgtDir ]; then
  echo ERROR: target dir ../$tgtDir already exists
  exit 1
fi
srcVer=`ls RSE-SDK-*.zip | sed -e 's,RSE-SDK-,,' -e 's,\.zip,,'`
if [ ! -f RSE-runtime-${srcVer}.zip ]; then
  echo ERROR: RSE-runtime-${srcVer}.zip not found, incorrect source?
  exit 1
fi
echo "Renaming Release: ${srcVer} --> ${tgtVer} into ../${tgtDir}"

mkdir ../${tgtDir}
for x in `ls` ; do
  if [ -f ${x} ]; then
    case $x in
    *-signed.zip)
      ;;
    *.zip|*.tar)
      y=`echo $x | sed -e "s,${srcVer},${tgtVer},"`
      cp ${x} ../${tgtDir}/${y}
      ;;
    index.php|buildNotes.php)
      cat ${x} | sed -e "s,/${srcVer},/${tgtDir},g" \
        -e "s,${srcVer},${tgtVer},g" \
        -e "/TM/s,Integration,${tgtBuild},g" \
        -e "/TM/s,Maintenance,${tgtBuild},g" \
        -e "/TM/s,Stable,${tgtBuild},g" \
        > ../${tgtDir}/${x}
      ;;
    package.count)
      cp ${x} ../${tgtDir}/${x}.orig
      ;;
    *)
      cp ${x} ../${tgtDir}/${x}
      ;;
    esac
  fi
done
