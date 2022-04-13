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
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Need a new FinalLaunchSequence for GDB 9.0
 * @since 6.6
 */
public class GDBControl_9_0 extends GDBControl_7_12 {
	public GDBControl_9_0(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
		super(session, config, factory);
	}

	@Override
	protected boolean isWorkaroundForBufferedMIStreamNeeded() {
		// Enable workaround for https://sourceware.org/bugzilla/show_bug.cgi?id=28711 (Linux only)
		return Platform.getOS().equals(Platform.OS_LINUX);
	}
}
