/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
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

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoBreakInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * 'info break [BP_REFERENCE]
 *
 * will return information about the specified breakpoint.  If no breakpoint is
 * specified, it will return information about all breakpoints.
 * We use it to find out to which inferior a breakpoint is applicable.
 * This information is not available from -break-list or -break-info until GDB 7.6.
 *
 * @since 4.2
 */
public class CLIInfoBreak extends CLICommand<CLIInfoBreakInfo> {

	private static final String INFO_BREAK = "info break"; //$NON-NLS-1$

	public CLIInfoBreak(IDMContext ctx) {
		super(ctx, INFO_BREAK);
	}

	public CLIInfoBreak(IDMContext ctx, int bpReference) {
		super(ctx, INFO_BREAK + Integer.toString(bpReference));
	}

	@Override
	public CLIInfoBreakInfo getResult(MIOutput MIresult) {
		return new CLIInfoBreakInfo(MIresult);
	}
}
