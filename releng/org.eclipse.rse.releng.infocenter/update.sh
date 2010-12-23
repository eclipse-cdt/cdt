#!/bin/sh
#*******************************************************************************
# Copyright (c) 2006, 2009 Wind River Systems, Inc.
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
ECL_DIR=$IHOME/eclipse
curdir=`pwd`

#update RSE into deployment directory
if [ ! -d $IHOME/deploy/rse ]; then
  mkdir -p $IHOME/deploy/rse
fi
cd $IHOME/deploy/rse
rm *.zip
echo "Downloading RSE-SDK-latest.zip..."
#wget -q "http://build.eclipse.org/tm/downloads/drops/N.latest/RSE-SDK-latest.zip"
wget -q "http://download.eclipse.org/tm/downloads/drops/N.latest/RSE-SDK-latest.zip"
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
  fi
else
  echo "Error downloading RSE-SDK-latest.zip"
fi

#update TM-terminal into deployment directory
if [ ! -d $IHOME/deploy/terminal ]; then
  mkdir -p $IHOME/deploy/terminal
fi
cd $IHOME/deploy/terminal
rm *.zip
echo "Downloading TM-terminal-latest.zip..."
wget -q "http://download.eclipse.org/tm/downloads/drops/N.latest/TM-terminal-latest.zip"
if [ -e TM-terminal-latest.zip ]; then
  echo "Unzipping..."
  unzip -q TM-terminal-latest.zip
  if [ -e plugins.tmp ]; then
    rm -rf plugins.tmp
  fi
  mkdir plugins.tmp
  mv eclipse/plugins/org.eclipse.tm.terminal.view_*.jar plugins.tmp
  mv eclipse/plugins/org.eclipse.tm.terminal_*.jar plugins.tmp
  rm -rf eclipse
  NUM=`ls plugins.tmp/*.jar | wc -l`
  echo "TM-terminal plugins.tmp: NUM=$NUM"
  if [ "$NUM" = "2" ]; then
    echo "Doc plugins got successfully, installing from plugins.tmp into plugins..."
    if [ -e plugins ]; then 
      rm -rf plugins
    fi
    mv plugins.tmp plugins
  fi
else
  echo "Error downloading TM-terminal-latest.zip"
fi

######################### Deploy all #############################
echo "Deploying new plug-ins..."
NEED_RESTART=0
for COMP in rse terminal ; do
  if [ -d "${IHOME}/deploy/${COMP}/plugins" ]; then
    if [ -d "$ECL_DIR/eclipse/dropins/${COMP}/eclipse/plugins" ]; then
      diff -r "${IHOME}/deploy/${COMP}/plugins" "$ECL_DIR/eclipse/dropins/${COMP}/eclipse/plugins" >/dev/null
      result=$?
      if [ "${result}" != "0" ]; then
        echo "${COMP} diffs as ${result} --> deploying"
        NEED_RESTART=1
      else
        echo "${COMP} is unchanged"
      fi
    else
      echo "${COMP} is NEW"
      NEED_RESTART=1
    fi
  fi
done 

######################### Restart Infocenter #############################
#update Infocenter with latest deployable plug-ins
if [ "$NEED_RESTART" != "0" ]; then
  echo "Shutting down infocenter..."
  $IHOME/bin/infocenter.sh shutdown

  for COMP in rse terminal ; do
    if [ -d "${IHOME}/deploy/${COMP}/plugins" ]; then
      if [ -d "$ECL_DIR/eclipse/dropins/${COMP}/eclipse/plugins" ]; then
        rm -rf "$ECL_DIR/eclipse/dropins/${COMP}"
      fi
      mkdir -p "$ECL_DIR/eclipse/dropins/${COMP}/eclipse"
      cp -Rp $IHOME/deploy/${COMP}/plugins "$ECL_DIR/eclipse/dropins/${COMP}/eclipse/"
    fi 
  done 
  
  #TODO: not sure if we need to delete the old index to force re-indexing
  echo "Deleting old index..."
  #rm -rf $IHOME/config/org.eclipse.help.base
  rm -rf $IHOME/eclipse/eclipse/configuration/org.eclipse.help.base
  rm -rf $IHOME/workspace

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
