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
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * Set the passcount of a tracepoint. The passcount is a way to automatically stop a trace experiment. 
 * If a tracepoint's passcount is N, then the trace experiment will be automatically stopped on the N'th
 * time that tracepoint is hit.  If no passcount is given, the trace experiment will run until stopped 
 * explicitly by the user.

 * @since 3.0
 */
public class CLIPasscount extends CLICommand<MIInfo> {
    public CLIPasscount(IBreakpointsTargetDMContext ctx, int breakpoint, int passcount) {
        super(ctx, "passcount"); //$NON-NLS-1$
		setParameters(new String[] { Integer.toString(passcount), Integer.toString(breakpoint) });
    }
}

