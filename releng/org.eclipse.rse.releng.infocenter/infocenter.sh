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
umask 022

#PATH=/opt/j2sdk1.4.2_12/bin:$PATH
PATH=/opt/JDKs/amd64/jdk1.6.0_11/bin:$PATH
export PATH

if [ "$IHOME" = "" ]; then
  IHOME=/home/infocenter/latest
fi
EHOME=${IHOME}/eclipse/eclipse
curdir=`pwd`

if [ "$1" = "" -o "$1" = "help" ]; then
  echo "Usage: infocenter.sh [start|shutdown|addSite -from dir|apply]"
  exit 1
fi

HELP_PLUGIN=`ls $EHOME/plugins/org.eclipse.help.base_3.*.jar | sort | tail -1`

java \
  -classpath $HELP_PLUGIN \
  org.eclipse.help.standalone.Infocenter \
  -command $* \
  -eclipsehome $EHOME \
  -data $IHOME/workspace \
  -port 27127 \
  -nl en -locales en
