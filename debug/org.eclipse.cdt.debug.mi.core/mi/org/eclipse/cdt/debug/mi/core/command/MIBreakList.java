/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIBreakListInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

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
 * `Type'
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
public class MIBreakList extends MICommand
{
	public MIBreakList () {
		super("-break-list"); //$NON-NLS-1$
	}

	public MIBreakListInfo getMIBreakListInfo() throws MIException {
		return (MIBreakListInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIBreakListInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
