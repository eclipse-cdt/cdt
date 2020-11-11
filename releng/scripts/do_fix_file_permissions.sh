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
# Remove executable permission on all files in the repository.
##
git ls-files | xargs --no-run-if-empty chmod -x

##
# Allow certain file types to have execute permission.
# The .gitattributes is used as a filter to identify files to allow executable
# permissions on. Patterns with this "# file permission +x" on the line before
# are considered (lines in .gitattributes starting with '#' are ignored).
##
awk '/# file permission \+x/{do getline; while ($0 ~ /^#/); print $1}' .gitattributes |
    while read i ; do
        echo "Restoring permissions for $i files"
        git ls-files -- "$i" | xargs --no-run-if-empty git checkout
    done
