/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;

/**
 */
public class Watchpoint extends Breakpoint implements ICDIWatchpoint {

	public Watchpoint(BreakpointManager m, MIBreakpoint miBreak) {
		super(m, miBreak);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#getWatchExpression()
	 */
	public String getWatchExpression() throws CDIException {
		return getMIBreakpoint().getWhat();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#isReadType()
	 */
	public boolean isReadType() {
		return getMIBreakpoint().isReadWatchpoint() || getMIBreakpoint().isAccessWatchpoint();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#isWriteType()
	 */
	public boolean isWriteType() {
		return getMIBreakpoint().isAccessWatchpoint() || getMIBreakpoint().isWriteWatchpoint();
	}

}
