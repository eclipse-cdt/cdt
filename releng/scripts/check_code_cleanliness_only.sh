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

if test ! -z "$(git status -s -uno)"; then
    echo "You have changes. Please stash them before continuing."
    exit 1
fi

##
# Check the features are all branded and all content has proper licenses
##
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
${DIR}/check_features.sh
${DIR}/check_license.sh

##
# The next set of scripts automatically apply formatting and other rules
# to CDT. At the end of this, git repo is checked for no diffs.
##
${DIR}/do_all_code_cleanups.sh

##
# Check that none of the above caused any changes
##
if test -z "$(git status -s -uno)"; then
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
# Error out if some XML files are badly formed
##
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
${DIR}/check_xml_well_formed.sh
