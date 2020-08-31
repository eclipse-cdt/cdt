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

set -eu

# This is the current set of allowed DLL dependencies for CDT code. Additional entries here are permitted,
# provided they are found on all Windows machines by default.
ALLOWED_DLLS="KERNEL32.DLL MSVCRT.DLL USER32.DLL PSAPI.DLL SHELL32.DLL ADVAPI32.DLL"
# In addition, the WINPTY.DLL is something CDT ships so is allowed to be a dependency
ALLOWED_DLLS+=" WINPTY.DLL"

exit_code=0
while read line; do
    while read import; do
        dllname=${import//DLL Name: /}
        dllname_upper=${dllname^^}
        if [[ ! " ${ALLOWED_DLLS} " =~ " ${dllname_upper} " ]]; then
            echo "ERROR: $line has illegal import of ${dllname}"
            exit_code=1
        fi
    done <<<$(x86_64-w64-mingw32-objdump -p $line | grep "DLL Name")
done <<<$(git ls-files -- \*.exe \*.dll)

exit ${exit_code}
