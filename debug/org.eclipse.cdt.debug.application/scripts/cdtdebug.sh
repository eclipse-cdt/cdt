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
cp config.ini $HOME/cdtdebugger
cp dev.properties $HOME/cdtdebugger
fi
olddir=`pwd`
cd ../..
OSGI_JAR=`ls org.eclipse.osgi_*.jar`
SWT_JAR=`ls org.eclipse.swt.*.jar`
SWT_PLUGIN=`echo $SWT_JAR | sed -e "s/_[0-9]*\..*.jar//"`
FS_JAR=`ls org.eclipse.core.filesystem.*.jar`
FS_PLUGIN=`echo $FS_JAR | sed -e "s/_[0-9]*\..*.jar//"`
LINUX_JAR=`ls org.eclipse.cdt.core.linux.*.jar`
LINUX_PLUGIN=`echo $LINUX_JAR | sed -e "s/_[0-9]*\..*.jar//"`
cd ..; ECLIPSE_HOME=`pwd`
cd $olddir
$ECLIPSE_HOME/eclipse -clean -product org.eclipse.cdt.debug.application.product \
-data $HOME/workspace-cdtdebug -configuration file\:$HOME/cdtdebugger \
-dev file\:$HOME/cdtdebugger/dev.properties $@ \
-vmargs -Dosgi.jar=$OSGI_JAR -Dswt.plugin=$SWT_PLUGIN -Dfs.plugin=$FS_PLUGIN \
-Dlinux.plugin=$LINUX_PLUGIN -Declipse.home=$ECLIPSE_HOME

