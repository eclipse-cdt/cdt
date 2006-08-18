#!/bin/sh
# Convert normal "site.xml" to "testUpdates"
#
# Prerequisites: 
# - Eclipse 3.2 installed in $HOME/ws/eclipse
# - Java5 in the PATH or in /shared/common/ibm-java2-ppc64-50

curdir=`pwd`
cd `dirname $0`
mydir=`pwd`

umask 002

#Use Java5 on build.eclipse.org - need JRE for pack200
export PATH=/shared/common/ibm-java2-ppc64-50/jre/bin:/shared/common/ibm-java2-ppc64-50/bin:$PATH

# patch site.xml
cd ..
SITE=`pwd`
rm site.xml web/site.xsl
cvs -q update -dPR
if [ -f site.xml.new ]; then
  rm -f site.xml.new
fi
if [ -f web/site.xsl.new ]; then
  rm -f web/site.xsl.new
fi

# get newest plugins and features: to be done manually on real update site
if [ `basename $SITE` = testUpdates ]; then
    echo "Working on test update site"
    REL=`ls $HOME/ws/working/package | sort | tail -1`
    if [ "$REL" != "" ]; then
      echo "Checking new Updates from $REL"
      DIR="$HOME/ws/working/package/$REL/updates"
      if [ -d "$DIR/features" ]; then
        echo "Copying new plugins and features from $DIR"
        rm -rf features
        rm -rf plugins
        cp -R $DIR/features .
        cp -R $DIR/plugins .
      fi
    fi
    sed -e 's,/dsdp/tm/updates,/dsdp/tm/testUpdates,g' \
        -e 's,Project Update,Project Test Update,g' \
        site.xml > site.xml.new
    mv -f site.xml.new site.xml
    sed -e 's,Project Update,Project Test Update,g' \
    	web/site.xsl > web/site.xsl.new
    mv -f web/site.xsl.new web/site.xsl
else
    echo "Working on official update site"
fi
FEATURES=`grep 'features/[^ ]*\.qualifier\.jar' site.xml | sed -e 's,^[^"]*"features/\([^_]*_[0-9.]*\).*$,\1,g'`
for feature in $FEATURES ; do
  #list newest ones first
  TAG=`ls -t features/${feature}*.jar | head -1 | sed -e 's,[^_]*_[0-9]*\.[0-9]*\.[0-9]*\.\([^.]*\).jar,\1,'`
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
java -Dorg.eclipse.update.jarprocessor.pack200=$mydir \
    -jar $HOME/ws/eclipse/startup.jar \
    -application org.eclipse.update.core.siteOptimizer \
    -jarProcessor -outputDir $SITE \
    -processAll -pack $SITE

#Create the digest
echo "Creating digest..."
java -jar $HOME/ws/eclipse/startup.jar \
    -application org.eclipse.update.core.siteOptimizer \
    -digestBuilder -digestOutputDir=$SITE \
    -siteXML=$SITE/site.xml

cd $SITE
chgrp -R dsdp-tm-rse .
chmod -R g+w .
cd $curdir