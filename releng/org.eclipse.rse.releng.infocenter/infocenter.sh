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

PATH=/opt/j2sdk1.4.2_12/bin:$PATH
export PATH

EHOME=/home/infocenter/eclipse3.2/eclipse
if [ "$IHOME" = "" ]; then
  IHOME=/home/infocenter/latest
fi

if [ "$1" = "" -o "$1" = "help" ]; then
  echo "Usage: infocenter.sh [start|shutdown|addSite -from dir|apply]"
  exit 1
fi

java \
  -classpath $EHOME/plugins/org.eclipse.help.base_3.2.0.v20060601.jar \
  org.eclipse.help.standalone.Infocenter \
  -command $* \
  -eclipsehome $EHOME \
  -data $IHOME/workspace \
  -port 27127 \
  -nl en -locales en \
  -configuration file://$IHOME/config 

