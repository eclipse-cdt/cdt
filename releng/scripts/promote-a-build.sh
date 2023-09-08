#!/bin/bash

set -u # run with unset flag error so that missing parameters cause build failure
set -e # error out on any failed commands
set -x # echo all commands used for debugging purposes

SSHUSER="genie.cdt@projects-storage.eclipse.org"
SSH="ssh ${SSHUSER}"
SCP="scp"
DOWNLOAD=/home/data/httpd/download.eclipse.org/tools/cdt/$RELEASE_OR_BUILD/$MINOR_VERSION/$MILESTONE
ARTIFACTS=https://ci.eclipse.org/cdt/job/$CDT_JOB_NAME/$CDT_BUILD_NUMBER/artifact
ARTIFACTS_REPO_TARGET=$ARTIFACTS/releng/org.eclipse.cdt.repo/target

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
    wget -q $ARTIFACTS_REPO_TARGET/org.eclipse.cdt.repo.zip && \
    mv org.eclipse.cdt.repo.zip $MILESTONE.zip"

