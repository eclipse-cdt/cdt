#!/bin/sh
#*******************************************************************************
# Copyright (c) 2006, 2010 Wind River Systems, Inc.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html 
# 
# Contributors: 
# Martin Oberhuber - initial API and implementation 
#*******************************************************************************
#
# setup.sh : Set up an environment for building TM / RSE
# Works on build.eclipse.org -- may need to be adjusted
# for other hosts.
#
# This must be run in $HOME/ws2 in order for the mkTestUpdateSite.sh
# script to find the published packages
#
# Bootstrapping: Get this script by
# export CVSROOT=:pserver:anonymous@dev.eclipse.org:/cvsroot/dsdp
# cvs co -r HEAD org.eclipse.tm.rse/releng/org.eclipse.rse.build
# sh org.eclipse.tm.rse/releng/org.eclipse.rse.build/setup.sh
#
# - OR -
#
# wget -O setup.sh "http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.tm.rse/releng/org.eclipse.rse.build/setup.sh?rev=HEAD&cvsroot=DSDP_Project&content-type=text/plain"
# sh setup.sh
# ./doit_ibuild.sh
# cd testUpdates/bin
# mkTestUpdates.sh

curdir=`pwd`

uname_s=`uname -s`
uname_m=`uname -m`
case ${uname_s}${uname_m} in
  Linuxppc*) ep_arch=linux-gtk-ppc
             cdt_arch=linux.ppc
             ;;
  Linuxx86_64*) ep_arch=linux-gtk-x86_64 
                cdt_arch=linux.x86_64
                ;;
  Linuxx86*) ep_arch=linux-gtk
             cdt_arch=linux.x86
             ;;
esac

# prepare the base Eclipse installation in folder "eclipse"
ep_rel="S-"
ep_ver=3.7M3
ep_date="-201010281441"
P2_disabled=false
P2_no_dropins=false
if [ ! -f eclipse/plugins/org.eclipse.swt_3.7.0.v3712b.jar ]; then
  curdir2=`pwd`
  if [ ! -d eclipse -o -h eclipse ]; then
    if [ -d eclipse-${ep_ver}-${ep_arch} ]; then
      rm -rf eclipse-${ep_ver}-${ep_arch}
    fi
    mkdir eclipse-${ep_ver}-${ep_arch}
    cd eclipse-${ep_ver}-${ep_arch}
  else
    rm -rf eclipse
  fi
  # Eclipse SDK: Need the SDK so we can link into docs
  echo "Getting Eclipse SDK..."
  wget "http://download.eclipse.org/eclipse/downloads/drops/${ep_rel}${ep_ver}${ep_date}/eclipse-SDK-${ep_ver}-${ep_arch}.tar.gz"
  tar xfvz eclipse-SDK-${ep_ver}-${ep_arch}.tar.gz
  rm eclipse-SDK-${ep_ver}-${ep_arch}.tar.gz
  cd "${curdir2}"
  if [ ! -d eclipse -o -h eclipse ]; then
    if [ -e eclipse ]; then 
      rm eclipse
    fi
    ln -s eclipse-${ep_ver}-${ep_arch}/eclipse eclipse
  fi
fi
if [ ! -f eclipse/startup.jar ]; then
  curdir2=`pwd`
  cd eclipse/plugins
  if [ -h ../startup.jar ]; then
    rm ../startup.jar
  fi
  LAUNCHER=`ls org.eclipse.equinox.launcher_*.jar | sort | tail -1`
  if [ "${LAUNCHER}" != "" ]; then
    echo "eclipse LAUNCHER=${LAUNCHER}" 
    ln -s plugins/${LAUNCHER} ../startup.jar
  else
    echo "Eclipse: NO startup.jar LAUNCHER FOUND!"
  fi
  cd ${curdir2}
fi

if ${P2_no_dropins} ; then
  #P2 disabled?
  DROPIN=.
  DROPUP=.
else
  #P2 enabled
  DROPIN=eclipse/dropins
  DROPUP=../..
fi

