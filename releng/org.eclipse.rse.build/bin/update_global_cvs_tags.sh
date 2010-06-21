#!/bin/sh
#*******************************************************************************
# Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
# Martin Oberhuber (Wind River) - initial API and implementation
#*******************************************************************************
#
# This script can be used to apply a release tag or branch tag globally
# to those files of the TM CVS Repository that are usually not seen. It
# needs to run from command-line because under Eclipse, these top-level
# modules and files would require checking out the entire TM module tree;
# from commandline, this works with checking out the top-level directories
# only.
#
# These files are named "readme.txt" in the top-level CVS modules, and
# contribute to the tags shown in the Eclipse CVS Repository Explorer
# when expanding the "Branches" or the "Tags" node.
#
# Before running this script, customize the "cvs tag" commands below.
#
curdir=`pwd`
CVSROOT=:ext:moberhuber@dev.eclipse.org:/cvsroot/dsdp
export CVSROOT
TOPMODULES="\
  org.eclipse.tm.rse \
  org.eclipse.tm.rse/doc \
  org.eclipse.tm.rse/examples \
  org.eclipse.tm.rse/features \
  org.eclipse.tm.rse/plugins \
  org.eclipse.tm.rse/releng \
  org.eclipse.tm.rse/tests \
  org.eclipse.tm.core \
  org.eclipse.tm.core/discovery \
  org.eclipse.tm.core/terminal \
  org.eclipse.tm.core/wince \
"
###  org.eclipse.tm.core/thirdparty \
cvs co -l $TOPMODULES
##exit 0
for topmod in $TOPMODULES ; do
  cd $topmod
  #cvs update -l -r R1_0 .
  #cvs tag -b R1_0_maintenance readme.txt
  #cvs tag -d v20060630 readme.txt
  cvs update -l -A .
  cvs tag R3_2
  cd $curdir
done
