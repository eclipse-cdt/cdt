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

# This script calls all the sub-scripts that do code cleanups

set -e

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
${DIR}/do_format_code.sh
${DIR}/do_remove_trailing_whitespace.sh
${DIR}/do_add_all_file_types_to_gitattributes.sh
${DIR}/do_project_settings.sh
${DIR}/do_rebuild_natives.sh
${DIR}/do_fix_file_permissions.sh
