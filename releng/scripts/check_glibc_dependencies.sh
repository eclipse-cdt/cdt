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


###
# Check that all .so files in CDT for a given ${ARCH} (using ${PREFIX} toolchain) 
# use glibc symbols no greater than ${ALLOWED_GLIBC_VERSION} and depend on
# no libs other than ${ALLOWED_LIBS}
function check {
    ARCH=$1; shift
    PREFIX=$1; shift
    ALLOWED_GLIBC_VERSION=$1; shift
    ALLOWED_LIBS="$@"; shift
    ret_code=0
    while read line; do
        ${PREFIX}-linux-gnu-readelf -d ${line} | grep -E '\(NEEDED\)' | while read needed; do
            needed=${needed//*Shared library: [/}
            needed=${needed//]*/}
            if [[ ! " ${ALLOWED_LIBS} " =~ " ${needed} " ]]; then
                echo "ERROR: $line has illegal dependency of ${needed}"
                ret_code=1
            fi
        done

        # The way the version check is done is that all symbol version info is extracted
        # from relocations match @GLIBC_*, the versions are sorted with the max
        # allowed version added to the list too. And then we check the last entry in
        # the list to make sure it is == to max allowed version.
        ${PREFIX}-linux-gnu-objdump -R ${line} | grep @GLIBC_ | while read version; do
            echo ${version//*@GLIBC_}
        done > /tmp/version_check
        echo ${ALLOWED_GLIBC_VERSION} >> /tmp/version_check
        max_version_in_use=$(cat /tmp/version_check | sort --unique --version-sort | tail -n1)
        if [ "$max_version_in_use" != "$ALLOWED_GLIBC_VERSION" ]; then
            echo "ERROR: $line has dependency on glibc greater than allowed version of ${ALLOWED_GLIBC_VERSION} for at least the following symbols"
            # This only lists greatest version number symbols
            ${PREFIX}-linux-gnu-objdump -R ${line} | grep @GLIBC_${max_version_in_use}
            ret_code=1
        fi
    done <<<$(git ls-files **/linux/${ARCH}/\*.so)
    return ${ret_code}
}


exit_code=0
# This is the current set of allowed so dependencies for CDT code. Additional entries here are permitted,
# provided they are found on all Linux machines by default.
check aarch64 aarch64 2.17 libc.so.6 ld-linux-aarch64.so.1 || exit_code=1
check x86_64 x86_64 2.4 libc.so.6 || exit_code=1
check ppc64le powerpc64le 2.17 libc.so.6 || exit_code=1

exit ${exit_code}
