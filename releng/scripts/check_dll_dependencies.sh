#!/bin/bash
###############################################################################
# Copyright (c) 2020, 2024 Kichwa Coders Canada Inc and others.
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################

set -eu

SCRIPT=$( basename "${BASH_SOURCE[0]}" )

if ! command -v x86_64-w64-mingw32-objdump &> /dev/null
then
    echo "WARNING: Skipping ${SCRIPT} because mingw cross compiler tools are not available"
    exit 0
fi


###
# Check that all .dll/.exe files in CDT for a given ${ARCH} (using ${PREFIX} toolchain)
function check {
    ARCH=$1; shift
    PREFIX=$1; shift
    ALLOWED_DLLS="$@"; shift
    ALLOWED_DLLS=${ALLOWED_DLLS^^} # change to uppercase
    ret_code=0

    while read line; do
        while read import; do
            dllname=${import//DLL Name: /}
            dllname_upper=${dllname^^}
            if [[ ! " ${ALLOWED_DLLS} " =~ " ${dllname_upper} " ]]; then
                echo "ERROR: $line has illegal import of ${dllname}"
                exit_code=1
            fi
        done <<<$(${PREFIX}-w64-mingw32-objdump -p $line | grep "DLL Name")
    done <<<$(git ls-files -- **/win32/${ARCH}/\*.exe **/win32/${ARCH}/\*.dll)
    return ${ret_code}
}

exit_code=0
# This is the current set of allowed so dependencies for CDT code. Additional entries here are permitted,
# provided they are found on all Windows machines by default.
dlls="kernel32.dll msvcrt.dll user32.dll psapi.dll shell32.dll advapi32.dll winpty.dll"
# The newer style api- dlls are listed separately because of as of this writing only
# the aarch64 has these references listed.
apidlls="api-ms-win-crt-runtime-l1-1-0.dll \
    api-ms-win-crt-runtime-l1-1-0.dll \
    api-ms-win-crt-stdio-l1-1-0.dll \
    api-ms-win-crt-time-l1-1-0.dll \
    api-ms-win-crt-heap-l1-1-0.dll \
    api-ms-win-crt-private-l1-1-0.dll \
    api-ms-win-crt-utility-l1-1-0.dll \
    api-ms-win-crt-string-l1-1-0.dll \
    api-ms-win-crt-convert-l1-1-0.dll \
    api-ms-win-crt-environment-l1-1-0.dll \
    api-ms-win-crt-filesystem-l1-1-0.dll \
    api-ms-win-crt-locale-l1-1-0.dll \
    api-ms-win-crt-math-l1-1-0.dll"
check aarch64 aarch64 ${dlls} ${apidlls} || exit_code=1
check x86_64 x86_64 ${dlls} || exit_code=1
exit ${exit_code}
