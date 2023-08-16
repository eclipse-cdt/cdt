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
# Make sure all versions have been bumped appropriately compared to the baseline
##
logfile=baseline-compare-and-replace.log
bundles_only_qualifier_changed=$(grep "Only qualifier changed" ${logfile} | sed -e 's/^.*Only qualifier changed for .//' -e 's@/.*@@' | sort)
if [ -n "$bundles_only_qualifier_changed" ]; then
    echo "The following bundles are missing a service segment version bump:"
    for bundle in $bundles_only_qualifier_changed; do
        echo "  - $bundle"
    done
    echo "Please bump service segment by 100 if on main branch"
    echo "The log of this build is above"
    echo "See: https://wiki.eclipse.org/Version_Numbering#When_to_change_the_service_segment"
    echo
fi

bundles_same_version_different_content=$(grep "baseline and build artifacts have same version but different contents" ${logfile} | sed -e 's/^.* on project //' -e 's@: baseline.*@@' | sort)
if [ -n "$bundles_same_version_different_content" ]; then
    echo "The following bundles have same version as baseline, but different contents:"
    for bundle in $bundles_same_version_different_content; do
        echo "  - $bundle"
    done
    echo "This can happen for a variety of reasons:"
    echo "  - The comparison filters in the root pom.xml are not working"
    echo "  - Different versions of Java are being used to compile compared to the baseline"
    echo "  - A dependency has changed causing the generated classes to be different"
    echo "The log of this build is above"
    echo "Please bump service segment by 100 if on main branch"
    echo "See: https://wiki.eclipse.org/Version_Numbering#When_to_change_the_service_segment"
    echo
fi

success=$(grep "SUCCESS - Maven check all versions have been bumped appropriately appears to have completed successfully" ${logfile})
if [ -n "$success" ]; then
    echo "Maven check all versions have been bumped appropriately appears to have completed successfully"
elif [ -z "$bundles_only_qualifier_changed" ] && [ -z "$bundles_same_version_different_content" ]; then
    echo "Maven 'check all versions have been bumped appropriately' failed! Please see the"
    echo "log of the failed maven run above"
    exit 1
fi
