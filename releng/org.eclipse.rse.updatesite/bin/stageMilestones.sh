#*******************************************************************************
# Copyright (c) 2007 Wind River Systems, Inc. and others.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html 
# 
# Contributors: 
# Martin Oberhuber (Wind River) - initial API and implementation
#*******************************************************************************
#!/bin/sh
curdir=`pwd`
cd `dirname $0`
mydir=`pwd`

umask 022

#Use Java5 on build.eclipse.org - need JRE for pack200
export PATH=/shared/dsdp/tm/jdk-1.5/jre/bin:/shared/dsdp/tm/jdk-1.5/bin:$PATH
basebuilder=${HOME}/ws2/org.eclipse.releng.basebuilder

# patch site.xml
cd ..
SITE=`pwd`

# get newest plugins and features: to be done manually on real update site
if [ `basename $SITE` != milestones ]; then
    echo "Must run on milestones update site"
    cd "$curdir"
    exit 1
fi

# store away previous version
echo "Storing away old versions"
if [ -d features.sav ]; then
    rm -rf features.sav
fi
if [ -d plugins.sav ]; then
    rm -rf plugins.sav
fi
mkdir features.sav
mkdir plugins.sav
mv features/*_2.0.1.v* features.sav
mv features/*_1.0.1.v* features.sav
mv plugins/*_2.0.1.v* plugins.sav
mv plugins/*_1.0.1.v* plugins.sav

# copy new version
echo "Copying new versions"
cp ../../signedUpdates/features/*_2.0.1.v* features
cp ../../signedUpdates/features/*_1.0.1.v* features
cp ../../signedUpdates/plugins/*_2.0.1.v* plugins
cp ../../signedUpdates/plugins/*_1.0.1.v* plugins

# diff, to be safe
ls features.sav | sort > f1.$$.txt
ls plugins.sav | sort > p1.$$.txt
ls features | grep '_[12]\.0\.1\.v' | sort > f2.$$.txt
ls plugins | grep  '_[12]\.0\.1\.v' | sort > p2.$$.txt
echo "Plugins diff:"
diff p1.$$.txt p2.$$.txt
echo "Features diff:"
diff f1.$$.txt f2.$$.txt
rm f1.$$.txt f2.$$.txt p1.$$.txt p2.$$.txt

cd "$curdir"
exit 0