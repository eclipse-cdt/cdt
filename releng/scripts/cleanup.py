###############################################################################
# Copyright (c) 2018, 2020 Kichwa Coders Ltd and others.
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################

# This script is used as part of the rebase_helper.sh script

loadModule('/System/UI')
while getActiveEditor():
	# We need to cleanup twice. The first one may remove some code (like unneeded
	# type parameters) that then needs to be reformatted.
	executeUI("org.eclipse.jdt.internal.ui.actions.AllCleanUpsAction(getActiveEditor()).run()")
	executeUI("org.eclipse.jdt.internal.ui.actions.AllCleanUpsAction(getActiveEditor()).run()")
	executeUI("getActiveEditor().close(True)")
