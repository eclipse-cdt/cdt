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
#
# setup.sh : Set up an environment for building TM / RSE
# Works on build.eclipse.org -- may need to be adjusted
# for other hosts.
#
# This must be run in $HOME/ws2 in order for the mkTestUpdateSite.sh
# script to find the published packages
#
# Bootstrapping: Get this script by
# wget -O setup.sh "http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.tm.rse/releng/org.eclipse.rse.build/setup.sh?rev=HEAD&cvsroot=DSDP_Project&content-type=text/plain"
# sh setup.sh
# ./doit_ibuild.sh
# cd testUpdates/bin
# mkTestUpdates.sh

curdir=`pwd`

# prepare the base Eclipse installation in folder "eclipse"
if [ ! -f eclipse/plugins/org.eclipse.core.resources_3.3.0.v20070202.jar ]; then
  ## Eclipse Platform 3.3M5
  #wget "http://download.eclipse.org/eclipse/downloads/drops/S-3.3M5-200702091006/eclipse-platform-3.3M5-linux-gtk-ppc.tar.gz"
  #tar xfvz eclipse-platform-3.3M5-linux-gtk-ppc.tar.gz
  #rm eclipse-platform-3.3M5-linux-gtk-ppc.tar.gz
  # Eclipse SDK 3.3M5: Need the SDK because EMF needs JDT (somehow)
  wget "http://download.eclipse.org/eclipse/downloads/drops/S-3.3M5-200702091006/eclipse-SDK-3.3M5-linux-gtk-ppc.tar.gz"
  tar xfvz eclipse-SDK-3.3M5-linux-gtk-ppc.tar.gz
  rm eclipse-SDK-3.3M5-linux-gtk-ppc.tar.gz
fi
if [ ! -f eclipse/plugins/org.eclipse.cdt.core_4.0.0.200702161600.jar ]; then
  # CDT 4.0.0 Runtime
  wget "http://download.eclipse.org/tools/cdt/releases/europa/dist/4.0.0M5/cdt-4.0.0-M5-linux.ppc.tar.gz"
  tar xfvz cdt-4.0.0-M5-linux.ppc.tar.gz
  rm cdt-4.0.0-M5-linux.ppc.tar.gz
fi
if [ ! -f eclipse/plugins/org.eclipse.emf_2.2.0.v200702121527.jar ]; then
  # EMF 2.3.0 Runtime
  wget "http://download.eclipse.org//modeling/emf/emf/downloads/drops/2.3.0/S200702121527/emf-sdo-runtime-2.3.0M5.zip"
  unzip -o emf-sdo-runtime-2.3.0M5.zip
  rm emf-sdo-runtime-2.3.0M5.zip 
fi
if [ ! -f eclipse/plugins/org.junit_3.8.2/junit.jar ]; then
  # Eclipse Test Framework
  wget "http://download.eclipse.org/eclipse/downloads/drops/S-3.3M5-200702091006/eclipse-test-framework-3.3M5.zip"
  unzip -o eclipse-test-framework-3.3M5.zip
  rm eclipse-test-framework-3.3M5.zip
fi

# checkout the basebuilder
if [ ! -f org.eclipse.releng.basebuilder/plugins/org.eclipse.core.runtime_3.3.100.v20061204.jar ]; then
  if [ -d org.eclipse.releng.basebuilder ]; then
    rm -rf org.eclipse.releng.basebuilder
  fi
  cvs -d :pserver:anonymous@dev.eclipse.org:/cvsroot/eclipse co -r M4_33 org.eclipse.releng.basebuilder
fi

# checkout the RSE builder
if [ -f org.eclipse.rse.build/CVS/Entries ]; then
  cd org.eclipse.rse.build
  cvs update -dPR
  cd ..
else
  if [ -d org.eclipse.rse.build ]; then
    rm -rf org.eclipse.rse.build
  fi
  cvs -d :pserver:anonymous@dev.eclipse.org:/cvsroot/dsdp co -Rd org.eclipse.rse.build org.eclipse.tm.rse/releng/org.eclipse.rse.build
fi

# prepare directories for the build
if [ ! -d working/package ]; then
  mkdir -p working/package
fi
if [ ! -d working/build ]; then
  mkdir -p working/build
fi
if [ ! -e publish ]; then
  ln -s /home/data/httpd/download.eclipse.org/dsdp/tm/downloads/drops publish
fi
if [ ! -e testUpdates ]; then
  ln -s /home/data/httpd/download.eclipse.org/dsdp/tm/testUpdates testUpdates
fi
if [ ! -e udpates ]; then
  ln -s /home/data/httpd/download.eclipse.org/dsdp/tm/updates updates
fi
if [ ! -e staging ]; then
  ln -s /home/data/httpd/download-staging.priv/dsdp/tm staging
fi

# create symlinks as needed
ln -s org.eclipse.rse.build/bin/doit_ibuild.sh .
ln -s org.eclipse.rse.build/bin/doit_nightly.sh .
chmod a+x doit_ibuild.sh doit_nightly.sh
cd org.eclipse.rse.build
chmod a+x build.pl build.rb go.sh nightly.sh
cd ..

echo "Your build environment is now created."
echo ""
echo "Run ./doit_ibuild to create an I-build."
echo "You will need to enter 3 items by keyboard blindly."
echo ""
echo "When done, cd testUpdates/bin and run ./mkTestUpdates.sh"
echo ""
echo "Test the testUpdates, then copy them to updates:"
echo "cd updates"
echo "rm -rf plugins features"
echo "cp -R ../testUpdates/plugins ."
echo "cp -R ../testUpdates/features ."
echo "cd bin"
echo "./mkTestUpdates.sh"

exit 0
