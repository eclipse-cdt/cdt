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

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
${DIR}/check_code_cleanliness_only.sh
${DIR}/check_bundle_versions.sh
${DIR}/check_bundle_versions_report.sh
