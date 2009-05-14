#!/bin/sh
#*******************************************************************************
#  Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
#  All rights reserved. This program and the accompanying materials
#  are made available under the terms of the Eclipse Public License v1.0
#  which accompanies this distribution, and is available at
#  http://www.eclipse.org/legal/epl-v10.html
# 
#  Contributors:
#  Martin Oberhuber (Wind River) - initial API and implementation
#*******************************************************************************
#
# Usage: make sure that appropriate signed update site is referenced in UPDATE_SITE,
# and current directory is a download. Will create signed downloads in subdirectory.
#

UPDATE_SITE=$HOME/downloads-tm/updates/3.1milestones

curdir=`pwd`
cd `dirname $0`
mydir=`pwd`
cd "${curdir}"

#Use Java5 on build.eclipse.org
#export PATH=/shared/dsdp/tm/ibm-java2-ppc64-50/bin:$PATH
export PATH=/shared/dsdp/tm/ibm-java2-ppc64-50/jre/bin:/shared/dsdp/tm/ibm-java2-ppc64-50/bin:$PATH
#export PATH=${HOME}/ws2/IBMJava2-ppc-142/bin:$PATH

SIGNED_JAR_SOURCE=${mydir}/eclipse_ext/tm
##SIGNED_JAR_SOURCE=${mydir}/eclipse_ext
OUTPUT=${curdir}/output.$$
RESULT=${curdir}/result.$$
TMP=${curdir}/tmp.$$
BASEBUILDER=$HOME/ws2/eclipse

# Provision update site into SIGNED_JAR_SOURCE
if [ ! -d "${SIGNED_JAR_SOURCE}" ]; then
  mkdir -p "${SIGNED_JAR_SOURCE}"
fi
${BASEBUILDER}/eclipse -nosplash \
  -data install-ws -consolelog -clean \
  -application org.eclipse.equinox.p2.repository.repo2runnable \
  -source file:${UPDATE_SITE} \
  -destination file:${SIGNED_JAR_SOURCE} \
  -vmargs \
    -Xms128M -Xmx256M -XX:PermSize=128M -XX:MaxPermSize=256M 
retval=$?
echo "result: ${retval}
exit ${retval}

if [ ! -d ${SIGNED_JAR_SOURCE}/server ]; then
  mkdir ${SIGNED_JAR_SOURCE}/server
fi
have_server=`ls "${SIGNED_JAR_SOURCE}"/server/*.jar 2>/dev/null`
if [ "${have_server}" = "" ]; then
  cd ${SIGNED_JAR_SOURCE}/server
  unzip ${curdir}/rseserver-*-signed.zip
  have_server=`ls *.jar 2>/dev/null`
  cd "${curdir}"
  if [ "${have_server}" = "" ]; then
    echo 'ERROR: signed rseserver-*-signed.zip not found!'
    echo "Please sign a server zip on build.eclipse.org, upload and retry."
    exit 1
  fi
fi

if [ ! -d ${TMP} ]; then
  mkdir -p ${TMP} 
fi
if [ ! -d ${OUTPUT} ]; then
  mkdir -p ${OUTPUT} 
fi
if [ ! -d ${RESULT} ]; then
  mkdir -p ${RESULT} 
fi
for zip in `ls *.zip *.tar` ; do
  cd ${TMP}
  case ${zip} in
    *.zip) unzip -q ${curdir}/${zip} ;;
    *.tar) tar xf ${curdir}/${zip} ;;
  esac
  case ${zip} in
    rseserver*) SIGNED_JARS=${SIGNED_JAR_SOURCE}/server ;;
    *) SIGNED_JARS=${SIGNED_JAR_SOURCE} ;;
  esac
  REF=`find . -name 'epl-v10.html'`
  FILES=`find . -name '*.jar' -o -name 'META-INF'`
  for f in ${FILES} ; do
    printf "${f}: "
    if [ -f ${SIGNED_JARS}/${f} ]; then
      cp -f ${SIGNED_JARS}/${f} ./${f}
      touch -r ${REF} ./${f}
      echo "signed"
    elif [ -d ${SIGNED_JARS}/${f} ]; then
      cp -Rf ${SIGNED_JARS}/${f}/* ${f}
      touch -r ${REF} ${f}/*
      echo "signed"
    else
      echo "."
    fi
  done
  ##cp ${curdir}/${zip} ${OUTPUT}
  case ${zip} in
    *.zip) zip -r -o -q ${OUTPUT}/${zip} * ;;
    *.tar) tar cfv ${OUTPUT}/${zip} * ; touch -r ${REF} ${OUTPUT}/${zip};
  esac
  rm -rf *
  rm "${OUTPUT}"/rseserver-*-signed.zip
  cd ${RESULT}
  case ${zip} in
     rseserver*) mkdir ${zip} ; cd ${zip} ;
        case ${zip} in
           *.zip) unzip -q -o ${OUTPUT}/${zip} ;;
           *.tar) tar xf ${OUTPUT}/${zip} ;;
        esac
        ;;
     *) unzip -q -o ${OUTPUT}/${zip} ;;
  esac
done
rm -rf ${TMP}

echo "--------------------------------------"
echo "DONE"
echo "--------------------------------------"
cd "${curdir}"
echo "MAIN:---------------------------------"
diff -r ${RESULT} ${SIGNED_JAR_SOURCE}
for f in `ls rseserver-*.zip rseserver-*.tar` ; do
  echo "${f}:-----------------------------------"
  diff -r -b ${RESULT}/${f} ${SIGNED_JAR_SOURCE}/server
done

