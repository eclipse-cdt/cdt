/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson and others.
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

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *
 * -break-commands NUMBER [ COMMAND-1 ... COMMAND-N ]
 *
 * Specifies the CLI commands that should be executed when breakpoint NUMBER is hit.
 * The parameters COMMAND-1 to COMMAND-N are the commands. If no command is specified,
 * any previously-set commands are cleared.
 *
 * Available since GDB 7.0
 *
 * @since 3.0
 */

public class MIBreakCommands extends MICommand<MIInfo> {
	/** @since 5.0 */
	public MIBreakCommands(IBreakpointsTargetDMContext ctx, String breakpoint, String[] commands) {
		super(ctx, "-break-commands"); //$NON-NLS-1$
		if (commands == null) {
			setParameters(new String[] { breakpoint });
		} else {
			String[] params = new String[commands.length + 1];
			params[0] = breakpoint;
			for (int i = 1; i < params.length; i++) {
				params[i] = commands[i - 1];
			}
			setParameters(params);
		}
	}
}
