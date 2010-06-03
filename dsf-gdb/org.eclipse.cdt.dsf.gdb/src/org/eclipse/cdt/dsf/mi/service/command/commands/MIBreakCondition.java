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
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * 
 *  -break-condition NUMBER EXPR
 *    
 *  Breakpoint NUMBER will stop the program only if the condition in
 *  EXPR is true.  The condition becomes part of the `-break-list' output
 *  (see the description of the DsfMIBreakList).
 */
 
public class MIBreakCondition extends MICommand<MIInfo>
{
	// In this particular case, because of a GDB peculiarity, setParameters() is 
	// not used and the whole command is formatted on the parent's constructor.
	// See bug 213076 for more information.

	public MIBreakCondition(IBreakpointsTargetDMContext ctx, int breakpoint, String condition) {
        super(ctx, "-break-condition " + Integer.toString(breakpoint) + " " + condition); //$NON-NLS-1$ //$NON-NLS-2$
//        super(ctx, "-break-condition"); //$NON-NLS-1$
//        setParameters(new String[] { Integer.toString(breakpoint), condition });
    }
}
