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
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDICatchEvent;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDICatchpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Breakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.cdi.model.Watchpoint;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIBreakAfter;
import org.eclipse.cdt.debug.mi.core.command.MIBreakCondition;
import org.eclipse.cdt.debug.mi.core.command.MIBreakDelete;
import org.eclipse.cdt.debug.mi.core.command.MIBreakDisable;
import org.eclipse.cdt.debug.mi.core.command.MIBreakEnable;
import org.eclipse.cdt.debug.mi.core.command.MIBreakInsert;
import org.eclipse.cdt.debug.mi.core.command.MIBreakList;
import org.eclipse.cdt.debug.mi.core.command.MIBreakWatch;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointDeletedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.output.MIBreakInsertInfo;
import org.eclipse.cdt.debug.mi.core.output.MIBreakListInfo;
import org.eclipse.cdt.debug.mi.core.output.MIBreakWatchInfo;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;

/**
 * Breakpoint Manager for the CDI interface.
 */
public class BreakpointManager extends Manager implements ICDIBreakpointManager {

	List breakList;
	List deferredList;
	boolean allowInterrupt;

	public BreakpointManager(Session session) {
		super(session, false);
		breakList = Collections.synchronizedList(new ArrayList());
		deferredList = Collections.synchronizedList(new ArrayList());
		allowInterrupt = true;
	}

