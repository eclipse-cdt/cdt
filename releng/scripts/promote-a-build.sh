#!/bin/bash

set -u # run with unset flag error so that missing parameters cause build failure
set -e # error out on any failed commands
set -x # echo all commands used for debugging purposes

SSHUSER="genie.cdt@projects-storage.eclipse.org"
SSH="ssh ${SSHUSER}"
SCP="scp"
DOWNLOAD=/home/data/httpd/download.eclipse.org/tools/cdt/$RELEASE_OR_BUILD/$MINOR_VERSION/$MILESTONE
ARTIFACTS=https://ci.eclipse.org/cdt/job/$CDT_REPO/job/$CDT_BRANCH/$CDT_BUILD_NUMBER/artifact
if [ "$CDT_REPO" == "cdt" ]; then
    ARTIFACTS_REPO_TARGET=$ARTIFACTS/releng/org.eclipse.cdt.repo/target
    ZIP_NAME=org.eclipse.cdt.repo.zip
elif [ "$CDT_REPO" == "cdt-lsp" ]; then
    ARTIFACTS_REPO_TARGET=$ARTIFACTS/releng/org.eclipse.cdt.lsp.repository/target
    ZIP_NAME=org.eclipse.cdt.lsp.repository.zip
else
    echo "unexpected value for CDT_REPO: ${CDT_REPO}"
    exit 1
fi

echo Using download location root of $DOWNLOAD
echo Using artifacts location root of $ARTIFACTS

echo Testing to make sure artifacts location is sane
wget -q --output-document=/dev/null  $ARTIFACTS

ECHO=echo
if [ "$DRY_RUN" == "false" ]; then
    ECHO=""
else
    echo Dry run of build:
fi

echo Testing to make sure we are publishing to a new directory
$SSH "test ! -e $DOWNLOAD"
$ECHO $SSH "mkdir -p $DOWNLOAD"

$ECHO $SSH "cd $DOWNLOAD && \
    wget -q $ARTIFACTS_REPO_TARGET/repository/*zip*/repository.zip && \
    unzip -q repository.zip && \
    mv repository/* . && \
    rm -r repository repository.zip"

$ECHO $SSH "cd $DOWNLOAD && \
    wget -q $ARTIFACTS_REPO_TARGET/$ZIP_NAME && \
    mv $ZIP_NAME $MILESTONE.zip"

