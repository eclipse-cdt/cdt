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
