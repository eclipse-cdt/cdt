/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class MIGDBSetBreakpointPending extends MIGDBSet 
{
    public MIGDBSetBreakpointPending(ICommandControlDMContext ctx, boolean enable) {
        super(ctx, new String[] {"breakpoint", "pending", enable ? "on" : "off"});//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
    }
}