# EMF 2.5.0
EMFBRANCH=2.5.0
EMFREL=R
EMFDATE=200906151043
EMFVER=2.5.0
if [ ! -f ${DROPIN}/eclipse/plugins/org.eclipse.emf.doc_${EMFBRANCH}.v${EMFDATE}.jar ]; then
  # Need EMF 2.4 SDK for Service Discovery ISV Docs Backlinks
  echo "Getting EMF SDK..."
  cd ${DROPIN}
  wget "http://download.eclipse.org/modeling/emf/emf/downloads/drops/${EMFBRANCH}/${EMFREL}${EMFDATE}/emf-xsd-SDK-${EMFVER}.zip"
  unzip -o emf-xsd-SDK-${EMFVER}.zip
  rm emf-xsd-SDK-${EMFVER}.zip
  cd ${DROPUP}
fi
if [ ! -f eclipse/plugins/org.junit_3.8.2.v3_8_2_v20100427-1100/junit.jar ]; then
  # Eclipse Test Framework
  echo "Getting Eclipse Test Framework..."
  wget "http://download.eclipse.org/eclipse/downloads/drops/${ep_rel}${ep_ver}${ep_date}/eclipse-test-framework-${ep_ver}.zip"
  unzip -o eclipse-test-framework-${ep_ver}.zip
  rm eclipse-test-framework-${ep_ver}.zip
fi
if [ ! -f ${DROPIN}/eclipse/plugins/gnu.io.rxtx_2.1.7.4_v20071016.jar ]; then
  echo "Getting RXTX..."
  cd ${DROPIN}
  #wget "http://rxtx.qbang.org/eclipse/downloads/RXTX-SDK-I20071016-1945.zip"
  #unzip -o RXTX-SDK-I20071016-1945.zip
  #rm RXTX-SDK-I20071016-1945.zip
  wget "http://download.eclipse.org/athena/runnables/RXTX-runtime-I20071016-1945.zip"
  unzip -o RXTX-runtime-I20071016-1945.zip
  rm RXTX-runtime-I20071016-1945.zip
  cd ${DROPUP}
fi

# Sonatype / Tycho app for generating p2 download stats
# See https://bugs.eclipse.org/bugs/show_bug.cgi?id=310132
if [ ! -f ${DROPIN}/org.sonatype.tycho.p2.updatesite_0.9.0.201005191712.jar ]; then
  echo "Getting Download Stats Generator..."
  cd ${DROPIN}
  wget "https://bugs.eclipse.org/bugs/attachment.cgi?id=171500" -O addStats_v3.zip
  unzip -o addStats_v3.zip
  rm addStats_v3.zip
  cd ${DROPUP}
fi

# CDT Runtime
#CDTREL=7.0.0
#CDTFEAT=7.0.0
#CDTVER=201006141710
CDTREL=8.0.0
CDTFEAT=8.0.0
CDTVER=201011050851
CDTNAME=cdt-master-${CDTREL}-I${CDTVER}.zip
CDTLOC=builds/${CDTREL}/I.I${CDTVER}/${CDTNAME}
if [ ! -f eclipse/plugins/org.eclipse.cdt_${CDTFEAT}.${CDTVER}.jar ]; then
  echo "Getting CDT Runtime..."
  wget "http://download.eclipse.org/tools/cdt/${CDTLOC}"
  CDTTMP=`pwd`/tmp.$$
  mkdir ${CDTTMP}
  cd ${CDTTMP}
  unzip ../${CDTNAME}
  cd ..
  #java -jar eclipse/startup.jar \
  LAUNCHER=`ls eclipse/plugins/org.eclipse.equinox.launcher_*.jar | sort | tail -1`
  java -jar ${LAUNCHER} \
    -application org.eclipse.update.core.standaloneUpdate \
    -command install \
    -from file://${CDTTMP} \
    -featureId org.eclipse.cdt.platform \
    -version ${CDTFEAT}.${CDTVER}
  java -jar ${LAUNCHER} \
    -application org.eclipse.update.core.standaloneUpdate \
    -command install \
    -from file://${CDTTMP} \
    -featureId org.eclipse.cdt \
    -version ${CDTFEAT}.${CDTVER}
  rm -rf ${CDTTMP}
  rm ${CDTNAME}
fi

