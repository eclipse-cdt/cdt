#!/bin/sh
###############################################################################
# Copyright (c) 2014 Red Hat, Inc. and others
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#    Red Hat Inc. - initial API and implementation
###############################################################################

# Verify that the install script is being run from a plug-ins folder of a
# downloaded Eclipse and not in a local user .eclipse folder.

SCRIPT_DIR=`dirname $0`

IS_MAC=0

if [ ! -f "$SCRIPT_DIR/../../../eclipse" ]; then
  if [ ! -f "$SCRIPT_DIR/../../../../MacOS/eclipse" ]; then
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
  else
    IS_MAC=1
  fi
fi

if [ ! -d "$HOME/cdtdebugger" ]; then
  mkdir -p "$HOME/cdtdebugger"
fi
cp "$SCRIPT_DIR/config.ini" "$HOME/cdtdebugger"
cp "$SCRIPT_DIR/dev.properties" "$HOME/cdtdebugger"
cp "$SCRIPT_DIR/cdtdebug.sh" "$HOME/cdtdebugger"
chmod +x "$HOME/cdtdebugger/cdtdebug.sh"

if [ $IS_MAC -eq 0 ]; then
  ECLIPSE_HOME=$(cd "$SCRIPT_DIR/../../.." && pwd)
else
  ECLIPSE_HOME=$(cd "$SCRIPT_DIR/../../../../MacOS" && pwd)
fi

# Replace the entire line with tag @#@# by the actual location of the eclipse installation
sed -i -e "s,^.*@#@#.*$,ECLIPSE_HOME=$ECLIPSE_HOME," "$HOME/cdtdebugger/cdtdebug.sh"
echo "Installation complete"
