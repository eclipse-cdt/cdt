#!/bin/bash

set -e

##
# The script is shared by all the CDT projects. When running
# local edits, you can, for example, do:
# SCRIPT_URL=file:///scratch/eclipse/src/cdt/cdt-infra/scripts/check_code_cleanliness.sh ./check_code_cleanliness.sh
##

: ${SCRIPT_URL:=https://raw.githubusercontent.com/eclipse-cdt/cdt-infra/master/scripts/check_code_cleanliness.sh}
export COREPROJECT=bundles/org.eclipse.launchbar.core
echo Obtaining check_code_cleanliness.sh from $SCRIPT_URL
curl -sL $SCRIPT_URL | bash
