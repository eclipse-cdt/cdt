#!/bin/bash
###############################################################################
# Copyright (c) 2018, 2020 Kichwa Coders Ltd and others.
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################

set -e

##
# This script is reused by other projects, if so, COREPROJECT should be set
# to the project to use a basis for project settings
##
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
CDTDIR=${DIR}/../..
: ${COREPROJECT:=core/org.eclipse.cdt.core}

##
# Format code
##
: ${ECLIPSE:=~/buildtools/eclipse-SDK-4.34/eclipse}

if test ! -e "$ECLIPSE" ; then
    echo "The Eclipse binary was not found at \"$ECLIPSE\"!"
    echo "You can download it to and pass it via environment variable:"
    echo "  curl -L https://download.eclipse.org/eclipse/downloads/drops4/R-4.34-202402290520/eclipse-SDK-4.34-linux-gtk-x86_64.tar.gz | tar xzC /tmp"
    echo "  ECLIPSE=/tmp/eclipse/eclipse ./releng/scripts/check_code_cleanliness.sh"
    exit 1
fi

if test -e check_code_cleanliness_workspace; then
    echo check_code_cleanliness_workspace needs to be deleted
    exit 1
fi
"${ECLIPSE}" \
    -consolelog -nosplash -application org.eclipse.jdt.core.JavaCodeFormatter \
    -config $CDTDIR/$COREPROJECT/.settings/org.eclipse.jdt.core.prefs \
    $PWD -data check_code_cleanliness_workspace
rm -rf check_code_cleanliness_workspace
