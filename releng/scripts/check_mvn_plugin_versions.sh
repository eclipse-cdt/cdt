#!/bin/bash
###############################################################################
# Copyright (c) 2015, 2017 Ericsson, EfficiOS Inc. and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Marc-André Laperle - Initial version
#     Alexandre Montplaisir - Initial version
#     Marc-André Laperle - Copied to CDT
###############################################################################

# Point ourselves to the script's directory (so it can be run "out-of-tree")
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
output=`mvn versions:display-plugin-updates -U -f $DIR/../../pom.xml`

#filter only updates and show unique
summary=`echo "${output}" | grep "\\->" | sort | uniq`
echo -e "Summary:\n${summary}"

#remove empty lines and count lines
outdatedNb=`echo "${summary}" | sed '/^\s*$/d' | wc -l`
echo Number of outdated plugins: "${outdatedNb}"
exit ${outdatedNb}
