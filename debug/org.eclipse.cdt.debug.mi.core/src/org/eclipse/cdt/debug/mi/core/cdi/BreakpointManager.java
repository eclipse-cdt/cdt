/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDICatchEvent;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDICatchpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIBreakAfter;
import org.eclipse.cdt.debug.mi.core.command.MIBreakCondition;
import org.eclipse.cdt.debug.mi.core.command.MIBreakDelete;
import org.eclipse.cdt.debug.mi.core.command.MIBreakDisable;
import org.eclipse.cdt.debug.mi.core.command.MIBreakEnable;
import org.eclipse.cdt.debug.mi.core.command.MIBreakInsert;
import org.eclipse.cdt.debug.mi.core.command.MIBreakList;
import org.eclipse.cdt.debug.mi.core.command.MIBreakWatch;
import org.eclipse.cdt.debug.mi.core.output.MIBreakInsertInfo;
import org.eclipse.cdt.debug.mi.core.output.MIBreakListInfo;
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

	boolean containsBreakpoint(int number) {
		return (getBreakpoint(number) != null);
	}

	Breakpoint getBreakpoint(int number) {
		ICDIBreakpoint[] bkpts = listBreakpoints();
		for (int i = 0; i < bkpts.length; i++) {
			if (bkpts[i] instanceof Breakpoint) {
				Breakpoint point = (Breakpoint) bkpts[i];
				MIBreakPoint miBreak = point.getMIBreakPoint();
				if (miBreak.getNumber() == number) {
					return point;
				}
			}
		}
		return null;
	}

	Watchpoint getWatchpoint(int number) {
		return (Watchpoint)getBreakpoint(number);
	}

	Breakpoint[] listBreakpoints() {
		return (Breakpoint[]) breakList.toArray(
			new Breakpoint[breakList.size()]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#deleteAllBreakpoints()
	 */
	public void deleteAllBreakpoints() throws CDIException {
		deleteBreakpoints(listBreakpoints());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#deleteBreakpoint(ICDIBreakpoint)
	 */
	public void deleteBreakpoint(ICDIBreakpoint breakpoint)
		throws CDIException {
		deleteBreakpoints(new ICDIBreakpoint[] { breakpoint });
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#deleteBreakpoints(ICDIBreakpoint[])
	 */
	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
		int[] numbers = new int[breakpoints.length];
		for (int i = 0; i < numbers.length; i++) {
			if (breakpoints[i] instanceof Breakpoint
				&& breakList.contains(breakpoints[i])) {
				numbers[i] =
					((Breakpoint) breakpoints[i]).getMIBreakPoint().getNumber();
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
				throw new CDIException("No answer");
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
		if (breakpoint instanceof Breakpoint
			&& breakList.contains(breakpoint)) {
			number = ((Breakpoint) breakpoint).getMIBreakPoint().getNumber();
		} else {
			throw new CDIException("Not a CDT breakpoint");
		}
		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakEnable breakEnable =
			factory.createMIBreakEnable(new int[] { number });
		try {
			s.getMISession().postCommand(breakEnable);
			MIInfo info = breakEnable.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
		((Breakpoint) breakpoint).getMIBreakPoint().setEnabled(true);
	}

	public void disableBreakpoint(ICDIBreakpoint breakpoint) throws CDIException {
		int number = 0;
		if (breakpoint instanceof Breakpoint
			&& breakList.contains(breakpoint)) {
			number = ((Breakpoint) breakpoint).getMIBreakPoint().getNumber();
		} else {
			throw new CDIException("Not a CDT breakpoint");
		}
		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakDisable breakDisable =
			factory.createMIBreakDisable(new int[] { number });
		try {
			s.getMISession().postCommand(breakDisable);
			MIInfo info = breakDisable.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
		((Breakpoint) breakpoint).getMIBreakPoint().setEnabled(false);
	}

	public void setCondition(ICDIBreakpoint breakpoint, ICDICondition condition) throws CDIException {
		int number = 0;
		if (breakpoint instanceof Breakpoint
			&& breakList.contains(breakpoint)) {
			number = ((Breakpoint) breakpoint).getMIBreakPoint().getNumber();
		} else {
			throw new CDIException("Not a CDT breakpoint");
		}

		// We only suppor expression not ignore count reset.
		String exprCond = condition.getExpression();
		if (exprCond != null) {
			CSession s = getCSession();
			CommandFactory factory = s.getMISession().getCommandFactory();
			MIBreakCondition breakCondition =
				factory.createMIBreakCondition(number, exprCond);
			try {
				s.getMISession().postCommand(breakCondition);
				MIInfo info = breakCondition.getMIInfo();
				if (info == null) {
					throw new CDIException("No answer");
				}
			} catch (MIException e) {
				throw new CDIException(e.toString());
			}
		} else {
			int ignoreCount = condition.getIgnoreCount();
			CSession s = getCSession();
			CommandFactory factory = s.getMISession().getCommandFactory();
			MIBreakAfter breakAfter =
				factory.createMIBreakAfter(number, ignoreCount);
			try {
				s.getMISession().postCommand(breakAfter);
				MIInfo info = breakAfter.getMIInfo();
				if (info == null) {
					throw new CDIException("No answer");
				}
			} catch (MIException e) {
				throw new CDIException(e.toString());
			}
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#getBreakpoints()
	 */
	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakList breakpointList = factory.createMIBreakList();
		try {
			s.getMISession().postCommand(breakpointList);
			MIBreakListInfo info = breakpointList.getMIBreakListInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			MIBreakPoint[] miPoints = info.getBreakPoints();
			for (int i = 0; i < miPoints.length; i++) {
				if (!containsBreakpoint(miPoints[i].getNumber())) {
					// FIXME: Generate a Create/Change Event??
					breakList.add(new Breakpoint(this, miPoints[i]));
				}
			}
			// FIXME: Generate a DestroyEvent for deleted ones.
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
		return (ICDIBreakpoint[]) listBreakpoints();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#setCatchpoint(int, ICDICatchEvent, String, ICDICondition, boolean)
	 */
	public ICDICatchpoint setCatchpoint( int type, ICDICatchEvent event, String expression,
		ICDICondition condition) throws CDIException {
		throw new CDIException("Not Supported");
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#setLocationBreakpoint(int, ICDILocation, ICDICondition, boolean, String)
	 */
	public ICDILocationBreakpoint setLocationBreakpoint( int type, ICDILocation location,
		ICDICondition condition, String threadId) throws CDIException {

		boolean hardware = (type == ICDIBreakpoint.HARDWARE);
		boolean temporary = (type == ICDIBreakpoint.TEMPORARY);
		String exprCond = null;
		int ignoreCount = 0;
		StringBuffer line = new StringBuffer();
		if (condition != null) {
			exprCond = condition.getExpression();
			ignoreCount = condition.getIgnoreCount();
		}

		if (location != null) {
			String file = location.getFile();
			String function = location.getFunction();
			if (file != null && file.length() > 0) {
				line.append(file).append(':');
				if (function != null && function.length() > 0) {
					line.append(function);
				} else {
					line.append(location.getLineNumber());
				}
			} else if (function != null && function.length() > 0) {
				line.append(function);
			} else {
				line.append('*').append(location.getAddress());
			}
		}

		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakInsert breakInsert =
			factory.createMIBreakInsert( temporary, hardware, exprCond,
				ignoreCount, line.toString());
		MIBreakPoint[] points = null;
		try {
			s.getMISession().postCommand(breakInsert);
			MIBreakInsertInfo info = breakInsert.getMIBreakInsertInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			points = info.getBreakPoints();
			if (points == null || points.length == 0) {
				throw new CDIException("Error parsing");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}

		Breakpoint bkpt = new Breakpoint(this, points[0]);
		breakList.add(bkpt);
		return bkpt;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#setWatchpoint(int, int, String, ICDICondition, boolean)
	 */
	public ICDIWatchpoint setWatchpoint( int type, int watchType, String expression,
		ICDICondition condition) throws CDIException {
		boolean access = (type == ICDIWatchpoint.WRITE);
		boolean read = (type == ICDIWatchpoint.READ);
		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakWatch breakWatch =
			factory.createMIBreakWatch(access, read, expression);
		MIBreakPoint[] points = null;
		try {
			s.getMISession().postCommand(breakWatch);
			MIBreakWatchInfo info = breakWatch.getMIBreakWatchInfo();
			points = info.getBreakPoints();
			if (info == null) {
				throw new CDIException("No answer");
			}
			if (points == null || points.length == 0) {
				throw new CDIException("Parsing Error");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}

		Watchpoint bkpt = new Watchpoint(this, points[0]);
		breakList.add(bkpt);
		return bkpt;
	}
}
