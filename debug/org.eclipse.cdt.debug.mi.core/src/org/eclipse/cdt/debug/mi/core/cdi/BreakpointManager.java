/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICBreakpoint;
import org.eclipse.cdt.debug.core.cdi.ICBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICCatchEvent;
import org.eclipse.cdt.debug.core.cdi.ICCatchpoint;
import org.eclipse.cdt.debug.core.cdi.ICCondition;
import org.eclipse.cdt.debug.core.cdi.ICLocation;
import org.eclipse.cdt.debug.core.cdi.ICLocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.ICWatchpoint;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIBreakInsert;
import org.eclipse.cdt.debug.mi.core.output.MIBreakInsertInfo;
import org.eclipse.cdt.debug.mi.core.output.MIBreakPoint;

/**
 *
 */
public class BreakpointManager extends SessionObject implements ICBreakpointManager {

	List breakList;
	
	public BreakpointManager(Session session) {
		super(session);
		breakList = new ArrayList(1);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#deleteAllBreakpoints()
	 */
	public void deleteAllBreakpoints() throws CDIException {
		deleteBreakpoints(getBreakpoints());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#deleteBreakpoint(ICBreakpoint)
	 */
	public void deleteBreakpoint(ICBreakpoint breakpoint) throws CDIException {
		deleteBreakpoints(new ICBreakpoint[]{breakpoint});
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#deleteBreakpoints(ICBreakpoint[])
	 */
	public void deleteBreakpoints(ICBreakpoint[] breakpoints) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#getBreakpoints()
	 */
	public ICBreakpoint[] getBreakpoints() throws CDIException {
		return (ICBreakpoint[])breakList.toArray(new ICBreakpoint[breakList.size()]);
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

		boolean hardware = type == ICBreakpoint.HARDWARE;
		boolean temporary = type == ICBreakpoint.TEMPORARY;
		String exprCond = condition.getExpression();
		int ignoreCount = condition.getIgnoreCount();
		String line = "";

		if (location.getFile() != null) {
			line = location.getFile().getPath() + ":";
			if (location.getFunction() != null) {
				line += location.getFunction();
			} else {
				line += Integer.toString(location.getLineNumber());
			}
		} else {
			line = "*" + Long.toString(location.getAddress());
		}

		Session s = (Session)getSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakInsert breakInsert = factory.createMIBreakInsert(temporary, hardware,
			exprCond, ignoreCount, line);
		MIBreakPoint[] points = null;
		try {
			MIBreakInsertInfo info = breakInsert.getMIBreakInsertInfo();
			if (info == null) {
				//throw new CDIException();
			}
			points = info.getBreakPoints();
			if (points == null || points.length == 0) {
				//throw new CDIException();
			}
		} catch (MIException e) {
			// throw new CDIException(e);
		}

		Breakpoint bkpt= new Breakpoint(this, points[0]);
		breakList.add(bkpt);
		return bkpt;
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
}
