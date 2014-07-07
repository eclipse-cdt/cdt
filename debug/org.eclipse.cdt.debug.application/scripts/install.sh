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
olddir=`pwd`
cd ../..
PLUGINS_DIR=`pwd`
# Verify that the install script is being run from a plug-ins folder of a
# downloaded Eclipse and not in a local user .eclipse folder.
if [ ! -f ../eclipse ]; then
echo "$0: error: eclipse executable not found in expected location"
echo " "
echo "This can occur if you are running this script from your local .eclipse directory"
echo "which would mean you are running a shared instance of the Eclipse platform for"
echo "your distro and have installed the Stand-alone Debugger from an eclipse.org"
echo "download repo.  Downloading the Stand-alone Debugger feature on top of a distro"
echo "version of Eclipse Debugger is not supported.  If you are using a distro version"
echo "of the Eclipse platform, you should not use this script.  Instead, install the"
echo "corresponding Eclipse CDT package for your distro (e.g. eclipse-cdt package)"
echo "which will install the Stand-alone Debugger for you."
exit 1
fi
cd $olddir
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
