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
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;

/**
 */
public class Watchpoint extends Breakpoint implements ICDIWatchpoint {

	int watchType;
	String what;

	public Watchpoint(BreakpointManager m, String expression, int type, int wType, ICDICondition cond) {
		super(m, type, null, cond, ""); //$NON-NLS-1$
		watchType = wType;
		what = expression;
	}

	public Watchpoint(BreakpointManager m, MIBreakpoint miBreak) {
		super(m, miBreak);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#getWatchExpression()
	 */
	public String getWatchExpression() throws CDIException {
		MIBreakpoint miPoint = getMIBreakpoint();
		if (miPoint != null)
			return getMIBreakpoint().getWhat();
		return what;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#isReadType()
	 */
	public boolean isReadType() {
		MIBreakpoint miPoint = getMIBreakpoint();
		if (miPoint != null)
			return getMIBreakpoint().isReadWatchpoint() || getMIBreakpoint().isAccessWatchpoint();
		return ((watchType & ICDIWatchpoint.READ) == ICDIWatchpoint.READ);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#isWriteType()
	 */
	public boolean isWriteType() {
		MIBreakpoint miPoint = getMIBreakpoint();
		if (miPoint != null)
			return getMIBreakpoint().isAccessWatchpoint() || getMIBreakpoint().isWriteWatchpoint();
		return ((watchType & ICDIWatchpoint.WRITE) == ICDIWatchpoint.WRITE);
	}

}
