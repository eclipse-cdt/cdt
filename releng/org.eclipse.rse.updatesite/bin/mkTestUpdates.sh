#!/bin/sh
#*******************************************************************************
# Copyright (c) 2006, 2011 Wind River Systems, Inc.
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
# - Java5 in the PATH or in /shared/tools/tm/jdk-1.5

curdir=`/bin/pwd`
cd `dirname $0`
mydir=`/bin/pwd`

umask 022

#Use Java5 on build.eclipse.org - need JRE for pack200
export PATH=/shared/tools/tm/jdk-1.5/jre/bin:/shared/tools/tm/jdk-1.5/bin:$PATH
basebuilder=${HOME}/ws2/org.eclipse.releng.basebuilder
tgtlauncher=`ls ${HOME}/ws2/eclipse/plugins/org.eclipse.equinox.launcher_* | sort | tail -1`

# patch site.xml
cd ..
SITE=`/bin/pwd`
echo "SITE is ${SITE}"
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
TPVERSION="Target Management"
VERSION=3.3
DO_STATS=0
DO_CATEGORIES=0
TYPE=none
SITEDIR=`basename ${SITE}`
case ${SITEDIR} in
  test*Updates)   TYPE=test ;;
  signed*Updates) TYPE=testSigned ;;
  *milestones)    TYPE=milestone ;;
  *interim)       TYPE=interim ;;
  *)              TYPE=unknown ;;
esac
case ${SITEDIR} in
  3.2*)  VERSION=3.2 ; DO_CATEGORIES=1 ;;
  3.3*)  VERSION=3.3 ; DO_CATEGORIES=1 ;;
esac
case ${SITEDIR} in
  3.2) DO_STATS=1 ;;
  3.3) DO_STATS=1 ;;
