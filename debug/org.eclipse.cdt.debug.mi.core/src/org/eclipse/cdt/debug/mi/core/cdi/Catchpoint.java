package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICatchEvent;
import org.eclipse.cdt.debug.core.cdi.ICDICatchpoint;
import org.eclipse.cdt.debug.mi.core.output.MIBreakPoint;

/**
 */
public class Catchpoint extends Breakpoint implements ICDICatchpoint {

	public Catchpoint(BreakpointManager m, MIBreakPoint miBreak) {
		super(m, miBreak);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDICatchpoint#getEvent()
	 */
	public ICDICatchEvent getEvent() throws CDIException {
		return null;
	}
}