# checkout the basebuilder
baseBuilderTag=R36_RC4
if [ ! -f org.eclipse.releng.basebuilder/plugins/org.eclipse.pde.core_3.6.0.v20100601.jar \
  -o ! -f org.eclipse.releng.basebuilder/plugins/org.eclipse.pde.build_3.6.0.v20100603/pdebuild.jar \
  -o ! -f org.eclipse.releng.basebuilder/plugins/org.eclipse.equinox.p2.metadata.generator_1.0.200.v20100503a.jar ]; then
  if [ -d org.eclipse.releng.basebuilder ]; then
    echo "Re-getting basebuilder from CVS..."
    rm -rf org.eclipse.releng.basebuilder
  else
    echo "Getting basebuilder from CVS..."
  fi
  cvs -Q -d :pserver:anonymous@dev.eclipse.org:/cvsroot/eclipse co -r ${baseBuilderTag} org.eclipse.releng.basebuilder
fi
if [ ! -f org.eclipse.releng.basebuilder/startup.jar ]; then
  curdir2=`pwd`
  cd org.eclipse.releng.basebuilder/plugins
  if [ -h ../startup.jar ]; then
    rm ../startup.jar
  fi
  LAUNCHER=`ls org.eclipse.equinox.launcher_*.jar | sort | tail -1`
  if [ "${LAUNCHER}" != "" ]; then
    echo "basebuilder: LAUNCHER=${LAUNCHER}" 
    ln -s plugins/${LAUNCHER} ../startup.jar
  else 
    echo "basebuilder: NO LAUNCHER FOUND"
  fi
  cd ${curdir2}
fi

# checkout the RSE builder
if [ -f org.eclipse.rse.build/CVS/Entries ]; then
  echo "Updating org.eclipse.rse.build from CVS"
  cd org.eclipse.rse.build
  cvs -q update -A -dPR
  cd ..
else
  if [ -d org.eclipse.rse.build ]; then
    echo "Re-getting org.eclipse.rse.build from CVS"
    rm -rf org.eclipse.rse.build
  else
    echo "Getting org.eclipse.rse.build from CVS"
  fi
  cvs -q -d :pserver:anonymous@dev.eclipse.org:/cvsroot/dsdp co -Rd org.eclipse.rse.build org.eclipse.tm.rse/releng/org.eclipse.rse.build
fi

# prepare directories for the build
echo "Preparing directories and symbolic links..."
if [ ! -d working/package ]; then
  mkdir -p working/package
fi
if [ ! -d working/build ]; then
  mkdir -p working/build
fi
if [ ! -d publish ]; then
  D=/home/data/httpd/download.eclipse.org/dsdp/tm/downloads/drops
  if [ -d ${D} ]; then ln -s ${D} publish; else mkdir publish; fi
fi
if [ ! -d testUpdates ]; then
  D=/home/data/httpd/download.eclipse.org/dsdp/tm/testUpdates
  if [ -d ${D} ]; then ln -s ${D} testUpdates; else mkdir testUpdates; fi
fi
if [ ! -d updates ]; then
  D=/home/data/httpd/download.eclipse.org/dsdp/tm/updates
  if [ -d ${D} ]; then ln -s ${D} updates; else mkdir updates; fi
fi
if [ ! -d staging ]; then
  D=/home/data/httpd/download-staging.priv/dsdp/tm
  if [ -d ${D} ]; then ln -s ${D} staging; else mkdir staging; fi
fi

# create symlinks as needed
if [ ! -h doit_irsbuild.sh ]; then
  ln -s org.eclipse.rse.build/bin/doit_irsbuild.sh .
fi
if [ ! -h doit_nightly.sh ]; then
  ln -s org.eclipse.rse.build/bin/doit_nightly.sh .
fi
if [ ! -h setup.sh ]; then
  if [ -f setup.sh ]; then rm -f setup.sh; fi
  ln -s org.eclipse.rse.build/setup.sh .
fi
chmod a+x doit_irsbuild.sh doit_nightly.sh
cd org.eclipse.rse.build
chmod a+x build.pl build.rb go.sh nightly.sh setup.sh
cd ..

echo "Your build environment is now created."
echo ""
echo "Run \"./doit_irsbuild.sh I\" to create an I-build."
echo ""
echo "Test the testUpdates, then copy them to updates:"
echo "cd updates"
echo "rm -rf plugins features"
echo "cp -R ../testUpdates/plugins ."
echo "cp -R ../testUpdates/features ."
echo "cd bin"
echo "cvs update"
echo "./mkTestUpdates.sh"

exit 0
