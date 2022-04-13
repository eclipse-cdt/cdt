/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Need a new FinalLaunchSequence for GDB 12.0
 * @since 6.6
 */
public class GDBControl_12_0 extends GDBControl_9_0 {
	public GDBControl_12_0(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
		super(session, config, factory);
	}

	@Override
	protected boolean isWorkaroundForBufferedMIStreamNeeded() {
		// Disable workaround for https://sourceware.org/bugzilla/show_bug.cgi?id=28711
		return false;
	}
}
