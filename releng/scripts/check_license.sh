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

set -u # run with unset flag error so that missing parameters cause build failure
set -e # error out on any failed commands
# set -x # echo all commands used for debugging purposes

# Point ourselves to the script's directory (so it can be run "out-of-tree")
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

##
# This script is reused by other projects, if so, COREPROJECT should be set
# to the project to use a basis for project settings
##
: ${COREPROJECT:=core/org.eclipse.cdt.core}

tmpfile=$(mktemp /tmp/check_license.XXXXXX)
git ls-files  -- \*\*/.project | while read i ; do
    d=$(dirname $i);
    natures=$(xmllint --xpath 'string(//projectDescription/natures)' $i)

    if [[ $natures == *"org.eclipse.pde.PluginNature"* ]]; then
        build_properties=$d/build.properties
        about_html=$d/about.html

        cat $build_properties | sed -z '-es,\\\n,,g' > $tmpfile
        if [ -z "$(grep bin.includes $tmpfile | grep about.html)" ]; then
            echo "$build_properties is missing bin.includes reference to about.html"
            exit 1
        fi
        if [ -z "$(grep src.includes $tmpfile | grep about.html)" ]; then
            echo "$build_properties is missing src.includes reference to about.html"
            exit 1
        fi

        if [ ! -e "$about_html" ]; then
            echo "$about_html is missing"
            exit 1
        elif ! cmp $COREPROJECT/about.html $about_html > /dev/null ; then
            echo "$about_html differs from $COREPROJECT/about.html"
            exit 1
        fi
    fi
done
