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

mkdir -p tools
cd tools
cvs -d:pserver:anonymous@dev.eclipse.org:/home/eclipse \
	checkout org.eclipse.releng.basebuilder
cd ..

java -jar tools/org.eclipse.releng.basebuilder/startup.jar \
	-ws gtk -application org.eclipse.ant.core.antRunner $*
