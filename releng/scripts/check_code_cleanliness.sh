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
# Check the features are all branded
##
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
${DIR}/check_features.sh

##
# The next set of scripts automatically apply formatting and other rules
# to CDT. At the end of this, git repo is checked for no diffs.
##
${DIR}/do_all_code_cleanups.sh

##
# Check that none of the above caused any changes
##
if test -z "$(git status -s)"; then
    echo "Tree looks clean!"
else
    echo "Tree is dirty - something needs to be cleaned up in your commit (more info below)"
    echo "Result of git status"
    git status
    echo "Result of git diff"
    git diff
    echo "Tree is dirty - something needs to be cleaned up in your commit (see above for git status/diff). The 'something'"
    echo "is likely a misformatted file, extra whitespace at end of line, or something similar. The diff above"
    echo "shows what changes you need to apply to your patch to get it past the code cleanliness check."
    exit 1
fi

##
# Error out if there are dependencies that are not allowed in the dlls, exes, sos
##
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
echo "Checking dependencies of all .dll, .exe and .so files in CDT to make"
echo "sure no dependencies on unexpected or newer libraries are accidentally"
echo "introduced."
${DIR}/check_dll_dependencies.sh
${DIR}/check_glibc_dependencies.sh


##
# Make sure all versions have been bumped appropriately compared to the baseline
##
logfile=baseline-compare-and-replace.log
echo "Running 'mvn verify -P baseline-compare-and-replace' to make sure all versions"
echo "have been appropriately incremented. The check output is very verbose, so it is"
echo "redirected to ${logfile} which is archived as part of the build artifacts."
if ${MVN:-mvn} \
        clean verify -B -V \
        -DskipDoc=true \
        -DskipTests=true \
        -P baseline-compare-and-replace >${logfile} 2>&1; then
    echo "Maven check all versions have been bumped appropriately appears to have completed successfully"
else
    if grep "Only qualifier changed" ${logfile} > /dev/null; then
        bundle=$(grep "Only qualifier changed" ${logfile} | sed -e 's/^.*Only qualifier changed for .//' -e 's@/.*@@')
        echo "Bundle '${bundle}' is missing a service segment version bump"
        echo "Please bump service segment by 100 if on master branch"
        echo "The log of this build is part of the artifacts"
        echo "See: https://wiki.eclipse.org/Version_Numbering#When_to_change_the_service_segment"
    elif grep "baseline and build artifacts have same version but different contents" ${logfile} > /dev/null; then
        bundle=$(grep "baseline and build artifacts have same version but different contents" ${logfile} | sed -e 's/^.* on project //' -e 's@: baseline@@')
        echo "Bundle '${bundle}' has same version as baseline, but different contents"
        echo "This can happen for a variety of reasons:"
        echo "  - The comparison filters in the root pom.xml are not working"
        echo "  - Different versions of Java are being used to compile compared to the baseline"
        echo "  - A dependency has changed causing the generated classes to be different"
        echo "The log of this build is part of the artifacts"
        echo "Please bump service segment by 100 if on master branch"
        echo "See: https://wiki.eclipse.org/Version_Numbering#When_to_change_the_service_segment"
    else
        echo "Maven 'check all versions have been bumped appropriately' failed! Please see the"
        echo "log of the failed maven run which is available as part of the artifacts in a"
        echo "file called baseline-compare-and-replace.log"
    fi
    exit 1
fi
