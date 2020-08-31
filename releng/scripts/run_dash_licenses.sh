#!/bin/bash
###############################################################################
# Copyright (c) 2020 Kichwa Coders Canada Inc and others.
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################

set -e

# This script is used to check the current status of all CDT dependencies to make sure no
# CQs need to be filed.


echo Generate the list of dependencies according to Maven
mvn package dependency:list -DappendOutput=true -DoutputFile=$PWD/deps-raw.log  >mvn-dependency-list.log 2>&1
echo Clean out the lines and whitespace that are not actually dependencies
cat deps-raw.log | grep -v "The following files have been resolved" | grep -v "   none" | grep -Poh '[^ ]*' > deps-stripped.log
echo Sort and uniqify and store all deps in deps.log that will be passed to the tool
cat deps-stripped.log | sort -u > deps.log

if test -e dash-licenses; then
    echo Pull and build Dash Licenses
    (cd dash-licenses && git pull)  >git-pull.log  2>&1
else
    echo Clone and build Dash Licenses
    git clone https://github.com/eclipse/dash-licenses.git >git-clone.log  2>&1
fi
mvn -f dash-licenses clean package >dash-build.log  2>&1

echo Run the license check
exit_code=0
if ! java -jar dash-licenses/target/org.eclipse.dash.licenses-*-SNAPSHOT.jar deps.log >dash-licenses.log  2>&1; then
    echo Some licenses appear to have failed a check, review output below.
    exit_code=1
fi

echo See *.log for various outputs. The key one is dash-licenses.log which is the summary and repeated below:
echo ============ dash-licenses.log ============
cat dash-licenses.log

exit $exit_code
