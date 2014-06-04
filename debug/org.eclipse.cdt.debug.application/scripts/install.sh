#!/bin/sh
###############################################################################
# Copyright (c) 2014 Red Hat, Inc. and others
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#    Red Hat Inc. - initial API and implementation
###############################################################################
if [ ! -d $HOME/cdtdebugger ]; then
mkdir -p $HOME/cdtdebugger
fi
cp config.ini $HOME/cdtdebugger
cp dev.properties $HOME/cdtdebugger
cp cdtdebug.sh $HOME/cdtdebugger
chmod +x $HOME/cdtdebugger/cdtdebug.sh
olddir=`pwd`
cd ../..
PLUGINS_DIR=`pwd`
cd $olddir
sed -i -e "s,cd ../..,cd $PLUGINS_DIR," $HOME/cdtdebugger/cdtdebug.sh
echo "Installation complete"
