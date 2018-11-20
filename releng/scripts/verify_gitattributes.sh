#!/bin/bash

# Print out how many of each file extension there is
# find . ! -path "./.git/*" -type f -printf "%f\n"  | sed -E -e 's/.+\./\*./'  | sort -u | while read i; do find . ! -path "./.git/*" -name $i | wc -l | tr -d '\n'; echo " : $i" ; done | sort -n

# Print out all the unique file extensions, including unique names with no extension
# Each of these should be in .gitattributes
# find . ! -path "./.git/*" -type f -printf "%f\n"  | sed -E -e 's/.+\./\*./'  | sort -u

find . ! -path "./.git/*" -type f -printf "%f\n"  | sed -E -e 's/.+\./\\\*\\./'  | sort -u | while read i
do
    echo -n "Checking $i in .gitattributes: "
    if grep "^$i " .gitattributes
    then
        echo "Found"
    else
        echo MISSING $i in .gitattributes. List of file:
        find . ! -path "./.git/*" -type f -name "$i"
        exit 1
    fi
done

