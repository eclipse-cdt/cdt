#!/bin/bash

set -e

##
# This script is reused by other projects, if so, COREPROJECT should be set
# to the project to use a basis for project settings
##
: ${COREPROJECT:=core/org.eclipse.cdt.core}

##
# Format code
##
: ${ECLIPSE:=~/buildtools/eclipse-SDK-4.13/eclipse}
if test -e check_code_cleanliness_workspace; then
    echo check_code_cleanliness_workspace needs to be deleted
    exit 1
fi
${ECLIPSE} \
    -consolelog -nosplash -application org.eclipse.jdt.core.JavaCodeFormatter \
    -config $PWD/$COREPROJECT/.settings/org.eclipse.jdt.core.prefs \
    $PWD -data check_code_cleanliness_workspace
rm -rf check_code_cleanliness_workspace
