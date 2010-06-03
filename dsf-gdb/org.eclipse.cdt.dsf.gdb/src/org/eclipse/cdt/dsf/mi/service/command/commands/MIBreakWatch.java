/*******************************************************************************
 * Copyright (c) 2007, 2009 Ericsson and others.
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
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * -break-watch [ -a | -r ] LOCATION
 *  
 * Create a watchpoint on LOCATION. Without either of the options, the watchpoint
 * created is a regular watchpoint, i.e., a watchpoint that triggers when the
 * memory LOCATION is accessed for writing.

 * The possible optional parameters of this command are:
 *
 * '-a'
 *      Creates an access watchpoint i.e. a watchpoint that triggers either
 *      on a read from or on a write to the memory location.
 *
 * '-r'
 *      Creates a read watchpoint i.e. a watchpoint that triggers only when
 *      the memory location is accessed for reading.
 */
public class MIBreakWatch extends MICommand<MIBreakInsertInfo> 
{
	public MIBreakWatch(IBreakpointsTargetDMContext ctx, boolean isRead, boolean isWrite, String expression)
	{
		super(ctx, "-break-watch"); //$NON-NLS-1$

		if (isRead) {
			if (isWrite)
	        	setOptions(new String[] { "-a" });  //$NON-NLS-1$
	        else
	        	setOptions(new String[] { "-r" });  //$NON-NLS-1$
		}

        setParameters(new String[]{ expression });
    }

    @Override
    public MIBreakInsertInfo getResult(MIOutput output) {
        return new MIBreakInsertInfo(output);
    }
}