esac
if [ ${TYPE} = test ]; then
    TPTYPE="${VERSION} Test"
    TPVERSION="${TPVERSION} ${TPTYPE}"
    echo "Working on ${TPVERSION} update site"
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
    # CHECK VERSION CORRECTNESS for MICRO or MINOR UPDATES only
    # Major version updates are not allowed.
    # Update of "qualifier" requires also updating "micro" or "minor"
    echo "VERIFYING VERSION CORRECTNESS: Features against ../updates/3.2"
    ls features/*.jar | sed -e 's,^.*features/,,' | sort > f1.$$.txt
    ls ../updates/3.2/features/*.jar | sed -e 's,^.*features/,,' | sort > f2.$$.txt
    diff f2.$$.txt f1.$$.txt | grep '^[>]' \
       | sed -e 's,[>] \(.*_[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*\)\..*,\1,' > f_new.txt
    for f in `cat f_new.txt`; do
      fold=`grep "${f}\." f2.$$.txt`
      if [ "${fold}" != "" ]; then
        echo "PROBLEM: QUALIFIER update without MICRO: ${f}"
      fi
      #fbase=`echo $f | sed -e 's,\(.*_[0-9][0-9]*\.[0-9][0-9]*\)\..*,\1,'`
      #fold=`grep "${fbase}\." f2.$$.txt`
      #if [ "${fold}" = "" ]; then
      #  echo "PROBLEM: MAJOR or MINOR update : ${f}"
      #fi
      fbase=`echo $f | sed -e 's,\(.*_[0-9][0-9]*\)\.[0-9][0-9]*\..*,\1,'`
      fold=`grep ${fbase} f2.$$.txt`
      if [ "${fold}" = "" ]; then
        echo "PROBLEM: MAJOR update or NEW : ${f}"
      fi
    done
    echo "VERIFYING VERSION CORRECTNESS: Plugins against ../updates/3.2"
    ls plugins/*.jar | sed -e 's,^.*plugins/,,' | sort > p1.$$.txt
    ls ../updates/3.2/plugins/*.jar | sed -e 's,^.*plugins/,,' | sort > p2.$$.txt
    diff p2.$$.txt p1.$$.txt | grep '^[>]' \
       | sed -e 's,[>] \(.*_[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*\)\..*,\1,' > p_new.txt
    for p in `cat p_new.txt`; do
      pold=`grep "${p}\." p2.$$.txt`
      if [ "${pold}" != "" ]; then
        echo "PROBLEM: QUALIFIER update without MICRO: ${p}"
      fi
      #pbase=`echo $p | sed -e 's,\(.*_[0-9][0-9]*\.[0-9][0-9]*\)\..*,\1,'`
      #pold=`grep "${pbase}\." p2.$$.txt`
      #if [ "${pold}" = "" ]; then
      #  echo "PROBLEM: MAJOR or MINOR update : ${p}"
      #fi
      pbase=`echo $p | sed -e 's,\(.*_[0-9][0-9]*\)\.[0-9][0-9]*\..*,\1,'`
      pold=`grep ${pbase} p2.$$.txt`
      if [ "${pold}" = "" ]; then
        echo "PROBLEM: MAJOR update or NEW : ${p}"
      fi
    done
    #rm f_new.txt p_new.txt
    mv -f f1.$$.txt fversions.txt
    mv -f p1.$$.txt pversions.txt
    mv -f f2.$$.txt f30versions.txt
    mv -f p2.$$.txt p30versions.txt
    ## rm f1.$$.txt f2.$$.txt p1.$$.txt p2.$$.txt    
    rm index.html site.xml web/site.xsl
    cvs -q update -dPR
    sed -e "s,/tm/updates/2.0,/tm/${SITEDIR},g" \
    	-e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	index.html > index.html.new
    mv -f index.html.new index.html
    sed -e "s,/tm/updates/2.0,/tm/${SITEDIR},g" \
        -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	-e '/<!-- BEGIN_2_0 -->/,/<!-- END_2_0_4 -->/d' \
    	-e '/<!-- BEGIN_3_0 -->/,/<!-- END_3_2 -->/d' \
        site.xml > site.xml.new
    mv -f site.xml.new site.xml
    sed -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	web/site.xsl > web/site.xsl.new
    mv -f web/site.xsl.new web/site.xsl
    echo "Conditioning the site... $SITE"
    #java -Dorg.eclipse.update.jarprocessor.pack200=$mydir \
    #java -jar $HOME/ws2/eclipse/startup.jar \
    #java -jar ${basebuilder}/plugins/org.eclipse.equinox.launcher.jar \
    java -jar ${tgtlauncher} \
        -application org.eclipse.update.core.siteOptimizer \
        -jarProcessor -outputDir $SITE \
        -processAll -repack $SITE
    #java -Dorg.eclipse.update.jarprocessor.pack200=$mydir \
    #	$HOME/ws2/jarprocessor/jarprocessor.jar \
	#	-outputDir $SITE -processAll -repack $SITE
elif [ ${TYPE} = testSigned ]; then
    TPTYPE="${VERSION} Signed Test"
    TPVERSION="${TPVERSION} ${TPTYPE}"
    echo "Working on ${TPVERSION} update site"
    echo "Signing jars from ${SITE}/../testUpdates (expecting conditioned jars)..."
    STAGING=/home/data/httpd/download-staging.priv/tools/tm
    stamp=`date +'%Y%m%d-%H%M'`
    if [ -d ${STAGING} -a -d ${SITE}/../testUpdates ]; then
      #get jars from testUpdates, sign them and put them here
      mkdir ${SITE}/features.${stamp}
      mkdir -p ${STAGING}/updates.${stamp}/features
      chmod -R g+w ${STAGING}/updates.${stamp}
      cp -R ${SITE}/../testUpdates/features/*.jar ${STAGING}/updates.${stamp}/features
      cd ${STAGING}/updates.${stamp}/features
      for x in `ls *.jar`; do
        result=`jarsigner -verify ${x} | head -1`
        if [ "$result" != "jar verified." ]; then
          # do not sign Orbit bundles again since they are signed already 
          echo "signing feature: ${x}"
          sign ${x} nomail >/dev/null
        fi
      done
      TRIES=10
      while [ $TRIES -gt 0 ]; do
        sleep 30
        echo "TRIES to go: ${TRIES}"
        for x in `ls *.jar | grep -v '^temp[_.]'`; do
          result=`jarsigner -verify ${x} | head -1`
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
        chmod -R g+w ${STAGING}/updates.${stamp}
        cp ${SITE}/../testUpdates/plugins/*.jar ${STAGING}/updates.${stamp}/plugins
        cd ${STAGING}/updates.${stamp}/plugins
        for x in `ls *.jar`; do
          result=`jarsigner -verify ${x} | head -1`
          if [ "$result" != "jar verified." ]; then
            # do not sign Orbit bundles again since they are signed already 
            echo "signing plugin: ${x}"
            sign ${x} nomail >/dev/null
          fi
        done
        TRIES=10
        while [ $TRIES -gt 0 ]; do
          sleep 30
          echo "TRIES to go: ${TRIES}"
          for x in `ls *.jar | grep -v '^temp[_.]'`; do
            result=`jarsigner -verify ${x} | head -1`
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
        rm fversions.txt pversions.txt f30versions.txt p30versions.txt f_new.txt p_new.txt 2>/dev/null
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
    sed -e "s,/tm/updates/2.0,/tm/${SITEDIR},g" \
    	-e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	index.html > index.html.new
    mv -f index.html.new index.html
    sed -e "s,/tm/updates/2.0,/tm/${SITEDIR},g" \
        -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	-e '/<!-- BEGIN_2_0 -->/,/<!-- END_2_0_4 -->/d' \
    	-e '/<!-- BEGIN_3_0 -->/,/<!-- END_3_2 -->/d' \
        site.xml > site.xml.new
    mv -f site.xml.new site.xml
    sed -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	web/site.xsl > web/site.xsl.new
    mv -f web/site.xsl.new web/site.xsl
elif [ ${TYPE} = milestone ]; then
    TPTYPE="${VERSION} Milestone"
    TPVERSION="${TPVERSION} ${TPTYPE}"
    echo "Working on ${TPVERSION} update site"
    echo "Expect that you copied your features and plugins yourself"
    stamp=`date +'%Y%m%d-%H%M'`
    rm index.html site.xml web/site.xsl
    cvs -q update -dPR
    sed -e "s,/tm/updates/2.0,/tm/updates/${SITEDIR},g" \
    	-e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	-e '\,</h1>,a\
This site contains Target Management Milestones (I-, S- and M- builds) which are \
being contributed to the Eclipse Indigo coordinated release train (Eclipse 3.7.x).' \
    	index.html > index.html.new
    mv -f index.html.new index.html
    ## keep 3.0.x features in site.xml
    ##	-e '/<!-- BEGIN_2_0_1 -->/,/<!-- END_2_0_4 -->/d' \
    sed -e "s,/tm/updates/2.0,/tm/updates/${SITEDIR},g" \
        -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	-e '/<!-- BEGIN_2_0 -->/,/<!-- END_2_0_4 -->/d' \
    	-e '/<!-- BEGIN_3_0 -->/,/<!-- END_3_2 -->/d' \
        site.xml > site.xml.new
    mv -f site.xml.new site.xml
    sed -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	web/site.xsl > web/site.xsl.new
    mv -f web/site.xsl.new web/site.xsl
elif [ ${TYPE} = interim ]; then
    TPTYPE="${VERSION} Interim"
    TPVERSION="${TPVERSION} ${TPTYPE}"
    echo "Working on ${TPVERSION} update site"
    echo "Expect that you copied your features and plugins yourself"
    stamp=`date +'%Y%m%d-%H%M'`
    rm index.html site.xml web/site.xsl
    cvs -q update -dPR
    sed -e "s,/tm/updates/2.0,/tm/updates/${SITEDIR},g" \
    	-e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	-e '\,</h1>,a\
This site contains Target Management Interim Maintenance builds (I-, S-, and M-builds) in order \
to test them before going live.' \
    	index.html > index.html.new
    mv -f index.html.new index.html
    ## keep 2.0.x features in site.xml
    ##	-e '/<!-- BEGIN_2_0_1 -->/,/<!-- END_2_0_4 -->/d' \
    sed -e "s,/tm/updates/2.0,/tm/updates/${SITEDIR},g" \
        -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	-e '/<!-- BEGIN_2_0 -->/,/<!-- END_2_0_4 -->/d' \
    	-e '/<!-- BEGIN_3_0 -->/,/<!-- END_3_2 -->/d' \
        site.xml > site.xml.new
    mv -f site.xml.new site.xml
    sed -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	web/site.xsl > web/site.xsl.new
    mv -f web/site.xsl.new web/site.xsl
elif [ `basename $SITE` = 3.0 ]; then
    TPTYPE="3.0"
    TPVERSION="${TPVERSION} ${TPTYPE}"
    TYPE=official
    echo "Working on ${TPVERSION} update site"
    echo "Expect that you copied your features and plugins yourself"
    stamp=`date +'%Y%m%d-%H%M'`
    rm index.html site.xml web/site.xsl
    cvs -q update -dPR
    sed -e "s,/tm/updates/2.0,/tm/updates/${SITEDIR},g" \
    	-e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	-e '\,</h1>,a\
This site contains Target Management 3.0 Releases and Updates (R- builds) which are \
being contributed to the Ganymede coordinated release train (Eclipse 3.4).' \
    	index.html > index.html.new
    mv -f index.html.new index.html
    ## dont keep 2.0.x features in site.xml
    sed -e "s,/tm/updates/2.0,/tm/updates/${SITEDIR},g" \
        -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	-e '/<!-- BEGIN_2_0 -->/,/<!-- END_2_0_4 -->/d' \
    	-e '/<!-- BEGIN_3_0_4 -->/,/<!-- END_3_0_4 -->/d' \
    	-e '/<!-- BEGIN_3_2 -->/,/<!-- END_ALL -->/d' \
        site.xml > site.xml.new
    mv -f site.xml.new site.xml
    sed -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	web/site.xsl > web/site.xsl.new
    mv -f web/site.xsl.new web/site.xsl
elif [ `basename $SITE` = 3.1 ]; then
    TPTYPE="3.1"
    TPVERSION="${TPVERSION} ${TPTYPE}"
    TYPE=official
    echo "Working on ${TPVERSION} update site"
    echo "Expect that you copied your features and plugins yourself"
    stamp=`date +'%Y%m%d-%H%M'`
    rm index.html site.xml web/site.xsl
    cvs -q update -dPR
    sed -e "s,/tm/updates/2.0,/tm/updates/${SITEDIR},g" \
    	-e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	-e '\,</h1>,a\
This site contains Target Management 3.1 Releases and Updates (R- builds) which are \
being contributed to the Galileo coordinated release train (Eclipse 3.5).' \
    	index.html > index.html.new
    mv -f index.html.new index.html
    ## dont keep 2.0.x features in site.xml
    sed -e "s,/tm/updates/2.0,/tm/updates/${SITEDIR},g" \
        -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	-e '/<!-- BEGIN_2_0 -->/,/<!-- END_2_0_4 -->/d' \
        -e '/<!-- BEGIN_3_0 -->/,/<!-- END_3_0_3 -->/d' \
        -e '/<!-- BEGIN_3_2 -->/,/<!-- END_ALL -->/d' \
        site.xml > site.xml.new
    mv -f site.xml.new site.xml
    sed -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	web/site.xsl > web/site.xsl.new
    mv -f web/site.xsl.new web/site.xsl
elif [ `basename $SITE` = 3.2 ]; then
    TPTYPE="3.2"
    TPVERSION="${TPVERSION} ${TPTYPE}"
    TYPE=official
    echo "Working on ${TPVERSION} update site"
    echo "Expect that you copied your features and plugins yourself"
    stamp=`date +'%Y%m%d-%H%M'`
    rm index.html site.xml web/site.xsl
    cvs -q update -dPR
    sed -e "s,/tm/updates/2.0,/tm/updates/${SITEDIR},g" \
    	-e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	-e '\,</h1>,a\
This site contains Target Management 3.2 Releases and Updates (R- builds) which are \
being contributed to the Eclipse Helios coordinated release train (Eclipse 3.6.x).' \
    	index.html > index.html.new
    mv -f index.html.new index.html
    ## dont keep 2.0.x features in site.xml
    sed -e "s,/tm/updates/2.0,/tm/updates/${SITEDIR},g" \
        -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	-e '/<!-- BEGIN_2_0 -->/,/<!-- BEGIN_3_2 -->/d' \
        site.xml > site.xml.new
    mv -f site.xml.new site.xml
    sed -e "s,Project 2.0 Update,Project ${TPTYPE} Update,g" \
    	web/site.xsl > web/site.xsl.new
    mv -f web/site.xsl.new web/site.xsl
else
    echo "Working on official update site"
    TYPE=official
    echo "Expect that you copied your features and plugins yourself"
    stamp=`date +'%Y%m%d-%H%M'`
    rm index.html site.xml web/site.xsl
    cvs -q update -dPR
    sed -e '/<!-- BEGIN_2_0_5 -->/,/<!-- END_2_0_5 -->/d' \
        site.xml > site.xml.new1
    sed -e '/<!-- BEGIN_3_0_3 -->/,/<!-- END_3_0_3 -->/d' \
        site.xml.new1 > site.xml.new2
    sed -e '/<!-- BEGIN_3_1 -->/,/<!-- END_ALL -->/d' \
        site.xml.new2 > site.xml.new
    mv -f site.xml.new site.xml
    rm site.xml.new1 site.xml.new2
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
#Create Europa version of site.xml
if [ -f site-europa.xml ]; then
  rm -rf site-europa.xml
fi
sed -e '/!EUROPA_ONLY!/d' site.xml > site-europa.xml

#Get rid of Europa comments completely in order to avoid SAX exception 
#in comment when the feature qualifier extends to --
awk 'BEGIN {doit=1}
  /-- !EUROPA_ONLY!/ {doit=0}
  { if(doit==1) print; }
  /!EUROPA_ONLY! --/ {doit=1}' site.xml > site.xml.tmp
mv -f site.xml.tmp site.xml

# optimize the site
# see http://wiki.eclipse.org/Platform-releng-faq
case ${TYPE} in test*)
  echo "Packing the site... $SITE"
  # Workaround for downgrading effort of pack200 to avoid VM bug
  # See https://bugs.eclipse.org/bugs/show_bug.cgi?id=154069
  #java -Dorg.eclipse.update.jarprocessor.pack200=$mydir \
  #java -jar $HOME/ws2/eclipse/startup.jar \
  #java -jar ${basebuilder}/plugins/org.eclipse.equinox.launcher.jar \
  java -jar ${tgtlauncher} \
    -application org.eclipse.update.core.siteOptimizer \
    -jarProcessor -outputDir $SITE \
    -processAll -pack $SITE
  #java -Dorg.eclipse.update.jarprocessor.pack200=$mydir \
  #    $HOME/ws2/jarprocessor/jarprocessor.jar \
  #    -outputDir $SITE -processAll -pack $SITE
  ;;
esac

#Create the digest
echo "Creating digest..."
#java -jar $HOME/ws2/eclipse/startup.jar \
#java -jar ${basebuilder}/plugins/org.eclipse.equinox.launcher.jar \
java -jar ${tgtlauncher} \
    -application org.eclipse.update.core.siteOptimizer \
    -digestBuilder -digestOutputDir=$SITE \
    -siteXML=$SITE/site-europa.xml

##if false ; then
#Create P2 metadata
echo "Creating P2 metadata..."
#Always create from scratch
cd ${SITE}
for x in content.xml content.jar content.jar.pack.gz artifacts.xml artifacts.jar artifacts.jar.pack.gz ; do
  if [ -f $x ]; then rm -f $x; fi
done

if [ x${DO_STATS} = x1 ]; then
  echo "Creating P2 metadata with download stats..."
  # Sonatype / Tycho app for generating p2 download stats
  # See https://bugs.eclipse.org/bugs/show_bug.cgi?id=310132
  #  -application org.eclipse.equinox.p2.publisher.UpdateSitePublisher \
  #CMD="java -jar ${basebuilder}/plugins/org.eclipse.equinox.launcher.jar \
  #  -p2.statsTrackedBundles org.eclipse.rse.sdk,org.eclipse.dstore.core,org.eclipse.rse.core,org.eclipse.rse.useractions,org.eclipse.rse.examples.tutorial,org.eclipse.rse.tests,org.eclipse.tm.rapi,org.eclipse.tm.terminal,org.eclipse.tm.terminal.view,org.eclipse.tm.terminal.local \
  CMD="java -jar ${tgtlauncher} \
    -application org.sonatype.tycho.p2.updatesite.UpdateSitePublisherWithJRE \
    -source ${SITE} \
    -metadataRepository file:${SITE} \
    -artifactRepository file:${SITE} \
    -compress \
    -p2.statsURI http://download.eclipse.org/stats/tm \
    -p2.statsTrackedFeatures org.eclipse.rse.sdk,org.eclipse.rse.dstore,org.eclipse.rse.core,org.eclipse.rse.useractions,org.eclipse.rse.examples,org.eclipse.rse.tests,org.eclipse.rse.wince,org.eclipse.tm.terminal.view,org.eclipse.tm.terminal.local \
    -p2.statsTrackedBundles org.eclipse.rse.core,org.eclipse.rse.core.source,org.eclipse.tm.terminal \
    -p2.statsSuffix _tm330
    -vmargs -Xmx256M"
  echo $CMD
  $CMD
  result=$?
  echo "result: ${result}"

else

  echo "Creating P2 metadata (no download stats)..."
  CMD="java -jar ${tgtlauncher} \
    -application org.eclipse.equinox.p2.publisher.UpdateSitePublisher \
    -metadataRepository file:${SITE} \
    -artifactRepository file:${SITE} \
    -source ${SITE} \
    -compress \
    -publishArtifacts \
    -reusePack200Files \
    -vmargs -Xmx256M"

#	-configs any.linux.x86 \
  echo $CMD
  $CMD
  result=$?
  echo "result: ${result}"
fi
    
	if [ x${DO_CATEGORIES} = x1 ]; then
  echo "Adding Categories..."
  CMD="java -jar ${tgtlauncher} \
    -application org.eclipse.equinox.p2.publisher.CategoryPublisher \
    -metadataRepository file:${SITE}/ \
    -categoryDefinition file:${SITE}/category.xml \
    -compress"
  echo $CMD
  $CMD
  result=$?
  echo "result: ${result}"
fi

cd $SITE
chgrp -R tools.tm .
chmod -R g+w .
chmod -R a+r .
cd $curdir
