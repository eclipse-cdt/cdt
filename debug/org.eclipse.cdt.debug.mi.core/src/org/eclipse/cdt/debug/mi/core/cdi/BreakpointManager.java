/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICBreakpoint;
import org.eclipse.cdt.debug.core.cdi.ICBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICCatchEvent;
import org.eclipse.cdt.debug.core.cdi.ICCatchpoint;
import org.eclipse.cdt.debug.core.cdi.ICCondition;
import org.eclipse.cdt.debug.core.cdi.ICLocation;
import org.eclipse.cdt.debug.core.cdi.ICLocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.ICSession;
import org.eclipse.cdt.debug.core.cdi.ICWatchpoint;
import org.eclipse.cdt.debug.mi.core.MISession;

/**
 *
 */
public class BreakpointManager implements ICBreakpointManager {

	MISession session;
	
	public BreakpointManager(MISession s) {
		session = s;
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#deleteAllBreakpoints()
	 */
	public void deleteAllBreakpoints() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#deleteBreakpoint(ICBreakpoint)
	 */
	public void deleteBreakpoint(ICBreakpoint breakpoint) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#deleteBreakpoints(ICBreakpoint[])
	 */
	public void deleteBreakpoints(ICBreakpoint[] breakpoints)
		throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#getBreakpoint(String)
	 */
	public ICBreakpoint getBreakpoint(String breakpointId)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#getBreakpoints()
	 */
	public ICBreakpoint[] getBreakpoints() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#setCatchpoint(int, ICCatchEvent, String, ICCondition, boolean)
	 */
	public ICCatchpoint setCatchpoint(
		int type,
		ICCatchEvent event,
		String expression,
		ICCondition condition,
		boolean enabled)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#setLocationBreakpoint(int, ICLocation, ICCondition, boolean, String)
	 */
	public ICLocationBreakpoint setLocationBreakpoint(
		int type,
		ICLocation location,
		ICCondition condition,
		boolean enabled,
		String threadId)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#setWatchpoint(int, int, String, ICCondition, boolean)
	 */
	public ICWatchpoint setWatchpoint(
		int type,
		int watchType,
		String expression,
		ICCondition condition,
		boolean enabled)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSessionObject#getSession()
	 */
	public ICSession getSession() {
		return null;
	}

}
