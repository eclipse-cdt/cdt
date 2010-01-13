#*******************************************************************************
# Copyright (c) 2005, 2008 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     IBM Corporation - initial API and implementation
#*******************************************************************************

# The CDT build script, set up the environment to run the build.xml ant script
# We are running on build.eclipse.org

# export display for running the tests
export DISPLAY=:1

# set up to use the Java 5 JRE
export PATH=/opt/public/common/ibm-java2-ppc-50/bin:$PATH

# make sure we're in the releng project dir 
cd `dirname $0`

# make sure the umask doesn't allow for group execute permissions\
# TODO not sure we really need this any more
umask 0022

# Checkout basebuilder to run the build
mkdir -p tools
cd tools
cvs -d:pserver:anonymous@dev.eclipse.org:/cvsroot/eclipse \
	checkout -r R36_M3 org.eclipse.releng.basebuilder
cd ..

# Let's go!
java $CDT_BUILD_VMARGS -jar tools/org.eclipse.releng.basebuilder/plugins/org.eclipse.equinox.launcher.jar \
	-ws gtk -arch ppc -os linux -application org.eclipse.ant.core.antRunner $*

# Copy the build.log to where the latest build is
cp /opt/public/download-staging.priv/tools/cdt/releng/build.log /home/www/tools/cdt/builds/7.0.0/latest
