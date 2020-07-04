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
    natures=$(xmllint --xpath 'string(//projectDescription/natures)' $i)
    mkdir -p $d/.settings

    # JDT
    if [[ $natures == *"org.eclipse.jdt.core.javanature"* ]]; then
        cp $COREPROJECT/.settings/org.eclipse.jdt.* $d/.settings
        # For test plug-ins we are more lenient so don't warn on some items
        if echo $i | grep -E '\.tests?[/\.]' > /dev/null; then
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
        fi
        if echo $i | grep 'org.eclipse.cdt.examples.dsf' > /dev/null; then
            sed -i \
                '-es@org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=warning@org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=ignore@' \
                $d/.settings/org.eclipse.jdt.core.prefs
        fi
    else
        rm -f $d/.settings/org.eclipse.jdt*.prefs
    fi

    # PDE
    if [[ $natures == *"org.eclipse.pde.PluginNature"* ]]; then
        cp $COREPROJECT/.settings/org.eclipse.pde.prefs $d/.settings
        cp $COREPROJECT/.settings/org.eclipse.pde.api.tools.prefs $d/.settings
        if echo $i | grep -E '\.tests?[/\.]' > /dev/null; then
            sed -i \
                '-es@compilers.p.not-externalized-att=1@compilers.p.not-externalized-att=2@' \
                $d/.settings/org.eclipse.pde.prefs
        fi
    else
        rm -f $d/.settings/org.eclipse.pde*.prefs
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
# Mark Windows binaries as executable
##
echo "Marking Windows binaries as executable"
git ls-files -- \*.exe \*.dll | while read line; do
    chmod +x "$line"
done

##
# Check that none of the above caused any changes
##
if test -z "$(git status -s)"; then
    echo "Tree looks clean!"
else
    echo "Tree is dirty - something needs to be cleaned up in your commit (more info below)"
    echo "Result of git status"
    git status
    echo "Result of git diff"
    git diff
    echo "Tree is dirty - something needs to be cleaned up in your commit (see above for git status/diff). The 'something'"
    echo "is likely a misformatted file, extra whitespace at end of line, or something similar. The diff above"
    echo "shows what changes you need to apply to your patch to get it past the code cleanliness check."
    exit 1
fi

##
# Make sure all versions have been bumped appropriately compared to the baseline
##
logfile=baseline-compare-and-replace.log
echo "Running 'mvn verify -P baseline-compare-and-replace' to make sure all versions"
echo "have been appropriately incremented. The check output is very verbose, so it is"
echo "redirected to ${logfile} which is archived as part of the build artifacts."
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
        echo "Maven 'check all versions have been bumped appropriately' failed! Please see the"
        echo "log of the failed maven run which is available as part of the artifacts in a"
        echo "file called baseline-compare-and-replace.log"
    fi
    exit 1
fi
