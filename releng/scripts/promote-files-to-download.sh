#!/bin/bash

set -u # run with unset flag error so that missing parameters cause build failure
set -e # error out on any failed commands
set -x # echo all commands used for debugging purposes

SSHUSER="genie.cdt@projects-storage.eclipse.org"
SSH="ssh ${SSHUSER}"
SCP="scp"
DOWNLOAD=/home/data/httpd/download.eclipse.org/tools/cdt

ECHO=echo
if [ "$DRY_RUN" == "false" ]; then
   ECHO=""
else
    echo Dry run of build:
fi

# Rather than putting in infra/coding to make this a fully generic script with parameters,
# just list out what needs to be copied where
$ECHO $SSH mkdir -p "${DOWNLOAD}/releases/latest/"
$ECHO $SCP ./releng/download/releases/latest/* "${SSHUSER}:${DOWNLOAD}/releases/latest/"
$ECHO $SCP ./releng/download/releases/10.6/* "${SSHUSER}:${DOWNLOAD}/releases/10.6/"
$ECHO $SCP ./releng/download/releases/10.7/* "${SSHUSER}:${DOWNLOAD}/releases/10.7/"
