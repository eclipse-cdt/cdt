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
#set -x # echo all commands used for debugging purposes

# Point ourselves to the script's directory (so it can be run "out-of-tree")
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

echo "Normalizing branding content on all features"
find * -name feature.xml -not -path */target/* | while read feature_xml; do
    plugin=$(xmllint --xpath 'string(//feature/@plugin)' $feature_xml)
    if [ -z "$plugin" ]; then
        plugin=$(xmllint --xpath 'string(//feature/@id)' $feature_xml)
    fi
    feature_dir=$(dirname ${feature_xml})

    if [ $(find * -name $plugin -type d -not -path */target/* | wc -l) -ne 1 ]; then
        echo "Script failure - did not find unique dir for $plugin part of feature $feature_dir":
        echo "This may mean the branding is missing, or not understood by the script.":
        find * -name $plugin -type d
        exit 1
    fi
    plugin_dir=$(find * -name $plugin -type d -not -path */target/*)

    feature_year=$(git log --reverse  --format='%ad' --date="format:%Y" -- $feature_xml */$plugin | head -1)
    feature_name=$(grep featureName= $feature_dir/feature.properties | sed '-es,featureName=,,')

    # echo Working on: $feature_xml 'whose plugin is' $plugin
    if [ $plugin != org.eclipse.cdt ] ; then
        cp releng/org.eclipse.cdt/cdt_logo_icon32.png $plugin_dir
        cp releng/org.eclipse.cdt/about.mappings $plugin_dir
    fi
    if [ -e ${plugin_dir}/welcome.xml ] ; then
        if [ $plugin != org.eclipse.cdt.sdk ] ; then
            cp releng/org.eclipse.cdt.sdk/about.ini $plugin_dir
        fi
    else
        if [ $plugin != org.eclipse.cdt ] ; then
            cp releng/org.eclipse.cdt/about.ini $plugin_dir
        fi
    fi

    # Note ${feature_year} is the first edit of the feature or its main bundle,
    # but the about.properties is copyright 2002 onwards as all of them have
    # been copied from the same original file
    cat > ${plugin_dir}/about.properties << EOM
###############################################################################
# Copyright (c) 2002, 2020 Contributors to the Eclipse Foundation
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################
# about.properties
# contains externalized strings for about.ini
# java.io.Properties file (ISO 8859-1 with "\" escapes)
# fill-ins are supplied by about.mappings
# This file should be translated.
# NOTE TO TRANSLATOR: Please do not translate the featureVersion variable.


blurb=${feature_name}\n\\
\n\\
Version: {featureVersion}\n\\
Build id: {0}\n\\
\n\\
Copyright (c) ${feature_year}, 2020 Contributors to the Eclipse Foundation\n\\
Visit http://www.eclipse.org/cdt
EOM

    for f in about.ini about.mappings about.properties cdt_logo_icon32.png; do
        if ! grep $f ${plugin_dir}/build.properties > /dev/null; then
            echo "Missing $f entry in $plugin/build.properties"
            # cp ./build/org.eclipse.cdt.autotools.ui/build.properties ${plugin_dir}/build.properties
            exit 1
        fi
    done

done

