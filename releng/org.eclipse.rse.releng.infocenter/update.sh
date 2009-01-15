#!/bin/sh
#*******************************************************************************
# Copyright (c) 2006, 2007 Wind River Systems, Inc.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html 
# 
# Contributors: 
# Martin Oberhuber - initial API and implementation 
#*******************************************************************************
umask 022
echo "Infocenter Update: running as"
id
if [ "$IHOME" = "" ]; then
  IHOME=/home/infocenter/latest
fi
curdir=`pwd`
NEED_RESTART=0

#update RSE into deployment directory
cd $IHOME/deploy/rse
rm *.zip
echo "Downloading RSE-SDK-latest.zip..."
wget -q "http://build.eclipse.org/dsdp/tm/downloads/drops/N.latest/RSE-SDK-latest.zip"
#wget -q "http://download.eclipse.org/dsdp/tm/downloads/drops/N.latest/RSE-SDK-latest.zip"
if [ -e RSE-SDK-latest.zip ]; then
  echo "Unzipping..."
  unzip -q RSE-SDK-latest.zip
  if [ -e plugins.tmp ]; then
    rm -rf plugins.tmp
  fi
  mkdir plugins.tmp
  mv eclipse/plugins/*doc* plugins.tmp
  rm -rf eclipse
  NUM=`ls plugins.tmp/*.jar | wc -l`
  echo "RSE plugins.tmp: NUM=$NUM"
  if [ "$NUM" = "3" ]; then
    echo "Doc plugins got successfully, installing from plugins.tmp into plugins..."
    if [ -e plugins ]; then 
      rm -rf plugins
    fi
    mv plugins.tmp plugins
    NEED_RESTART=1
  fi
else
  echo "Error downloading RSE-SDK-latest.zip"
fi

#update MTJ into deployment directory
cd $IHOME/deploy/mtj
rm *.zip
echo "Downloading dsdp-mtj-SDK-incubation-latest.zip..."
wget -q "http://build.eclipse.org/dsdp/mtj/downloads/drops/N.latest/dsdp-mtj-SDK-incubation-latest.zip"
if [ -e dsdp-mtj-SDK-incubation-latest.zip ]; then
  echo "Unzipping..."
  unzip -q dsdp-mtj-SDK-incubation-latest.zip
  if [ -e plugins.tmp ]; then
    rm -rf plugins.tmp
  fi
  mkdir plugins.tmp
  mv eclipse/plugins/*doc* plugins.tmp
  rm -rf eclipse
  NUM=`ls plugins.tmp/*.jar | wc -l`
  echo "MTJ plugins.tmp: NUM=$NUM"
  if [ "$NUM" = "2" ]; then
    echo "Doc plugins got successfully, installing from plugins.tmp into plugins..."
    if [ -e plugins ]; then 
      rm -rf plugins
    fi
    mv plugins.tmp plugins
    NEED_RESTART=1
  fi
else
  echo "Error downloading dsdp-mtj-SDK-incubation-latest.zip"
fi

#update Infocenter with latest deployable plug-ins
if [ "$NEED_RESTART" != "0" ]; then
  echo "Shutting down infocenter..."
  $IHOME/bin/infocenter.sh shutdown

  echo "Deploying new plug-ins..."
  ######################### Deploy RSE #############################
  rm $IHOME/plugins/rse/eclipse/plugins/*
  cp -p $IHOME/deploy/rse/plugins/* $IHOME/plugins/rse/eclipse/plugins/
  ####################### Deploy dd.dsf ############################
  rm $IHOME/plugins/dd.dsf/eclipse/plugins/*
  cp -p $IHOME/deploy/dd.dsf/plugins/* $IHOME/plugins/dd.dsf/eclipse/plugins/
  ####################### Deploy nab ###############################
  rm -rf $IHOME/plugins/nab/eclipse/plugins/*
  cp -Rp $IHOME/deploy/nab/plugins/* $IHOME/plugins/nab/eclipse/plugins/
  ####################### Deploy ercp ##############################
  rm -rf $IHOME/plugins/ercp/eclipse/plugins/*
  cp -Rp $IHOME/deploy/ercp/*.jar $IHOME/plugins/ercp/eclipse/plugins/
  ####################### Deploy ercp ##############################
  rm -rf $IHOME/plugins/mtj/eclipse/plugins/*
  cp -Rp $IHOME/deploy/mtj/*.jar $IHOME/plugins/mtj/eclipse/plugins/
  
  #TODO: not sure if we need to delete the old index to force re-indexing
  echo "Deleting old index..."
  rm -rf $IHOME/config/org.eclipse.help.base

  cd $IHOME
  echo "Restarting infocenter..."
  nohup $IHOME/bin/infocenter.sh start &
  echo "Waiting for Infocenter / Apache to come up [60 seconds]"
  sleep 60
  echo "Doing fake search to force rebuilding index"
  wget -q "http://localhost/help/latest/advanced/searchView.jsp?searchWord=SystemBasePlugin&maxHits=2" -O search.out.jsp -t 3 --waitretry=30
  echo "Done, index should be up again!"
else
  echo "Nothing new deployed, no restart necessary."
fi

cd "$curdir"
