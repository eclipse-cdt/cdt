/*******************************************************************************
 * Copyright (c) 2009, 2016 Ericsson and others.
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
 * Set the passcount of a tracepoint. The passcount is a way to automatically stop a trace experiment.
 * If a tracepoint's passcount is N, then the trace experiment will be automatically stopped on the N'th
 * time that tracepoint is hit.  If no passcount is given, the trace experiment will run until stopped
 * explicitly by the user.

 * @since 3.0
 */
public class CLIPasscount extends CLICommand<MIInfo> {
	/** @since 5.0 */
	public CLIPasscount(IBreakpointsTargetDMContext ctx, String breakpoint, int passcount) {
		super(ctx, "passcount"); //$NON-NLS-1$
		setParameters(new String[] { Integer.toString(passcount), breakpoint });
	}
}
