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
TAG=`ls features | head -1 | sed -e 's,[^_]*_[0-9.]*\([^.]*\).jar,\1,'`
rm site.xml
cvs -q update -d
if [ `basename $SITE` = testUpdates ]; then
    echo "Working on test update site"
    sed -e 's,/dsdp/tm/updates,/dsdp/tm/testUpdates,g' \
        -e 's,Project Update Site,Project Test Update Site,g' \
        site.xml > site.xml.new
    mv -f site.xml.new site.xml
else
    echo "Working on official update site"
fi
sed -e "s,200607201800,$TAG,g" \
    site.xml > site.xml.new
mv -f site.xml.new site.xml
chgrp dsdp-tm-rse site.xml

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
