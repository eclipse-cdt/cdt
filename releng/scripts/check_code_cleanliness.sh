#!/bin/bash

###
# This script is run automatically as part of gerrit validation jobs
# to ensure that coding standards have been followed. It can also be
# used to make code follow standards again.
#
# The overall design is to apply a number of fixes (formatting, trim
# whitespace, etc) and then check if there are any modifications
# in git.
###

set -e

##
# Format code
##
test ! -e check_code_cleanliness_workspace
/scratch/eclipse/eclipse-committers-2018-12-M2/eclipse \
    -consolelog -nosplash -application org.eclipse.jdt.core.JavaCodeFormatter \
    -config $PWD/core/org.eclipse.cdt.core/.settings/org.eclipse.jdt.core.prefs \
    $PWD -data check_code_cleanliness_workspace
rm -rf check_code_cleanliness_workspace

##
# Remove trailing whitespace.
# The .gitattributes is used as a filter to identify files to check. Patters with
# this "# check trailing whitespace" on the line before are checked
##
awk '/# remove trailing whitespace/{getline; print $1}' .gitattributes | \
    while read i ; do \
        echo "Removing trailing whitespace on $i files"
        find .  ! -path "./.git/*" -type f -name "$i" -exec sed -i 's/[ \t]*$//' {} +; \
    done

##
# Add all file types to .gitattributes
##
find . ! -path "./.git/*" -type f -printf "%f\n"  | sed -E -e 's/.+\./\\\*\\./'  | sort -u | while read i ; do
    if ! grep "^$i " .gitattributes > /dev/null
    then
        echo "MISSING $i in .gitattributes, adding as text, check if that is correct"
        echo "$i text # automatically added - please verify" >> .gitattributes
    fi
done

##
# Copy JDT/PDE preferences
##
find . ! -path "./.git/*" -name .project ! -path './core/org.eclipse.cdt.core/.project' | while read i ; do
    d=`dirname $i`;
    if test ! -e $d/feature.xml; then
        mkdir -p $d/.settings
        cp core/org.eclipse.cdt.core/.settings/org.eclipse.jdt.* core/org.eclipse.cdt.core/.settings/org.eclipse.pde.* $d/.settings
        # For test plug-ins, don't warn on missing NLS
        if echo $i | grep '\.tests[/\.]' > /dev/null; then
            sed -i '-es@org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=warning@org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=ignore@' $d/.settings/org.eclipse.jdt.core.prefs
        fi
    fi
done

##
# Check that none of the above caused any changes
##
if test -z "$(git status -s)"; then
    echo "Tree looks clean!"
else
    echo "Tree is dirty - something needs to be cleaned up in your commit"
    echo "Result of git status"
    git status
    echo "Result of git diff"
    git diff
    echo "Tree is dirty - something needs to be cleaned up in your commit (see above for git status/diff)"
fi
