#!/bin/bash
###############################################################################
# Copyright (c) 2018, 2022 Kichwa Coders Ltd and others.
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################


mkdir -p /tmp/eplv2
curl -s https://www.eclipse.org/legal/epl/epl-2.0/about.html > /tmp/eplv2/about.html
curl -s http://www.eclipse.org/legal/epl/notice.html > /tmp/eplv2/notice.html
curl -s http://www.eclipse.org/legal/epl/notice.html > /tmp/eplv2/license.html

find . -name about.html   -exec cp /tmp/eplv2/about.html {} \;
find . -name license.html   -exec cp /tmp/eplv2/license.html {} \;
find . -name notice.html   -exec cp /tmp/eplv2/notice.html {} \;
