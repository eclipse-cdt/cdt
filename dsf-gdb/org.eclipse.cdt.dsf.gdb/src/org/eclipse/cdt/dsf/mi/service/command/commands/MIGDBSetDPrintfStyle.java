/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * -gdb-set dprintf-style STYLE
 *
 * Set the dprintf output to be handled in one of several different styles enumerated below.
 * A change of style affects all existing dynamic printfs immediately.
 *
 *   gdb
 *     Handle the output using the gdb printf command.
 *   call
 *     Handle the output by calling a function in your program (normally printf).
 *   agent
 *     Have the remote debugging agent (such as gdbserver) handle the output itself.
 *     This style is only available for agents that support running commands on the target.
 *
 * @since 4.4
 */
public class MIGDBSetDPrintfStyle extends MIGDBSet {

	public static final String GDB_STYLE = "gdb"; //$NON-NLS-1$
	public static final String CALL_STYLE = "call"; //$NON-NLS-1$
	public static final String AGENT_STYLE = "agent"; //$NON-NLS-1$

	public MIGDBSetDPrintfStyle(ICommandControlDMContext dmc, String style) {
		super(dmc, new String[] { "dprintf-style", style }); //$NON-NLS-1$
	}
}
