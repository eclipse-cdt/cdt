#*******************************************************************************
# Copyright (c) 2005, 2010 IBM Corporation and others.
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
export PATH=/opt/public/common/ibm-java-ppc-605/bin:/usr/local/bin:$PATH
#export PATH=/opt/public/common/ibm-java2-ppc-50/bin/java:/usr/local/bin:$PATH

# make sure we're in the releng project dir 
cd `dirname $0`

# Checkout basebuilder to run the build
mkdir -p tools
cd tools
cvs -d /cvsroot/eclipse	co -r R36_M4 org.eclipse.releng.basebuilder
cp /home/data/httpd/download.eclipse.org/technology/subversive/0.7/pde-update-site/plugins/org.eclipse.team.svn.pde.build_0.7.8.I20090525-1500.jar \
	org.eclipse.releng.basebuilder/plugins
cd ..

# Let's go!
java $CDT_BUILD_VMARGS -jar tools/org.eclipse.releng.basebuilder/plugins/org.eclipse.equinox.launcher.jar \
	-Djvm1.5=/opt/public/common/ibm-java2-ppc-50/bin/java \
	-ws gtk -arch ppc -os linux -application org.eclipse.ant.core.antRunner $*
