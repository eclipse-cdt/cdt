#!/bin/bash

set -e

##
# Remove trailing whitespace.
# The .gitattributes is used as a filter to identify files to check. Patters with
# this "# check trailing whitespace" on the line before are checked
##
awk '/# remove trailing whitespace/{getline; print $1}' .gitattributes |
    while read i ; do
        echo "Removing trailing whitespace on $i files"
        git ls-files -- "$i" | xargs --no-run-if-empty sed -i 's/[ \t]*$//'
    done
