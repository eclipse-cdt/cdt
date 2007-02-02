#!/bin/sh
#*******************************************************************************
# Copyright (c) 2006 Wind River Systems, Inc.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html 
# 
# Contributors: 
# Martin Oberhuber - initial API and implementation 
#*******************************************************************************
# Convert normal "site.xml" to "testUpdates"
#
# Prerequisites: 
# - Eclipse 3.3Mx installed in $HOME/ws2/eclipse
# - Java5 in the PATH or in /shared/common/ibm-java2-ppc64-50

curdir=`pwd`
cd `dirname $0`
mydir=`pwd`

umask 022

#Use Java5 on build.eclipse.org - need JRE for pack200
export PATH=/shared/common/ibm-java2-ppc64-50/jre/bin:/shared/common/ibm-java2-ppc64-50/bin:$PATH

# patch site.xml
cd ..
SITE=`pwd`
if [ -f index.html.new ]; then
  rm -f index.html.new
fi
if [ -f site.xml.new ]; then
  rm -f site.xml.new
fi
if [ -f web/site.xsl.new ]; then
  rm -f web/site.xsl.new
fi

# get newest plugins and features: to be done manually on real update site
if [ `basename $SITE` = testUpdates ]; then
    echo "Working on test update site"
    REL=`ls $HOME/ws2/working/package | sort | tail -1`
    if [ "$REL" != "" ]; then
      echo "Checking new Updates from $REL"
      DIR="$HOME/ws2/working/package/$REL/updates"
      if [ -d "$DIR/features" ]; then
        echo "Copying new plugins and features from $DIR"
        rm -rf features
        rm -rf plugins
        cp -R $DIR/features .
        cp -R $DIR/plugins .
      fi
    fi
    rm index.html site.xml web/site.xsl
    cvs -q update -dPR
    sed -e 's,/dsdp/tm/updates,/dsdp/tm/testUpdates,g' \
    	-e 's,Project Update,Project Test Update,g' \
    	index.html > index.html.new
    mv -f index.html.new index.html
    sed -e 's,/dsdp/tm/updates,/dsdp/tm/testUpdates,g' \
        -e 's,Project Update,Project Test Update,g' \
        site.xml > site.xml.new
    mv -f site.xml.new site.xml
    sed -e 's,Project Update,Project Test Update,g' \
    	web/site.xsl > web/site.xsl.new
    mv -f web/site.xsl.new web/site.xsl
    echo "Conditioning the site... $SITE"
    #java -Dorg.eclipse.update.jarprocessor.pack200=$mydir \
    #    -jar $HOME/ws2/eclipse/startup.jar \
    #    -application org.eclipse.update.core.siteOptimizer \
    #    -jarProcessor -outputDir $SITE \
    #    -processAll -repack $SITE
    java -Dorg.eclipse.update.jarprocessor.pack200=$mydir \
    	$HOME/ws2/jarprocessor/jarprocessor.jar \
		-outputDir $SITE -processAll -repack $SITE
