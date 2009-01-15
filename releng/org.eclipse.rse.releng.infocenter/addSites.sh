#!/bin/sh
#*******************************************************************************
# Copyright (c) 2006, 2007 Wind River Systems, Inc.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html 
# 
# Contributors: 
# Martin Oberhuber - initial API and implementation 
#*******************************************************************************
#
# Add new Eclipse "extension locations" to the infocenter
#
IHOME=/home/infocenter/latest
$IHOME/bin/infocenter.sh addSite -from $IHOME/plugins/rse/eclipse
$IHOME/bin/infocenter.sh addSite -from $IHOME/plugins/dd.dsf/eclipse
$IHOME/bin/infocenter.sh addSite -from $IHOME/plugins/nab/eclipse
$IHOME/bin/infocenter.sh addSite -from $IHOME/plugins/ercp/eclipse
$IHOME/bin/infocenter.sh addSite -from $IHOME/plugins/mtj/eclipse
$IHOME/bin/infocenter.sh apply
