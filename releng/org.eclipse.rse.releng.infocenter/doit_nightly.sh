#!/bin/sh

#Find out the IHOME - it's the parent of my own directory ($IHOME/bin)
curdir=`pwd`
cd `dirname $0`
mydir=`pwd`
cd ..
IHOME=`pwd`
export IHOME

#Update scripts - prerequisite: IHOME is infocenter home
umask 022
cd "$IHOME/bin"
cvs -q update -d
chmod a+x *.sh

#Update the infocenter
./update.sh