	public MIBreakpoint[] getMIBreakpoints() throws CDIException {
		Session s = (Session)getSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIBreakList breakpointList = factory.createMIBreakList();
		try {
			s.getMISession().postCommand(breakpointList);
			MIBreakListInfo info = breakpointList.getMIBreakListInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			return info.getMIBreakpoints();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	boolean hasBreakpointChanged(Breakpoint point, MIBreakpoint miBreakpoint) {
		MIBreakpoint miBreak = point.getMIBreakpoint();
		return miBreak.isEnabled() != miBreakpoint.isEnabled() ||
			!miBreak.getCondition().equals(miBreakpoint.getCondition()) ||
			miBreak.getIgnoreCount() != miBreakpoint.getIgnoreCount();
	}

	public Breakpoint getBreakpoint(int number) {
		Breakpoint[] bkpts = (Breakpoint[]) breakList.toArray(new Breakpoint[0]);
		for (int i = 0; i < bkpts.length; i++) {
			MIBreakpoint miBreak = bkpts[i].getMIBreakpoint();
			if (miBreak.getNumber() == number) {
				return bkpts[i];
			}
		}
		return null;
	}

	public Watchpoint getWatchpoint(int number) {
		return (Watchpoint)getBreakpoint(number);
	}

	boolean suspendInferior(ICDITarget target) throws CDIException {
		boolean shouldRestart = false;
		// Stop the program
		if (allowInterrupt) {
			if (target instanceof Target) {
				Target ctarget = (Target)target;
				// Disable events.
				if (ctarget.isRunning()) {
					ctarget.suspend();
					shouldRestart = true;
				}
			} else if (!target.isSuspended()) {
				target.suspend();
				shouldRestart = true;
			}
		}
		return shouldRestart;
	}

	void resumeInferior(ICDITarget target, boolean shouldRestart) throws CDIException {
		if (shouldRestart) {
			target.resume();
		}
	}

	public void deleteBreakpoint (int no) {
		Breakpoint[] points = (Breakpoint[]) breakList.toArray(new Breakpoint[0]);
		for (int i = 0; i < points.length; i++) {
			if (points[i].getMIBreakpoint().getNumber() == no) {
				breakList.remove(points[i]);
				break;
			}
		}
	}

	public void enableBreakpoint(ICDIBreakpoint breakpoint) throws CDIException {
		int number = 0;
		if (breakpoint instanceof Breakpoint
			&& breakList.contains(breakpoint)) {
			number = ((Breakpoint) breakpoint).getMIBreakpoint().getNumber();
		} else {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
		}
		boolean state = suspendInferior(breakpoint.getTarget());
		Session session = (Session)getSession();
		CommandFactory factory = session.getMISession().getCommandFactory();
		MIBreakEnable breakEnable = factory.createMIBreakEnable(new int[] { number });
		try {
			session.getMISession().postCommand(breakEnable);
			MIInfo info = breakEnable.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			// Resume the program and enable events.
			resumeInferior(breakpoint.getTarget(), state);
		}
		((Breakpoint) breakpoint).getMIBreakpoint().setEnabled(true);
		// Fire a changed Event.
		MISession mi = session.getMISession();
		mi.fireEvent(new MIBreakpointChangedEvent(((Breakpoint)breakpoint).getMIBreakpoint().getNumber()));
	}

	public void disableBreakpoint(ICDIBreakpoint breakpoint) throws CDIException {
		int number = 0;
		if (breakpoint instanceof Breakpoint
			&& breakList.contains(breakpoint)) {
			number = ((Breakpoint) breakpoint).getMIBreakpoint().getNumber();
		} else {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
		}

		Session session = (Session)getSession();
		boolean state = suspendInferior(breakpoint.getTarget());
		CommandFactory factory = session.getMISession().getCommandFactory();
		MIBreakDisable breakDisable =
			factory.createMIBreakDisable(new int[] { number });
		try {
			session.getMISession().postCommand(breakDisable);
			MIInfo info = breakDisable.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			resumeInferior(breakpoint.getTarget(), state);
		}
		((Breakpoint) breakpoint).getMIBreakpoint().setEnabled(false);
		// Fire a changed Event.
		MISession mi = session.getMISession();
		mi.fireEvent(new MIBreakpointChangedEvent(((Breakpoint)breakpoint).getMIBreakpoint().getNumber()));
	}

	public void setCondition(ICDIBreakpoint breakpoint, ICDICondition condition) throws CDIException {
		int number = 0;
		if (breakpoint instanceof Breakpoint
			&& breakList.contains(breakpoint)) {
			number = ((Breakpoint) breakpoint).getMIBreakpoint().getNumber();
		} else {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
		}

		Session session = (Session)getSession();
		boolean state = suspendInferior(breakpoint.getTarget());
		CommandFactory factory = session.getMISession().getCommandFactory();

		// reset the values to sane states.
		String exprCond = condition.getExpression();
		if (exprCond == null) {
			exprCond = ""; //$NON-NLS-1$
		}
		int ignoreCount = condition.getIgnoreCount();
		if (ignoreCount < 0) { 
			ignoreCount = 0;
		}

		try {
			MIBreakCondition breakCondition =
				factory.createMIBreakCondition(number, exprCond);
			session.getMISession().postCommand(breakCondition);
			MIInfo info = breakCondition.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			MIBreakAfter breakAfter =
				factory.createMIBreakAfter(number, ignoreCount);
			session.getMISession().postCommand(breakAfter);
			info = breakAfter.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			resumeInferior(breakpoint.getTarget(), state);
		}
		// Fire a changed Event.
		MISession mi = session.getMISession();
		mi.fireEvent(new MIBreakpointChangedEvent(((Breakpoint)breakpoint).getMIBreakpoint().getNumber()));
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#update()
	 */
	public void update() throws CDIException {
		MIBreakpoint[] newMIBreakpoints = getMIBreakpoints();
		List eventList = new ArrayList(newMIBreakpoints.length);
		for (int i = 0; i < newMIBreakpoints.length; i++) {
			int no = newMIBreakpoints[i].getNumber();
			Breakpoint bp = getBreakpoint(no);
			if (bp != null) {
				if (hasBreakpointChanged(bp, newMIBreakpoints[i])) {
					// Fire ChangedEvent
					bp.setMIBreakpoint(newMIBreakpoints[i]);
					eventList.add(new MIBreakpointChangedEvent(no)); 
				}
			} else {
				// add the new breakpoint and fire CreatedEvent
				if (newMIBreakpoints[i].isWatchpoint()) {
					breakList.add(new Watchpoint(this, newMIBreakpoints[i]));
				} else {
					breakList.add(new Breakpoint(this, newMIBreakpoints[i]));
				}
				eventList.add(new MIBreakpointCreatedEvent(no)); 
			}
		}
		// Check if any breakpoint was removed.
		Breakpoint[] oldBreakpoints = (Breakpoint[]) breakList.toArray(new Breakpoint[0]);
		for (int i = 0; i < oldBreakpoints.length; i++) {
			boolean found = false;
			int no = oldBreakpoints[i].getMIBreakpoint().getNumber();
			for (int j = 0; j < newMIBreakpoints.length; j++) {
				if (no == newMIBreakpoints[j].getNumber()) {
					found = true;
					break;
				}
			}
			if (!found) {
				// Fire destroyed Events.
				eventList.add(new MIBreakpointDeletedEvent(no)); 
			}
		}
		MISession mi = ((Session)getSession()).getMISession();
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#allowProgramInterruption()
	 */
	public void allowProgramInterruption(boolean e) {
		allowInterrupt = e;
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
		deleteBreakpoints(new ICDIBreakpoint[] { breakpoint });
	}

	public void deleteFromDeferredList(Breakpoint bkpt) {
		deferredList.remove(bkpt);
	}

	public void addToBreakpointList(Breakpoint bkpt) {
		breakList.add(bkpt);
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
					((Breakpoint) breakpoints[i]).getMIBreakpoint().getNumber();
			} else {
				throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
			}
		}
		Session session = (Session)getSession();
		boolean state = suspendInferior(session.getCurrentTarget());
		CommandFactory factory = session.getMISession().getCommandFactory();
		MIBreakDelete breakDelete = factory.createMIBreakDelete(numbers);
		try {
			session.getMISession().postCommand(breakDelete);
			MIInfo info = breakDelete.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			resumeInferior(session.getCurrentTarget(), state);
		}
		List eventList = new ArrayList(breakpoints.length);
		for (int i = 0; i < breakpoints.length; i++) {
			int no = ((Breakpoint)breakpoints[i]).getMIBreakpoint().getNumber();
			eventList.add(new MIBreakpointDeletedEvent(no));
		}
		MISession mi = session.getMISession();
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#getBreakpoints()
	 */
	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		return (ICDIBreakpoint[]) breakList.toArray(new ICDIBreakpoint[0]);
	}

	public ICDIBreakpoint[] getDeferredBreakpoints() throws CDIException {
		return (ICDIBreakpoint[]) deferredList.toArray(new ICDIBreakpoint[0]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#setCatchpoint(int, ICDICatchEvent, String, ICDICondition, boolean)
	 */
	public ICDICatchpoint setCatchpoint( int type, ICDICatchEvent event, String expression,
		ICDICondition condition) throws CDIException {
		throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_Supported")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#setLocationBreakpoint(int, ICDILocation, ICDICondition, boolean, String)
	 */
	public ICDILocationBreakpoint setLocationBreakpoint(int type, ICDILocation location,
		ICDICondition condition, String threadId) throws CDIException {
		return setLocationBreakpoint(type, location, condition, threadId, false);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#setLocationBreakpoint(int, ICDILocation, ICDICondition, boolean, String)
	 */
	public ICDILocationBreakpoint setLocationBreakpoint(int type, ICDILocation location,
		ICDICondition condition, String threadId, boolean deferred) throws CDIException {

		Breakpoint bkpt = new Breakpoint(this, type, location, condition, threadId);
		try {
			setLocationBreakpoint(bkpt);
			breakList.add(bkpt);

			// Fire a created Event.
			Session session = (Session)getSession();
			MISession mi = session.getMISession();
			mi.fireEvent(new MIBreakpointCreatedEvent(bkpt.getMIBreakpoint().getNumber()));
		} catch (CDIException e) {
			if (!deferred) {
				throw e;
			}
			Session session = (Session)getSession();
			ICDISharedLibraryManager sharedMgr  = session.getSharedLibraryManager();
			if (sharedMgr instanceof SharedLibraryManager) {
				SharedLibraryManager mgr = (SharedLibraryManager)sharedMgr;
				if (mgr.isDeferredBreakpoint()) {
					deferredList.add(bkpt);
				} else {
					throw e;
				}
			}
		}
		return bkpt;
	}

	MIBreakInsert createMIBreakInsert(Breakpoint bkpt) throws CDIException {
		boolean hardware = bkpt.isHardware();
		boolean temporary = bkpt.isTemporary();
		String exprCond = null;
		int ignoreCount = 0;
		StringBuffer line = new StringBuffer();

		if (bkpt.getCondition() != null) {
			ICDICondition condition = bkpt.getCondition();
			exprCond = condition.getExpression();
			ignoreCount = condition.getIgnoreCount();
		}

		if (bkpt.getLocation() != null) {
			ICDILocation location = bkpt.getLocation();
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
			} else if (location.getLineNumber() != 0) {
				line.append(location.getLineNumber());
			} else {
				line.append('*').append(location.getAddress());
			}
		}
		Session session = (Session)getSession();
		CommandFactory factory = session.getMISession().getCommandFactory();
		return factory.createMIBreakInsert(temporary, hardware, exprCond, ignoreCount, line.toString());
	}

	public void setLocationBreakpoint (Breakpoint bkpt) throws CDIException {
		Session session = (Session)getSession();
		boolean state = suspendInferior(session.getCurrentTarget());
		MIBreakInsert breakInsert = createMIBreakInsert(bkpt);
		MIBreakpoint[] points = null;
		try {
			session.getMISession().postCommand(breakInsert);
			MIBreakInsertInfo info = breakInsert.getMIBreakInsertInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			points = info.getMIBreakpoints();
			if (points == null || points.length == 0) {
				throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Parsing_Error")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			resumeInferior(session.getCurrentTarget(), state);
		}

		bkpt.setMIBreakpoint(points[0]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#setWatchpoint(int, int, String, ICDICondition, boolean)
	 */
	public ICDIWatchpoint setWatchpoint( int type, int watchType, String expression,
		ICDICondition condition) throws CDIException {
		boolean access = ( (watchType & ICDIWatchpoint.WRITE) == ICDIWatchpoint.WRITE && 
						   (watchType & ICDIWatchpoint.READ) == ICDIWatchpoint.READ );
		boolean read = ( !((watchType & ICDIWatchpoint.WRITE) == ICDIWatchpoint.WRITE) && 
						  (watchType & ICDIWatchpoint.READ) == ICDIWatchpoint.READ );

		Session session = (Session)getSession();
		boolean state = suspendInferior(session.getCurrentTarget());
		CommandFactory factory = session.getMISession().getCommandFactory();
		MIBreakWatch breakWatch =
			factory.createMIBreakWatch(access, read, expression);
		MIBreakpoint[] points = null;
		try {
			session.getMISession().postCommand(breakWatch);
			MIBreakWatchInfo info = breakWatch.getMIBreakWatchInfo();
			points = info.getMIBreakpoints();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			if (points == null || points.length == 0) {
				throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Parsing_Error")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			resumeInferior(session.getCurrentTarget(), state);
		}
		Watchpoint bkpt = new Watchpoint(this, points[0]);
		breakList.add(bkpt);

		// Fire a created Event.
		MISession mi = session.getMISession();
		mi.fireEvent(new MIBreakpointCreatedEvent(bkpt.getMIBreakpoint().getNumber()));
		return bkpt;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#createCondition(int, String)
	 */
	public ICDICondition createCondition(int ignoreCount, String expression) {
		return new Condition(ignoreCount, expression);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#createLocation(String, String, int)
	 */
	public ICDILocation createLocation(String file, String function, int line) {
		return new Location(file, function, line);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#createLocation(long)
	 */
	public ICDILocation createLocation(long address) {
		return new Location(address);
	}

}
