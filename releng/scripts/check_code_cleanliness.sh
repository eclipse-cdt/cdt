#!/bin/bash

set -e

##
# Check the features are all branded
##
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
${DIR}/check_features.sh

##
# The script is shared by all the CDT projects. When running
# local edits, you can, for example, do:
# SCRIPT_URL=file:///scratch/eclipse/src/cdt/cdt-infra/scripts/check_code_cleanliness.sh ./check_code_cleanliness.sh
##

: ${SCRIPT_URL:=https://raw.githubusercontent.com/eclipse-cdt/cdt-infra/master/scripts/check_code_cleanliness.sh}
export COREPROJECT=core/org.eclipse.cdt.core
echo Obtaining check_code_cleanliness.sh from $SCRIPT_URL
curl -sL $SCRIPT_URL | bash
