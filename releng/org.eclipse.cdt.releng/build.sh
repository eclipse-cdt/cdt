#*******************************************************************************
# Copyright (c) 2005, 2006 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     IBM Corporation - initial API and implementation
#*******************************************************************************
umask 0022

cd `dirname $0`

# Checkout basebuilder to run the build
mkdir -p tools
cd tools
cvs -d:pserver:anonymous@dev.eclipse.org:/cvsroot/eclipse \
	checkout -r M5_34 org.eclipse.releng.basebuilder
cd ..

# Mylyn build dependencies
set mylynTag=R_2_3_2
mkdir -p mylyn
cd mylyn
cvs -d:pserver:anonymous@dev.eclipse.org:/cvsroot/tools \
	checkout -r $mylynTag org.eclipse.mylyn/org.eclipse.mylyn.context.core
cvs -d:pserver:anonymous@dev.eclipse.org:/cvsroot/tools \
	checkout -r $mylynTag org.eclipse.mylyn/org.eclipse.mylyn.context.ui
cvs -d:pserver:anonymous@dev.eclipse.org:/cvsroot/tools \
	checkout -r $mylynTag org.eclipse.mylyn/org.eclipse.mylyn.monitor.core
cvs -d:pserver:anonymous@dev.eclipse.org:/cvsroot/tools \
	checkout -r $mylynTag org.eclipse.mylyn/org.eclipse.mylyn.monitor.ui
cvs -d:pserver:anonymous@dev.eclipse.org:/cvsroot/tools \
	checkout -r $mylynTag org.eclipse.mylyn/org.eclipse.mylyn.resources.ui
cvs -d:pserver:anonymous@dev.eclipse.org:/cvsroot/tools \
	checkout -r $mylynTag org.eclipse.mylyn/org.eclipse.mylyn.tasks.core
cvs -d:pserver:anonymous@dev.eclipse.org:/cvsroot/tools \
	checkout -r $mylynTag org.eclipse.mylyn/org.eclipse.mylyn.tasks.ui
cd ..
mkdir -p results/plugins
mv mylyn/org.eclipse/mylyn/* results/plugins

java -jar tools/org.eclipse.releng.basebuilder/plugins/org.eclipse.equinox.launcher.jar \
	-ws gtk -arch ppc -os linux -application org.eclipse.ant.core.antRunner $*
