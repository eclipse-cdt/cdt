#!/bin/sh
umask 022
IHOME=/home/infocenter/latest

curdir=`pwd`

#update RSE
cd $IHOME/deploy/rse
rm *
wget "http://download.eclipse.org/dsdp/tm/downloads/drops/N.latest/RSE-SDK-latest.zip"
if [ -e RSE-SDK-latest.zip ]; then
  unzip RSE-SDK-latest.zip
  rm -rf plugins
  mkdir plugins
  mv eclipse/plugins/*doc* plugins
  rm -rf eclipse
  NUM=`ls plugins/*.jar | wc -l`
  echo "NUM=$NUM"
  if [ "$NUM" = "3" ]; then
    $IHOME/infocenter.sh shutdown
    rm $IHOME/plugins/rse/eclipse/plugins/*
    cp -p plugins/* $IHOME/plugins/rse/eclipse/plugins/
    cd $IHOME
    nohup $IHOME/infocenter.sh start &
    echo "Doing fake search to force rebuilding index"
    sleep 5
    wget "http://localhost/help/latest/advanced/searchView.jsp?searchWord=SystemBasePlugin&maxHits=2" -O search.out.jsp
  fi
fi

