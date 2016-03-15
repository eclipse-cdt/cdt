/*******************************************************************************
 * Copyright (c) 2007, 2016 Ericsson and others.
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
 *  -break-after NUMBER COUNT
 *
 *  The breakpoint number NUMBER is not in effect until it has been hit
 *  COUNT times.  The count becomes part of the `-break-list' output
 *  (see the description of the DsfMIBreakList).
 */
 
public class MIBreakAfter extends MICommand<MIInfo>
{
    /** @since 5.0 */
    public MIBreakAfter(IBreakpointsTargetDMContext ctx, String breakpoint, int ignoreCount) {
        super(ctx, "-break-after", new String[] { breakpoint, Integer.toString(ignoreCount) }); //$NON-NLS-1$
    }
}
