/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICatchEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDICatchpoint;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;

/**
 */
public class Catchpoint extends Breakpoint implements ICDICatchpoint {

	public Catchpoint(BreakpointManager m, MIBreakpoint miBreak) {
		super(m, miBreak);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDICatchpoint#getEvent()
	 */
	public ICDICatchEvent getEvent() throws CDIException {
		return null;
	}
}
