#!/bin/bash
###############################################################################
# Copyright (c) 2025 Kichwa Coders Canada Inc
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################

set -e

SCRIPT=$( basename "${BASH_SOURCE[0]}" )

##
# Make sure that asciidocs are up to date
##
for p in doc/org.eclipse.cdt.doc.user; do
    echo "Ensure adoc files start with expected contents"
    ref_header=$p/adoc-headers.txt
    git ls-files -- $p/**/*.adoc | while read i ; do
        end_line=$(awk '/\/\/ ENDOFHEADER/{ print NR + 1; exit }' $i)
        tmpfile=$(mktemp /tmp/adoc.XXXXXX)
        cat $ref_header > $tmpfile
        tail --lines=+${end_line:=0} $i >> $tmpfile
        mv -f $tmpfile $i
    done
done
