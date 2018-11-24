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
: ${ECLIPSE:=~/buildtools/eclipse-SDK-4.9/eclipse}
test ! -e check_code_cleanliness_workspace
${ECLIPSE} \
    -consolelog -nosplash -application org.eclipse.jdt.core.JavaCodeFormatter \
    -config $PWD/core/org.eclipse.cdt.core/.settings/org.eclipse.jdt.core.prefs \
    $PWD -data check_code_cleanliness_workspace
rm -rf check_code_cleanliness_workspace

##
# Remove trailing whitespace.
# The .gitattributes is used as a filter to identify files to check. Patters with
# this "# check trailing whitespace" on the line before are checked
##
awk '/# remove trailing whitespace/{getline; print $1}' .gitattributes |
    while read i ; do
        echo "Removing trailing whitespace on $i files"
        git ls-files -- "$i" | xargs sed -i 's/[ \t]*$//'
    done

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

##
# Copy JDT/PDE preferences
##
git ls-files  -- \*\*/.project ':!core/org.eclipse.cdt.core/.project' | while read i ; do
    d=`dirname $i`;
    if test ! -e $d/feature.xml; then
        mkdir -p $d/.settings
        cp core/org.eclipse.cdt.core/.settings/org.eclipse.jdt.* core/org.eclipse.cdt.core/.settings/org.eclipse.pde.* $d/.settings
        # For test plug-ins we are more lenient so don't warn on some items
        if echo $i | grep '\.tests[/\.]' > /dev/null; then
            sed -i \
                '-es@org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=warning@org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=ignore@' \
                '-es@org.eclipse.jdt.core.compiler.problem.discouragedReference=warning@org.eclipse.jdt.core.compiler.problem.discouragedReference=ignore@' \
                '-es@org.eclipse.jdt.core.compiler.problem.deprecation=warning@org.eclipse.jdt.core.compiler.problem.deprecation=ignore@' \
                '-es@org.eclipse.jdt.core.compiler.problem.discouragedReference=warning@org.eclipse.jdt.core.compiler.problem.discouragedReference=ignore@' \
                '-es@org.eclipse.jdt.core.compiler.problem.emptyStatement=warning@org.eclipse.jdt.core.compiler.problem.emptyStatement=ignore@' \
                '-es@org.eclipse.jdt.core.compiler.problem.fieldHiding=warning@org.eclipse.jdt.core.compiler.problem.fieldHiding=ignore@' \
                '-es@org.eclipse.jdt.core.compiler.problem.finalParameterBound=warning@org.eclipse.jdt.core.compiler.problem.finalParameterBound=ignore@' \
                '-es@org.eclipse.jdt.core.compiler.problem.forbiddenReference=error@org.eclipse.jdt.core.compiler.problem.forbiddenReference=warning@' \
                '-es@org.eclipse.jdt.core.compiler.problem.uncheckedTypeOperation=warning@org.eclipse.jdt.core.compiler.problem.uncheckedTypeOperation=ignore@' \
                '-es@org.eclipse.jdt.core.compiler.problem.unusedLocal=warning@org.eclipse.jdt.core.compiler.problem.unusedLocal=ignore@' \
                '-es@org.eclipse.jdt.core.compiler.problem.unusedPrivateMember=warning@org.eclipse.jdt.core.compiler.problem.unusedPrivateMember=ignore@' \
                '-es@org.eclipse.jdt.core.compiler.problem.potentialNullReference=warning@org.eclipse.jdt.core.compiler.problem.potentialNullReference=ignore@' \
                '-es@org.eclipse.jdt.core.compiler.problem.rawTypeReference=warning@org.eclipse.jdt.core.compiler.problem.rawTypeReference=ignore@' \
                '-es@org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch=warning@org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch=ignore@' \
                $d/.settings/org.eclipse.jdt.core.prefs
            sed -i \
                '-es@compilers.p.not-externalized-att=1@compilers.p.not-externalized-att=2@' \
                $d/.settings/org.eclipse.pde.prefs
        fi
        if echo $i | grep 'org.eclipse.cdt.examples.dsf' > /dev/null; then
            sed -i \
                '-es@org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=warning@org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=ignore@' \
                $d/.settings/org.eclipse.jdt.core.prefs
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
    exit 1
fi
