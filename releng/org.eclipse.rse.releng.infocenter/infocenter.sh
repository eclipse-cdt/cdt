#!/bin/sh
umask 022

PATH=/opt/j2sdk1.4.2_12/bin:$PATH
export PATH

EHOME=/home/infocenter/eclipse3.2/eclipse
IHOME=/home/infocenter/latest

if [ "$1" = "" -o "$1" = "help" ]; then
  echo "Usage: infocenter.sh [start|shutdown|addSite -from dir|apply]"
  exit 1
fi

java \
  -classpath $EHOME/plugins/org.eclipse.help.base_3.2.0.v20060601.jar \
  org.eclipse.help.standalone.Infocenter \
  -command $* \
  -eclipsehome $EHOME \
  -data $IHOME/workspace \
  -port 27127 \
  -nl en -locales en \
  -configuration file://$IHOME/config 

