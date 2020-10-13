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
git ls-files -- \*/feature.xml | while read feature_xml; do
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

    # echo Working on: $feature_xml 'whose plugin is' $plugin
    cp releng/templates/feature/cdt_logo_icon32.png $plugin_dir
    cp releng/templates/feature/about.mappings $plugin_dir
    if [ -e ${plugin_dir}/welcome.xml ] ; then
        cp releng/templates/feature/about.with_welcome.ini ${plugin_dir}/about.ini
    else
        cp releng/templates/feature/about.ini $plugin_dir
    fi

    case $feature_xml in
    launchbar*)
        # The git history does not show the proper start year for launchbar because of Bug 558439
        # Note, we can't simply use --follow because that causes other features to have a too
        # early copyright year.
        feature_start_year=2014
        ;;
    cmake/org.eclipse.cdt.cmake-feature/feature.xml)
        # The git history does not show the proper start year for cmake because of Bug 567488
        # Note, we can't simply use --follow because that causes other features to have a too
        # early copyright year.
        feature_start_year=2015
        ;;
    *)
        feature_start_year=$(git log --reverse --format='%ad' --date="format:%Y" -- $feature_xml $plugin_dir | head -1)
        ;;
    esac
    feature_end_year=$(git log --format='%ad' --date="format:%Y" -- $feature_xml $plugin_dir | head -1)
    feature_name=$(grep featureName= $feature_dir/feature.properties | sed '-es,featureName=,,')
    export feature_start_year feature_end_year feature_name

    envsubst '$feature_start_year $feature_end_year $feature_name'  < \
        releng/templates/feature/about.properties > \
        ${plugin_dir}/about.properties

    for f in about.ini about.mappings about.properties cdt_logo_icon32.png; do
        if ! grep $f ${plugin_dir}/build.properties > /dev/null; then
            echo "Missing $f entry in $plugin/build.properties"
            # cp ./build/org.eclipse.cdt.autotools.ui/build.properties ${plugin_dir}/build.properties
            exit 1
        fi
    done

    # Check the license and copyright settings of the feature
    if [ "$(xmllint --xpath 'string(//feature/copyright)' $feature_xml | xargs)" != "%copyright" ]; then
        echo "Copyright entry in $feature_xml should be %copyright"
        exit 1
    fi
    if [ "$(xmllint --xpath 'string(//feature/license)' $feature_xml | xargs)" != "%license" ]; then
        echo "License entry in $feature_xml should be %license"
        exit 1
    fi
    if [ "$(xmllint --xpath 'string(//feature/license/@url)' $feature_xml | xargs)" != "%licenseURL" ]; then
        echo "License URL in $feature_xml should be %licenseURL"
        exit 1
    fi
    if [ "$(xmllint --xpath 'string(//feature/@license-feature)' $feature_xml | xargs)" != "org.eclipse.license" ]; then
        echo "License feature in $feature_xml should be org.eclipse.license"
        exit 1
    fi
    if [ "$(xmllint --xpath 'string(//feature/@license-feature-version)' $feature_xml | xargs)" != "0.0.0" ]; then
        echo "License version in $feature_xml should be 0.0.0"
        exit 1
    fi
    if [ -z "$(grep -E '^copyright=' $feature_dir/feature.properties)" ]; then
        echo "Copyright entry in $feature_dir/feature.properties missing (key copyright= not found)"
        exit 1
    fi

done

