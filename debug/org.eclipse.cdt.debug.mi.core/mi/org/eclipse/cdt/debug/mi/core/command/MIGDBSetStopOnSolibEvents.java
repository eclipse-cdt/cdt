/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -gdb-set stop-on-solib-events
 *
 *   Set an internal GDB variable.
 * 
 */
public class MIGDBSetStopOnSolibEvents extends MIGDBSet {
	public MIGDBSetStopOnSolibEvents(boolean isSet) {
		super(new String[] {"stop-on-solib-events", (isSet) ? "1" : "0"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
