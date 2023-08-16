#!/bin/bash
###############################################################################
# Copyright (c) 2018, 2023 Kichwa Coders Ltd and others.
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################

set -e
set -o pipefail

if test ! -z "$(git status -s -uno)"; then
    echo "You have changes. Please stash them before continuing."
    exit 1
fi

##
# Make sure all versions have been bumped appropriately compared to the baseline
##
logfile=baseline-compare-and-replace.log
echo "Running 'mvn verify -P baseline-compare-and-replace' to make sure all versions"
echo "have been appropriately incremented."


if ${MVN:-mvn} \
        clean verify -B -V --fail-at-end \
        -DskipDoc=true \
        -DskipTests=true \
        -P baseline-compare-and-replace \
        -P api-baseline-check \
         2>&1 | tee ${logfile}; then
    echo "SUCCESS - Maven check all versions have been bumped appropriately appears to have completed successfully"
    echo "SUCCESS - Maven check all versions have been bumped appropriately appears to have completed successfully" >> ${logfile}
else
    echo "FAILED - Maven check all versions have been bumped appropriately appears to have failed. See the report in the next build step."
    echo "FAILED - Maven check all versions have been bumped appropriately appears to have failed. See the report in the next build step." >> ${logfile}
fi
