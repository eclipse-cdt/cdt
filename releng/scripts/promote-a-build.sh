#!/bin/bash

set -u # run with unset flag error so that missing parameters cause build failure
set -e # error out on any failed commands
set -x # echo all commands used for debugging purposes

SSHUSER="genie.cdt@projects-storage.eclipse.org"
SSH="ssh ${SSHUSER}"
DOWNLOAD=/home/data/httpd/download.eclipse.org/tools/cdt/$RELEASE_OR_BUILD/$MINOR_VERSION/$MILESTONE
ARTIFACTS=/home/data/httpd/download.eclipse.org/tools/cdt/builds/$CDT_REPO/$CDT_BRANCH

if [ "$CDT_REPO" == "cdt" ]; then
    ZIP_NAME=org.eclipse.cdt.repo.zip
elif [ "$CDT_REPO" == "cdt-lsp" ]; then
    ZIP_NAME=org.eclipse.cdt.lsp.repository.zip
else
    echo "unexpected value for CDT_REPO: ${CDT_REPO}"
    exit 1
fi

echo Using output download location root of $DOWNLOAD
echo Using input download location root of $ARTIFACTS

ECHO=echo
if [ "$DRY_RUN" == "false" ]; then
    ECHO=""
else
    echo Dry run of build:
fi

echo Testing to make sure we are publishing to a new directory
$SSH "test ! -e $DOWNLOAD"
$ECHO $SSH "mkdir -p $DOWNLOAD"
echo Copying artifacts from latest build output of $CDT_REPO/$CDT_BRANCH to $DOWNLOAD
$ECHO $SSH "cp -rpvf $ARTIFACTS/* $DOWNLOAD/"
echo Renaming zip of artifacts to match the expected name for the milestone
$ECHO $SSH "mv -vf $DOWNLOAD/$ZIP_NAME $DOWNLOAD/$MILESTONE.zip"
$ECHO $SSH "find $DOWNLOAD -exec touch {} +"
