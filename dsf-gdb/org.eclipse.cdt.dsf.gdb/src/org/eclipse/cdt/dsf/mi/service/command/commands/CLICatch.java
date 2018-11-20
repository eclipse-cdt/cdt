/*******************************************************************************
 * Copyright (c) 2010, 2016 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLICatchInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * gdb 'catch' command. Even though the command has been around since gdb 6.6,
 * it's still not supported in gdb 7.0 MI.
 *
 * @since 3.0
 */
public class CLICatch extends CLICommand<CLICatchInfo> {

	private static String formOperation(String event, String[] args) {
		StringBuilder oper = new StringBuilder("catch ").append(event); //$NON-NLS-1$
		for (String arg : args) {
			oper.append(' ').append(arg);
		}
		return oper.toString();
	}

	/**
	 * Constructor
	 * @param ctx the context for the command
	 * @param event the type of event to be caught; one of the keywords documented in 'help catch'
	 * @param args zero or more arguments particular to the 'event'
	 */
	public CLICatch(IBreakpointsTargetDMContext ctx, String event, String[] args) {
		super(ctx, formOperation(event, args));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.mi.service.command.commands.MICommand#getResult(org.eclipse.cdt.dsf.mi.service.command.output.MIOutput)
	 */
	@Override
	public MIInfo getResult(MIOutput MIresult) {
		return new CLICatchInfo(MIresult);
	}
}
