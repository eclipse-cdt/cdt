#!/bin/sh
#*******************************************************************************
# Copyright (c) 2008, 2009 Wind River Systems, Inc.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html 
# 
# Contributors: 
# Martin Oberhuber - initial API and implementation 
#*******************************************************************************
umask 022

curdir=`pwd`
#PATH=/opt/j2sdk1.4.2_12/bin:$PATH
PATH=/opt/JDKs/amd64/jdk1.6.0_11/bin:$PATH
export PATH

if [ "$IHOME" = "" ]; then
  IHOME=/home/infocenter/latest
fi
# prepare the base Eclipse installation in folder "eclipse"
ECL_DIR=$IHOME/eclipse
#ep_rel=S
#ep_ver=3.7M3
#ep_date=201010281441
#ep_swtver=3.7.0.v3712b

#Use 3.6.1 base for now because 3.7M3 seems unreliable with long delay over network
ep_rel=R
ep_ver=3.6.1
ep_date=201009090800
ep_swtver=3.6.1.v3655c

uname_s=`uname -s`
uname_m=`uname -m`
case ${uname_s}${uname_m} in
  Linuxppc*) ep_arch=linux-gtk-ppc
             cdt_arch=linux.ppc
             ;;
  Linuxx86_64*) ep_arch=linux-gtk-x86_64 
                cdt_arch=linux.x86_64
                ;;
  Linuxx86*|Linuxi686*) ep_arch=linux-gtk
             cdt_arch=linux.x86
             ;;
esac

P2_no_dropins=false
if [ ! -f ${ECL_DIR}/eclipse/plugins/org.eclipse.swt_${ep_swtver}.jar ]; then
  if [ ! -d ${ECL_DIR} ]; then
    mkdir -p ${ECL_DIR}
  else
    rm -rf ${ECL_DIR}/*
  fi
  cd "${ECL_DIR}"
  echo "Getting Eclipse Platform SDK..."
  wget "http://download.eclipse.org/eclipse/downloads/drops/${ep_rel}-${ep_ver}-${ep_date}/eclipse-platform-SDK-${ep_ver}-${ep_arch}.tar.gz"
  tar xfvz eclipse-platform-SDK-${ep_ver}-${ep_arch}.tar.gz
  rm eclipse-platform-SDK-${ep_ver}-${ep_arch}.tar.gz
  cd "${curdir}"
fi
if [ ! -f "${ECL_DIR}/eclipse/startup.jar" ]; then
  cd "${ECL_DIR}/eclipse/plugins"
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
  cd "${curdir}"
fi
if [ ! -d "${ECL_DIR}/eclipse/plugins.disabled" ]; then
  mkdir -p "${ECL_DIR}/eclipse/plugins.disabled"
  cd "${ECL_DIR}/eclipse/plugins"
  mv org.eclipse.platform.doc.user_*.jar ../plugins.disabled
  cd "${curdir}"
fi 
