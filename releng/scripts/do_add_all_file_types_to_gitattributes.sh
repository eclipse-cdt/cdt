#!/bin/bash

set -e

##
# Add all file types to .gitattributes
##
git ls-files | sed -E '-es@^.*/([^/]+)$@\1@' '-es@.+\.@\\\*\\.@'  | sort -u | while read i ; do
    if ! grep "^$i " .gitattributes > /dev/null
    then
        echo "MISSING $i in .gitattributes, adding as text, check if that is correct"
        echo "$i text # automatically added - please verify" >> .gitattributes
    fi
done
