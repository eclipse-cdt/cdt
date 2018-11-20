/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.io.IOException;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;

/**
 * Note that starting with GDB 7.12, as long as a PTY is available, this process
 * is used instead of GDBBackendProcess. This is because the GDB CLI is handled
 * directly by GDB and the current class only needs to handle the life-cycle of
 * the GDB process.
 *
 * This class is therefore a representation of the GDB process that will be
 * added to the launch. This class is not the real GDB process but simply an
 * entry for the launch to handle user actions but no IO.
 *
 * This class extends {@link GDBBackendCLIProcess} to re-use its implementation
 * of the {@link Process} abstract methods, but disables all I/O and
 * local CLI handling.
 *
 * @since 5.2
 */
public class GDBBackendProcessWithoutIO extends GDBBackendCLIProcess implements IGDBBackendProcessWithoutIO {

	public GDBBackendProcessWithoutIO(ICommandControlService commandControl, IMIBackend backend) throws IOException {
		super(commandControl, backend);
	}

	@Override
	public boolean handleIO() {
		// Streams are handled directly by the real process.
		// This class is just representation for the launch, without IO.
		return false;
	}
}
