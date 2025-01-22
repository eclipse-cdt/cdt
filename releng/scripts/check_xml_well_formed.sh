#!/bin/bash
###############################################################################
# Copyright (c) 2020, 2025 Kichwa Coders Canada Inc and others.
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################

set -eu

exit_code=0
while read line; do
    if ! xmllint $line > /dev/null; then
        echo $line has badly formed XML;
        exit_code=1
    fi
done <<<$(git ls-files '**/*.xml')

exit ${exit_code}
