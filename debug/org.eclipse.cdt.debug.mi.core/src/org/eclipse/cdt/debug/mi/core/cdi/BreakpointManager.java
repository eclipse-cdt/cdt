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
public class BreakpointManager extends SessionObject implements ICBreakpointManager {

	List breakList;
	
	public BreakpointManager(CSession session) {
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
		int[] numbers = new int[breakpoints.length];
		for (int i = 0; i < numbers.length; i++) {
			if (breakpoints[i] instanceof Breakpoint
				&& breakList.contains(breakpoints[i])) {
				numbers[i] = ((Breakpoint)breakpoints[i]).getMIBreakPoint().getNumber();
			} else {
				//throw new CDIException();
			}
		}
		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakDelete breakDelete = factory.createMIBreakDelete(numbers);
		try {
			s.getMISession().postCommand(breakDelete);
			MIInfo info = breakDelete.getMIInfo();
			if (info == null) {
				//throw new CDIException();
			}
		} catch (MIException e) {
			// throw new CDIException(e);
		}
		for (int i = 0; i < breakpoints.length; i++) {
			breakList.remove(breakpoints[i]);
		}
	}

	public void enableBreakpoint(ICBreakpoint breakpoint) throws CDIException {
		int number = 0;
		if (breakpoint instanceof Breakpoint && breakList.contains(breakpoint)) {
			number = ((Breakpoint)breakpoint).getMIBreakPoint().getNumber();
		} else {
			//throw new CDIException();
		}
		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakEnable breakEnable = factory.createMIBreakEnable(new int[]{number});
		try {
			s.getMISession().postCommand(breakEnable);
			MIInfo info = breakEnable.getMIInfo();
			if (info == null) {
				//throw new CDIException();
			}
		} catch (MIException e) {
			// throw new CDIException(e);
		}
		((Breakpoint)breakpoint).getMIBreakPoint().setEnabled(true);
	}

	public void disableBreakpoint(ICBreakpoint breakpoint) throws CDIException {
		int number = 0;
		if (breakpoint instanceof Breakpoint && breakList.contains(breakpoint)) {
			number = ((Breakpoint)breakpoint).getMIBreakPoint().getNumber();
		} else {
			// throw new CDIException();
		}
		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakDisable breakDisable = factory.createMIBreakDisable(new int[]{number});
		try {
			s.getMISession().postCommand(breakDisable);
			MIInfo info = breakDisable.getMIInfo();
			if (info == null) {
				//throw new CDIException();
			}
		} catch (MIException e) {
			// throw new CDIException(e);
		}
		((Breakpoint)breakpoint).getMIBreakPoint().setEnabled(false);
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
	public ICCatchpoint setCatchpoint(int type, ICCatchEvent event, String expression,
		ICCondition condition) throws CDIException {
		// throw new CDIException();
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpointManager#setLocationBreakpoint(int, ICLocation, ICCondition, boolean, String)
	 */
	public ICLocationBreakpoint setLocationBreakpoint(int type, ICLocation location,
		ICCondition condition, String threadId) throws CDIException {

		boolean hardware = (type == ICBreakpoint.HARDWARE);
		boolean temporary = (type == ICBreakpoint.TEMPORARY);
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
	public ICWatchpoint setWatchpoint(int type, int watchType, String expression,
		ICCondition condition) throws CDIException {
		boolean access = (type == ICWatchpoint.WRITE);
		boolean read = (type == ICWatchpoint.READ);

		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakWatch breakWatch = factory.createMIBreakWatch(access, read, expression);
		MIBreakPoint[] points = null;
		try {
			s.getMISession().postCommand(breakWatch);
			MIBreakWatchInfo info = breakWatch.getMIBreakWatchInfo();
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
}
