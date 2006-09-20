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

#Find out the IHOME - it's the parent of my own directory ($IHOME/bin)
curdir=`pwd`
cd `dirname $0`
mydir=`pwd`
cd ..
IHOME=`pwd`
export IHOME

#Update scripts - prerequisite: IHOME is infocenter home
umask 022
cd "$IHOME/bin"
cvs -q update -d
chmod a+x *.sh

#Update the infocenter
./update.sh
