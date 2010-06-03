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
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * 
 *   -break-disable ( BREAKPOINT )+
 *
 * Disable the named BREAKPOINT(s).  The field `enabled' in the break
 * list is now set to `n' for the named BREAKPOINT(s).
 * 
 * Result:
 *  ^done
 */
 
public class MIBreakDisable extends MICommand<MIInfo>
{
    public MIBreakDisable (IBreakpointsTargetDMContext ctx, int[] array) {
        super(ctx, "-break-disable"); //$NON-NLS-1$
        if (array != null && array.length > 0) {
            String[] brkids = new String[array.length];
            for (int i = 0; i < array.length; i++) {
                brkids[i] = Integer.toString(array[i]);
            }
            setParameters(brkids);
        } 
    }
}
