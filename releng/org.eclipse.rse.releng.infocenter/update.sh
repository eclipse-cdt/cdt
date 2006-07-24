#!/bin/sh
umask 022
if [ "$IHOME" = "" ]; then
  IHOME=/home/infocenter/latest
fi
curdir=`pwd`
NEED_RESTART=0

#update RSE into deplopyment directory
cd $IHOME/deploy/rse
rm *.zip
wget -q "http://download.eclipse.org/dsdp/tm/downloads/drops/N.latest/RSE-SDK-latest.zip"
if [ -e RSE-SDK-latest.zip ]; then
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
    if [ -e plugins ]; then 
      rm -rf plugins
    fi
    mv plugins.tmp plugins
    NEED_RESTART=1
  fi
else
  echo "Error downloading RSE-SDK-latest.zip"
fi

#update Infocenter with latest deployable plug-ins
if [ "$NEED_RESTART" != "0" ]; then
  echo "Shutting down infocenter..."
  $IHOME/bin/infocenter.sh shutdown

  echo "Deploying new plug-ins..."
  rm $IHOME/plugins/rse/eclipse/plugins/*
  cp -p $IHOME/deploy/rse/plugins/* $IHOME/plugins/rse/eclipse/plugins/
  
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
