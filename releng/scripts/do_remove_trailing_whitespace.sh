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
# Remove trailing whitespace.
# The .gitattributes is used as a filter to identify files to check. Patters with
# this "# check trailing whitespace" on the line before are checked
##
awk '/# remove trailing whitespace/{getline; print $1}' .gitattributes |
    while read i ; do
        echo "Removing trailing whitespace on $i files"
        git ls-files -- "$i" | xargs --no-run-if-empty sed -i 's/[ \t]*$//'
    done
