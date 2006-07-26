#!/bin/sh
# Convert normal "site.xml" to "testUpdates"

curdir=`pwd`
cd `dirname $0`
mydir=`pwd`

umask 002

# patch site.xml
cd ..
TAG=`ls plugins | head -1 | sed -e 's,[^_]*_[0-9.]*\([^.]*\).jar,\1,'`
rm site.xml
cvs -q update -d
sed -e 's,/dsdp/tm/updates,/dsdp/tm/testUpdates,g' \
    -e "s,200607201800,$TAG,g" \
    site.xml > site.xml.new
chgrp dsdp-tm-rse site.xml.new
mv -f site.xml.new site.xml

# optimize the site
# see http://wiki.eclipse.org/index.php/Platform-releng-faq
cd ..
site=`pwd`
cd $HOME/ws/eclipse
#Use Java5 on build.eclipse.org
export PATH=/shared/common/ibm-java2-ppc64-50/bin:$PATH
#Pack the site
java -jar startup.jar \
    -application org.eclipse.update.core.siteOptimizer 
    -jarProcessor -outputDir $site
    -processAll -pack $site

#Create the digest
java -jar startup.jar \
    -application org.eclipse.update.core.siteOptimizer \
    -digestBuilder -digestOutputDir=$site \
    -siteXML=$site/site.xml
