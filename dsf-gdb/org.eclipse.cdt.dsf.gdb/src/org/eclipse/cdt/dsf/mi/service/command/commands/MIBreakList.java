/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * 
 *   -break-list
 *
 *   Displays the list of inserted breakpoints, showing the following
 * fields:
 *
 * `Number'
 *     number of the breakpoint
 *
 * `State'
 *     type of the breakpoint: `breakpoint' or `watchpoint'
 *
 * `Disposition'
 *     should the breakpoint be deleted or disabled when it is hit: `keep'
 *     or `nokeep'
 *
 * `Enabled'
 *     is the breakpoint enabled or no: `y' or `n'
 *
 * `Address'
 *     memory location at which the breakpoint is set
 *
 * `What'
 *     logical location of the breakpoint, expressed by function name,
 *
 * `Times'
 *     number of times the breakpoint has been hit
 *
 *   If there are no breakpoints or watchpoints, the `BreakpointTable'
 *   `body' field is an empty list.
 *
 */
public class MIBreakList extends MICommand<MIBreakListInfo>
{
    public MIBreakList (IBreakpointsTargetDMContext ctx) {
        super(ctx, "-break-list"); //$NON-NLS-1$
    }
    
    @Override
    public MIBreakListInfo getResult(MIOutput output) {
        return new MIBreakListInfo(output);
    }
}
