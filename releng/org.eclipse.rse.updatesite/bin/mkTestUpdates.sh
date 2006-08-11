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

# patch site.xml
cd ..
SITE=`pwd`
rm site.xml web/site.xsl
cvs -q update -d
if [ `basename $SITE` = testUpdates ]; then
    echo "Working on test update site"
    REL==`ls $HOME/ws/working/package | sort | tail -1`
    if [ "$REL" != "" ]; then
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
TAG=`ls features | head -1 | sed -e 's,[^_]*_[0-9.]*\([^.]*\).jar,\1,'`
sed -e "s,200607201800,$TAG,g" \
    site.xml > site.xml.new
mv -f site.xml.new site.xml

# optimize the site
# see http://wiki.eclipse.org/index.php/Platform-releng-faq
#Use Java5 on build.eclipse.org
export PATH=/shared/common/ibm-java2-ppc64-50/bin:$PATH
#Pack the site
echo "Packing the site... $SITE"
java -jar $HOME/ws/eclipse/startup.jar \
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
