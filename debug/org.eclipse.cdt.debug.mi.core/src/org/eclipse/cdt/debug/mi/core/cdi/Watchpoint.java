/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.output.MIBreakPoint;

/**
 */
public class Watchpoint extends Breakpoint implements ICDIWatchpoint {

	public Watchpoint(BreakpointManager m, MIBreakPoint miBreak) {
		super(m, miBreak);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#getWatchExpression()
	 */
	public String getWatchExpression() throws CDIException {
		return getMIBreakPoint().getWhat();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#isReadType()
	 */
	public boolean isReadType() {
		return getMIBreakPoint().isReadWatchpoint() || getMIBreakPoint().isAccessWatchpoint();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#isWriteType()
	 */
	public boolean isWriteType() {
		return getMIBreakPoint().isAccessWatchpoint() || getMIBreakPoint().isWriteWatchpoint();
	}

}
