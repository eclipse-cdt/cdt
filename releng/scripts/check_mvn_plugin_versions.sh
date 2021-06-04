#!/bin/bash
###############################################################################
# Copyright (c) 2015, 2019 Ericsson, EfficiOS Inc. and others
#
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#     Marc-André Laperle - Initial version
#     Alexandre Montplaisir - Initial version
#     Marc-André Laperle - Copied to CDT
###############################################################################

set -u # run with unset flag error so that missing parameters cause build failure
set -e # error out on any failed commands
set -x # echo all commands used for debugging purposes

# Point ourselves to the script's directory (so it can be run "out-of-tree")
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
output="$(mktemp /tmp/check_mvn_plugin_versions.output.XXXXXX)"
${MVN:-mvn} versions:display-plugin-updates -P build-standalone-debugger-rcp -U -B -f $DIR/../../pom.xml | tee $output

#filter only updates and show unique
# XXX: Jonah added the exclusion for sonar-maven-plugin as Eclipse's SonarQube installation is not new enough
# XXX: see https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven#AnalyzingwithSonarQubeScannerforMaven-Compatibility
summary=`cat $output | grep "\\->" | grep -v "org.codehaus.mojo:sonar-maven-plugin" | sort | uniq`
echo -e "Summary:\n${summary}"

#remove empty lines and count lines
outdatedNb=`echo "${summary}" | sed '/^\s*$/d' | wc -l`
echo Number of outdated plugins: "${outdatedNb}"
exit ${outdatedNb}
