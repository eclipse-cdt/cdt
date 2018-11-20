/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 *
 * -gdb-set breakpoint pending [on | off]
 *
 * This command should be used to make breakpoints/tracepoints that are created
 * using a CLI command to be set as potentially pending breakpoints in GDB.
 *
 * Available with GDB 6.1
 *
 * @since 4.0
 *
 */
public class MIGDBSetBreakpointPending extends MIGDBSet {
	public MIGDBSetBreakpointPending(ICommandControlDMContext ctx, boolean enable) {
		super(ctx, new String[] { "breakpoint", "pending", enable ? "on" : "off" });//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
	}
}