elif [ `basename $SITE` = signedUpdates ]; then
    echo "Working on signed update site"
    echo "Signing jars from test update site (expecting conditioned jars)..."
    STAGING=/home/data/httpd/download-staging.priv/dsdp/tm
    stamp=`date +'%Y%m%d-%H%M'`
    if [ -d ${STAGING} -a -d ${SITE}/../testUpdates ]; then
      #get jars from testUpdates, sign them and put them here
      mkdir ${SITE}/features.${stamp}
      mkdir -p ${STAGING}/updates.${stamp}/features
      cp -R ${SITE}/../testUpdates/features/*.jar ${STAGING}/updates.${stamp}/features
      cd ${STAGING}/updates.${stamp}/features
      for x in `ls *.jar`; do
        echo "signing feature: ${x}"
        sign ${x} nomail >/dev/null
      done
      TRIES=10
      while [ $TRIES -gt 0 ]; do
        sleep 30
        echo "TRIES to go: ${TRIES}"
        for x in `ls *.jar | grep -v '^temp[_.]'`; do
          result=`jarsigner -verify ${x}`
          if [ "$result" = "jar verified." ]; then
            echo "${result}: ${x}"
            cp ${x} ${SITE}/features.${stamp}
            rm ${x}
          else
            echo "-pending- ${x} : ${result}" | head -1
            sleep 30
          fi
        done
        FILES=`ls 2>/dev/null`
        if [ "$FILES" = "" ]; then
          TRIES=0
          ok=1
        else
          echo "--> FILES is $FILES"
          TRIES=`expr $TRIES - 1`
          ok=0
        fi
      done
      if [ "$ok" = "1" ]; then
        rmdir ${STAGING}/updates.${stamp}/features
        mkdir ${SITE}/plugins.${stamp}
        mkdir -p ${STAGING}/updates.${stamp}/plugins
        cp ${SITE}/../testUpdates/plugins/*.jar ${STAGING}/updates.${stamp}/plugins
        cd ${STAGING}/updates.${stamp}/plugins
        for x in `ls *.jar`; do
          echo "signing plugin: ${x}"
          sign ${x} nomail >/dev/null
        done
        TRIES=10
        while [ $TRIES -gt 0 ]; do
          sleep 30
          echo "TRIES to go: ${TRIES}"
          for x in `ls *.jar | grep -v '^temp[_.]'`; do
            result=`jarsigner -verify ${x}`
            if [ "$result" = "jar verified." ]; then
              echo "${result}: ${x}"
              cp ${x} ${SITE}/plugins.${stamp}
              rm ${x}
            else
              echo "-pending- ${x} : ${result}" | head -1
              sleep 30
            fi
          done
          FILES=`ls 2>/dev/null`
          if [ "$FILES" = "" ]; then
            TRIES=0
            ok=1
          else
            echo "--> FILES is $FILES"
            TRIES=`expr $TRIES - 1`
            ok=0
          fi
        done
      fi
      if [ "$ok" = "1" ]; then
        cd ${SITE}
        rmdir ${STAGING}/updates.${stamp}/plugins
        rmdir ${STAGING}/updates.${stamp}
        #mv features features.old.${stamp}
        #mv plugins plugins.old.${stamp}
        rm -rf features plugins
        mv features.${stamp} features
        mv plugins.${stamp} plugins
      else
        echo "Something went wrong during staging and signing."
        echo "Keeping existing update site intact."
        exit 1
      fi
    else
      echo "staging or testUpdates not found:"
      echo "please fix your pathes"
      exit 1
    fi
    rm index.html site.xml web/site.xsl
    cvs -q update -dPR
    sed -e 's,/dsdp/tm/updates,/dsdp/tm/signedUpdates,g' \
    	-e 's,Project Update,Project Signed Test Update,g' \
    	index.html > index.html.new
    mv -f index.html.new index.html
    sed -e 's,/dsdp/tm/updates,/dsdp/tm/signedUpdates,g' \
        -e 's,Project Update,Project Signed Test Update,g' \
        site.xml > site.xml.new
    mv -f site.xml.new site.xml
    sed -e 's,Project Update,Project Signed Test Update,g' \
    	web/site.xsl > web/site.xsl.new
    mv -f web/site.xsl.new web/site.xsl
elif [ `basename $SITE` = milestones ]; then
    echo "Working on milestone update site"
    echo "Expect that you copied your features and plugins yourself"
    stamp=`date +'%Y%m%d-%H%M'`
    rm index.html site.xml web/site.xsl
    cvs -q update -dPR
    sed -e 's,/dsdp/tm/updates,/dsdp/tm/updates/milestones,g' \
    	-e 's,Project Update,Project Milestone Update,g' \
    	-e '\,</h1>,a\
This site contains Target Management Milestones (I-, S- and M- builds) which are \
being contributed to the Europa coordinated release train (Eclipse 3.3).' \
    	index.html > index.html.new
    mv -f index.html.new index.html
    sed -e 's,/dsdp/tm/updates,/dsdp/tm/updates/milestones,g' \
        -e 's,Project Update,Project Milestone Update,g' \
        site.xml > site.xml.new
    mv -f site.xml.new site.xml
    sed -e 's,Project Update,Project Milestone Update,g' \
    	web/site.xsl > web/site.xsl.new
    mv -f web/site.xsl.new web/site.xsl
else
    echo "Working on official update site"
    echo "Expect that you copied your features and plugins yourself"
    stamp=`date +'%Y%m%d-%H%M'`
    rm index.html site.xml web/site.xsl
    cvs -q update -dPR
fi
FEATURES=`grep 'features/[^ ]*\.qualifier\.jar' site.xml | sed -e 's,^[^"]*"features/\([^0-9]*[0-9][0-9.]*\).*$,\1,g'`
for feature in $FEATURES ; do
  #list newest ones first
  TAG=`ls -t features/${feature}*.jar | head -1 | sed -e 's,[^0-9]*[0-9][0-9]*\.[0-9]*\.[0-9]*\.\([^.]*\).jar,\1,'`
  if [ "$TAG" != "" ]; then
    echo "$feature : $TAG"
    sed -e "/$feature/s/qualifier/$TAG/g" site.xml > site.xml.new
    mv -f site.xml.new site.xml
  fi
done

# optimize the site
# see http://wiki.eclipse.org/index.php/Platform-releng-faq
#Pack the site
# Workaround for downgrading effort of pack200 to avoid VM bug
# See https://bugs.eclipse.org/bugs/show_bug.cgi?id=154069
echo "Packing the site... $SITE"
#java -Dorg.eclipse.update.jarprocessor.pack200=$mydir \
#    -jar $HOME/ws2/eclipse/startup.jar \
#    -application org.eclipse.update.core.siteOptimizer \
#    -jarProcessor -outputDir $SITE \
#    -processAll -pack $SITE
java -Dorg.eclipse.update.jarprocessor.pack200=$mydir \
    $HOME/ws2/jarprocessor/jarprocessor.jar \
    -outputDir $SITE -processAll -pack $SITE

#Create the digest
echo "Creating digest..."
java -jar $HOME/ws2/eclipse/startup.jar \
    -application org.eclipse.update.core.siteOptimizer \
    -digestBuilder -digestOutputDir=$SITE \
    -siteXML=$SITE/site.xml

cd $SITE
chgrp -R dsdp-tmadmin .
chmod -R g+w .
chmod -R a+r .
cd $curdir
