#!/bin/bash

set -e

##
# Check the features are all branded
##
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
${DIR}/check_features.sh

#!/bin/bash

##
# This script is reused by other projects, if so, COREPROJECT should be set
# to the project to use a basis for project settings
##
: ${COREPROJECT:=core/org.eclipse.cdt.core}

##
# Format code
##
: ${ECLIPSE:=~/buildtools/eclipse-SDK-4.13/eclipse}
test ! -e check_code_cleanliness_workspace
${ECLIPSE} \
    -consolelog -nosplash -application org.eclipse.jdt.core.JavaCodeFormatter \
    -config $PWD/$COREPROJECT/.settings/org.eclipse.jdt.core.prefs \
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
        git ls-files -- "$i" | xargs --no-run-if-empty sed -i 's/[ \t]*$//'
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
git ls-files  -- \*\*/.project ":!$COREPROJECT/.project" | while read i ; do
    d=`dirname $i`;
    if test ! -e $d/feature.xml; then
        mkdir -p $d/.settings
        cp $COREPROJECT/.settings/org.eclipse.jdt.* $d/.settings
        cp $COREPROJECT/.settings/org.eclipse.pde.prefs $d/.settings
        cp $COREPROJECT/.settings/org.eclipse.pde.api.tools.prefs $d/.settings        
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
# Make sure that natives are up to date
##
if test -e native/org.eclipse.cdt.native.serial/jni; then
    echo "Rebuilding natives to make sure they match source"
    logfile=$(mktemp /tmp/make-natives-log.XXXXXX)
    if ! make -C native/org.eclipse.cdt.native.serial/jni rebuild >${logfile} 2>&1; then
        echo "Rebuilding of natives failed. The log is part of the artifacts of the build"
        cp ${logfile} make-natives.log
        exit 1
    fi
fi

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

##
# Make sure all versions have been bumped appropriately compared to the baseline
##
echo "Running 'mvn verify -P baseline-compare-and-replace' to make sure all versions have been appropriately incremented"
logfile=$(mktemp /tmp/baseline-compare-and-replace.XXXXXX)
if ${MVN:-mvn} \
        clean verify -B -V \
        -DskipDoc=true \
        -DskipTests=true \
        -P baseline-compare-and-replace >${logfile} 2>&1; then
    echo "Maven check all versions have been bumped appropriately appears to have completed successfully"
else
    if grep "Only qualifier changed" ${logfile} > /dev/null; then
        bundle=$(grep "Only qualifier changed" ${logfile} | sed -e 's/^.*Only qualifier changed for .//' -e 's@/.*@@')
        echo "Bundle '${bundle}' is missing a service segment version bump"
        echo "Please bump service segment by 100 if on master branch"
        echo "The log of this build is part of the artifacts"
        echo "See: https://wiki.eclipse.org/Version_Numbering#When_to_change_the_service_segment"
    elif grep "baseline and build artifacts have same version but different contents" ${logfile} > /dev/null; then
        bundle=$(grep "baseline and build artifacts have same version but different contents" ${logfile} | sed -e 's/^.* on project //' -e 's@: baseline@@')
        echo "Bundle '${bundle}' has same version as baseline, but different contents"
        echo "This can happen for a variety of reasons:"
        echo "  - The comparison filters in the root pom.xml are not working"
        echo "  - Different versions of Java are being used to compile compared to the baseline"
        echo "  - A dependency has changed causing the generated classes to be different"
        echo "The log of this build is part of the artifacts"
        echo "Please bump service segment by 100 if on master branch"
        echo "See: https://wiki.eclipse.org/Version_Numbering#When_to_change_the_service_segment"
    else
        echo "Maven 'check all versions have been bumped appropriately' failed!"
        echo "The log of this build is part of the artifacts"
    fi
    cp ${logfile} baseline-compare-and-replace.log
    exit 1
fi
