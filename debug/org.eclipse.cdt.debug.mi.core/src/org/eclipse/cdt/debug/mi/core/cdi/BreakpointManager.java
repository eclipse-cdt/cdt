/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDICatchEvent;
import org.eclipse.cdt.debug.core.cdi.ICDICatchpoint;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIBreakDelete;
import org.eclipse.cdt.debug.mi.core.command.MIBreakDisable;
import org.eclipse.cdt.debug.mi.core.command.MIBreakEnable;
import org.eclipse.cdt.debug.mi.core.command.MIBreakInsert;
import org.eclipse.cdt.debug.mi.core.command.MIBreakWatch;
import org.eclipse.cdt.debug.mi.core.output.MIBreakInsertInfo;
import org.eclipse.cdt.debug.mi.core.output.MIBreakPoint;
import org.eclipse.cdt.debug.mi.core.output.MIBreakWatchInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;

/**
 *
 */
public class BreakpointManager extends SessionObject implements ICDIBreakpointManager {

	List breakList;
	
	public BreakpointManager(CSession session) {
		super(session);
		breakList = new ArrayList(1);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#deleteAllBreakpoints()
	 */
	public void deleteAllBreakpoints() throws CDIException {
		deleteBreakpoints(getBreakpoints());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#deleteBreakpoint(ICDIBreakpoint)
	 */
	public void deleteBreakpoint(ICDIBreakpoint breakpoint) throws CDIException {
		deleteBreakpoints(new ICDIBreakpoint[]{breakpoint});
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#deleteBreakpoints(ICDIBreakpoint[])
	 */
	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
		int[] numbers = new int[breakpoints.length];
		for (int i = 0; i < numbers.length; i++) {
			if (breakpoints[i] instanceof Breakpoint
				&& breakList.contains(breakpoints[i])) {
				numbers[i] = ((Breakpoint)breakpoints[i]).getMIBreakPoint().getNumber();
			} else {
				throw new CDIException("Not a CDT breakpoint");
			}
		}
		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakDelete breakDelete = factory.createMIBreakDelete(numbers);
		try {
			s.getMISession().postCommand(breakDelete);
			MIInfo info = breakDelete.getMIInfo();
			if (info == null) {
				throw new CDIException("Timedout");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
		for (int i = 0; i < breakpoints.length; i++) {
			breakList.remove(breakpoints[i]);
		}
	}

	public void enableBreakpoint(ICDIBreakpoint breakpoint) throws CDIException {
		int number = 0;
		if (breakpoint instanceof Breakpoint && breakList.contains(breakpoint)) {
			number = ((Breakpoint)breakpoint).getMIBreakPoint().getNumber();
		} else {
			throw new CDIException("Not a CDT breakpoint");
		}
		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakEnable breakEnable = factory.createMIBreakEnable(new int[]{number});
		try {
			s.getMISession().postCommand(breakEnable);
			MIInfo info = breakEnable.getMIInfo();
			if (info == null) {
				throw new CDIException("Timedout");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
		((Breakpoint)breakpoint).getMIBreakPoint().setEnabled(true);
	}

	public void disableBreakpoint(ICDIBreakpoint breakpoint) throws CDIException {
		int number = 0;
		if (breakpoint instanceof Breakpoint && breakList.contains(breakpoint)) {
			number = ((Breakpoint)breakpoint).getMIBreakPoint().getNumber();
		} else {
			throw new CDIException("Not a CDT breakpoint");
		}
		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakDisable breakDisable = factory.createMIBreakDisable(new int[]{number});
		try {
			s.getMISession().postCommand(breakDisable);
			MIInfo info = breakDisable.getMIInfo();
			if (info == null) {
				throw new CDIException("Timeout");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
		((Breakpoint)breakpoint).getMIBreakPoint().setEnabled(false);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#getBreakpoints()
	 */
	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		return (ICDIBreakpoint[])breakList.toArray(new ICDIBreakpoint[breakList.size()]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#setCatchpoint(int, ICDICatchEvent, String, ICDICondition, boolean)
	 */
	public ICDICatchpoint setCatchpoint(int type, ICDICatchEvent event, String expression,
		ICDICondition condition) throws CDIException {
		throw new CDIException("Not Supported");
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#setLocationBreakpoint(int, ICDILocation, ICDICondition, boolean, String)
	 */
	public ICDILocationBreakpoint setLocationBreakpoint(int type, ICDILocation location,
		ICDICondition condition, String threadId) throws CDIException {

		boolean hardware = (type == ICDIBreakpoint.HARDWARE);
		boolean temporary = (type == ICDIBreakpoint.TEMPORARY);
		String exprCond = null;
		int ignoreCount = 0;
		String line = "";

		if (condition != null) {
			exprCond = condition.getExpression();
			ignoreCount = condition.getIgnoreCount();
		}

		if (location != null) {
			if (location.getFile() != null) {
				line = location.getFile() + ":";
				if (location.getFunction() != null) {
					line += location.getFunction();
				} else {
					line += Integer.toString(location.getLineNumber());
				}
			} else {
				line = "*" + Long.toString(location.getAddress());
			}
		}

		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakInsert breakInsert = factory.createMIBreakInsert(temporary, hardware,
			exprCond, ignoreCount, line);
		MIBreakPoint[] points = null;
		try {
			s.getMISession().postCommand(breakInsert);
			MIBreakInsertInfo info = breakInsert.getMIBreakInsertInfo();
			if (info == null) {
				throw new CDIException("Timedout");
			}
			points = info.getBreakPoints();
			if (points == null || points.length == 0) {
				throw new CDIException("Error parsing");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}

		Breakpoint bkpt= new Breakpoint(this, points[0]);
		breakList.add(bkpt);
		return bkpt;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#setWatchpoint(int, int, String, ICDICondition, boolean)
	 */
	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression,
		ICDICondition condition) throws CDIException {
		boolean access = (type == ICDIWatchpoint.WRITE);
		boolean read = (type == ICDIWatchpoint.READ);

		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakWatch breakWatch = factory.createMIBreakWatch(access, read, expression);
		MIBreakPoint[] points = null;
		try {
			s.getMISession().postCommand(breakWatch);
			MIBreakWatchInfo info = breakWatch.getMIBreakWatchInfo();
			if (info == null) {
				throw new CDIException("Timedout");
			}
			points = info.getBreakPoints();
			if (points == null || points.length == 0) {
				throw new CDIException("Parsing Error");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}

		Watchpoint bkpt= new Watchpoint(this, points[0]);
		breakList.add(bkpt);
		return bkpt;
	}
}
