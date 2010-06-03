/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLITraceInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * This command creates a tracepoint.
 * @since 3.0
 */
public class CLITrace extends CLICommand<CLITraceInfo> {
	public CLITrace(IBreakpointsTargetDMContext ctx, String location) {
		this(ctx, location, null);
	}

	// In this particular case, because of a GDB peculiarity, setParameters() is 
	// not used and the whole command is formatted on the parent's constructor.
	// See bug 213076 for more information.
	public CLITrace(IBreakpointsTargetDMContext ctx, String location, String condition) {
		super(ctx, "trace " + location + //$NON-NLS-1$
				   ((condition != null && condition.trim().length() > 0) ? " if " + condition : "")); //$NON-NLS-1$  //$NON-NLS-2$

	}
	
	@Override
	public CLITraceInfo getResult(MIOutput output) {
		return (CLITraceInfo)getMIInfo(output);
	}

	public MIInfo getMIInfo(MIOutput out) {
		MIInfo info = null;
		if (out != null) {
			info = new CLITraceInfo(out);
		}
		return info;
	}
}

