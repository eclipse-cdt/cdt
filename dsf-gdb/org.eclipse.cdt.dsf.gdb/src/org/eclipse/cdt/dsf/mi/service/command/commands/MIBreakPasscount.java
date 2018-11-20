/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
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
 * -break-passcount TRACEPOINT-NUMBER PASSCOUNT
 *
 * Set the passcount for tracepoint TRACEPOINT_NUMBER to PASSCOUNT. If the breakpoint
 * referred to by TRACEPOINT_NUMBER is not a tracepoint, an error is emitted. This
 * corresponds to the CLI command 'passcount'.
 *
 * Available starting with GDB 7.1
 *
 * @since 3.0
 */

public class MIBreakPasscount extends MICommand<MIInfo> {
	public MIBreakPasscount(IBreakpointsTargetDMContext ctx, int tracepoint, int passCount) {
		super(ctx, "-break-passcount", null, //$NON-NLS-1$
				new String[] { Integer.toString(tracepoint), Integer.toString(passCount) });
	}
}
