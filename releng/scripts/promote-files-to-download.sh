#!/bin/bash

set -u # run with unset flag error so that missing parameters cause build failure
set -e # error out on any failed commands
set -x # echo all commands used for debugging purposes

SSHUSER="genie.cdt@projects-storage.eclipse.org"
SSH="ssh ${SSHUSER}"
SCP="scp"
DOWNLOAD=/home/data/httpd/download.eclipse.org/tools/cdt
ARCHIVE=/home/data/httpd/archive.eclipse.org/tools/cdt

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
$ECHO $SCP ./releng/download/releases/11.6/* "${SSHUSER}:${DOWNLOAD}/releases/11.6/"
$ECHO $SCP ./releng/download/releases/12.0/* "${SSHUSER}:${DOWNLOAD}/releases/12.0/"

$ECHO $SSH mkdir -p "${DOWNLOAD}/releases/cdt-lsp-latest/"
$ECHO $SCP ./releng/download/releases/cdt-lsp-latest/* "${SSHUSER}:${DOWNLOAD}/releases/cdt-lsp-latest/"
$ECHO $SCP ./releng/download/releases/cdt-lsp-2.0/* "${SSHUSER}:${DOWNLOAD}/releases/cdt-lsp-2.0/"
$ECHO $SCP ./releng/download/releases/cdt-lsp-3.0/* "${SSHUSER}:${DOWNLOAD}/releases/cdt-lsp-3.0/"
$ECHO $SCP ./releng/download/releases/cdt-lsp-3.1/* "${SSHUSER}:${DOWNLOAD}/releases/cdt-lsp-3.1/"

# Issue #235 - some of the old archived releases had bad composites. For those
# ones we started maintaining the files here as it was the easiest way to edit
# the files. Add more here as needed.
$ECHO $SCP ./releng/download/releases/9.3/* "${SSHUSER}:${ARCHIVE}/releases/9.3/"
$ECHO $SCP ./releng/download/releases/9.4/* "${SSHUSER}:${ARCHIVE}/releases/9.4/"
$ECHO $SCP ./releng/download/releases/9.5/* "${SSHUSER}:${ARCHIVE}/releases/9.5/"
$ECHO $SCP ./releng/download/releases/9.6/* "${SSHUSER}:${ARCHIVE}/releases/9.6/"
$ECHO $SCP ./releng/download/releases/9.7/* "${SSHUSER}:${ARCHIVE}/releases/9.7/"
$ECHO $SCP ./releng/download/releases/9.8/* "${SSHUSER}:${ARCHIVE}/releases/9.8/"
$ECHO $SCP ./releng/download/releases/10.6/* "${SSHUSER}:${ARCHIVE}/releases/10.6/"
$ECHO $SCP ./releng/download/releases/10.7/* "${SSHUSER}:${ARCHIVE}/releases/10.7/"
$ECHO $SCP ./releng/download/releases/11.0/* "${SSHUSER}:${ARCHIVE}/releases/11.0/"
$ECHO $SCP ./releng/download/releases/11.1/* "${SSHUSER}:${ARCHIVE}/releases/11.1/"
$ECHO $SCP ./releng/download/releases/11.2/* "${SSHUSER}:${ARCHIVE}/releases/11.2/"
$ECHO $SCP ./releng/download/releases/11.3/* "${SSHUSER}:${ARCHIVE}/releases/11.3/"
$ECHO $SCP ./releng/download/releases/11.4/* "${SSHUSER}:${ARCHIVE}/releases/11.4/"
$ECHO $SCP ./releng/download/releases/11.5/* "${SSHUSER}:${ARCHIVE}/releases/11.5/"
$ECHO $SCP ./releng/download/releases/cdt-lsp-1.0/* "${SSHUSER}:${ARCHIVE}/releases/cdt-lsp-1.0/"
$ECHO $SCP ./releng/download/releases/cdt-lsp-1.1/* "${SSHUSER}:${ARCHIVE}/releases/cdt-lsp-1.1/"
