#!/bin/sh
#
# setup.sh : Set up an environment for building TM / RSE
# Works on build.eclipse.org -- may need to be adjusted
# for other hosts.
#
# This must be run in $HOME/ws in order for the mkTestUpdateSite.sh
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
if [ ! -f eclipse/plugins/org.eclipse.core.resources_3.2.0.v20060603.jar ]; then
  # Eclipse Platform 3.2
  wget "http://download.eclipse.org/eclipse/downloads/drops/R-3.2-200606291905/eclipse-platform-3.2-linux-gtk-ppc.tar.gz"
  tar xfvz eclipse-platform-3.2-linux-gtk-ppc.tar.gz
  rm eclipse-platform-3.2-linux-gtk-ppc.tar.gz
fi
if [ ! -f eclipse/plugins/org.eclipse.cdt.core_3.1.0.200606261600.jar ]; then
  # CDT 3.1.0 Runtime
  wget "http://download.eclipse.org/tools/cdt/releases/callisto/dist/3.1.0/org.eclipse.cdt-3.1.0-linux.ppc.tar.gz"
  tar xfvz org.eclipse.cdt-3.1.0-linux.ppc.tar.gz
  rm org.eclipse.cdt-3.1.0-linux.ppc.tar.gz
fi
if [ ! -f eclipse/plugins/org.eclipse.emf_2.2.0.v200606271057.jar ]; then
  # EMF 2.2.0 Runtime
  wget "http://download.eclipse.org/tools/emf/downloads/drops/2.2.0/R200606271057/emf-sdo-runtime-2.2.0.zip"
  unzip -o emf-sdo-runtime-2.2.0.zip
  rm emf-sdo-runtime-2.2.0.zip 
fi

# checkout the basebuilder
if [ ! -f org.eclipse.releng.basebuilder/plugins/org.eclipse.core.runtime_3.2.0.v20060603.jar ]; then
  if [ -d org.eclipse.releng.basebuilder ]; then
    rm -rf org.eclipse.releng.basebuilder
  fi
  cvs -d :pserver:anonymous@dev.eclipse.org:/cvsroot/eclipse co -r r321_v20060830 org.eclipse.releng.basebuilder
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
