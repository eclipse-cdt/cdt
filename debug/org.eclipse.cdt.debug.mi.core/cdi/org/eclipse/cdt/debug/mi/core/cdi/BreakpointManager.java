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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

	public static ICDIBreakpoint[] EMPTY_BREAKPOINTS = {};

	Map breakMap;
	Map deferredMap;
	boolean allowInterrupt;

	public BreakpointManager(Session session) {
		super(session, false);
		breakMap = Collections.synchronizedMap(new HashMap());
		deferredMap = Collections.synchronizedMap(new HashMap());
		allowInterrupt = true;
	}

	synchronized List getBreakpointsList(Target target) {
		List bList = (List)breakMap.get(target);
		if (bList == null) {
			bList = Collections.synchronizedList(new ArrayList());
			breakMap.put(target, bList);
		}
		return bList;
	}

	public MIBreakpoint[] getMIBreakpoints(MISession miSession) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MIBreakList breakpointList = factory.createMIBreakList();
		try {
			miSession.postCommand(breakpointList);
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

	public Watchpoint getWatchpoint(MISession miSession, int number) {
		return (Watchpoint)getBreakpoint(miSession, number);
	}
	public Breakpoint getBreakpoint(MISession miSession, int number) {
		Session session = (Session)getSession();
		Target target = session.getTarget(miSession);
		if (target != null) {
			return getBreakpoint(target, number);
		}
		return null;
	}
	public Breakpoint getBreakpoint(Target target, int number) {
		List bList = (List)breakMap.get(target);
		if (bList != null) {
			Breakpoint[] bkpts = (Breakpoint[]) bList.toArray(new Breakpoint[0]);
			for (int i = 0; i < bkpts.length; i++) {
				MIBreakpoint miBreak = bkpts[i].getMIBreakpoint();
				if (miBreak.getNumber() == number) {
					return bkpts[i];
				}
			}
		}
		return null;
	}

	boolean suspendInferior(Target target) throws CDIException {
		boolean shouldRestart = false;
		// Stop the program
		if (allowInterrupt) {
			// Disable events.
			if (target.isRunning()) {
				target.suspend();
				shouldRestart = true;
			}
		}
		return shouldRestart;
	}

	void resumeInferior(Target target, boolean shouldRestart) throws CDIException {
		if (shouldRestart) {
			target.resume();
		}
	}

	public void deleteBreakpoint(MISession miSession, int no) {
		Session session = (Session)getSession();
		Target target = session.getTarget(miSession);
		if (target != null) {
			deleteBreakpoint(target, no);
		}
	}
	public void deleteBreakpoint (Target target, int no) {
		List bList = (List)breakMap.get(target);
		if (bList != null) {
			Breakpoint[] points = (Breakpoint[]) bList.toArray(new Breakpoint[0]);
			for (int i = 0; i < points.length; i++) {
				if (points[i].getMIBreakpoint().getNumber() == no) {
					bList.remove(points[i]);
					break;
				}
			}
		}
	}

	public void enableBreakpoint(ICDIBreakpoint breakpoint) throws CDIException {
		Target target = (Target)breakpoint.getTarget();
		List bList = (List)breakMap.get(target);
		if (bList == null) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
		}
		int number = 0;
		if (bList.contains(breakpoint)) {
			number = ((Breakpoint) breakpoint).getMIBreakpoint().getNumber();
		} else {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
		}
		boolean state = suspendInferior(target);
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIBreakEnable breakEnable = factory.createMIBreakEnable(new int[] { number });
		try {
			miSession.postCommand(breakEnable);
			MIInfo info = breakEnable.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			// Resume the program and enable events.
			resumeInferior(target, state);
		}
		((Breakpoint) breakpoint).getMIBreakpoint().setEnabled(true);
		// Fire a changed Event.
		miSession.fireEvent(new MIBreakpointChangedEvent(miSession, ((Breakpoint)breakpoint).getMIBreakpoint().getNumber()));
	}

	public void disableBreakpoint(ICDIBreakpoint breakpoint) throws CDIException {
		Target target = (Target)breakpoint.getTarget();
		List bList = (List)breakMap.get(target);
		if (bList == null) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
		}
		int number = 0;
		if (bList.contains(breakpoint)) {
			number = ((Breakpoint) breakpoint).getMIBreakpoint().getNumber();
		} else {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
		}

		Session session = (Session)target.getSession();
		boolean state = suspendInferior(target);
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIBreakDisable breakDisable =
			factory.createMIBreakDisable(new int[] { number });
		try {
			miSession.postCommand(breakDisable);
			MIInfo info = breakDisable.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			resumeInferior(target, state);
		}
		((Breakpoint) breakpoint).getMIBreakpoint().setEnabled(false);
		// Fire a changed Event.
		miSession.fireEvent(new MIBreakpointChangedEvent(miSession, ((Breakpoint)breakpoint).getMIBreakpoint().getNumber()));
	}

	public void setCondition(ICDIBreakpoint breakpoint, ICDICondition condition) throws CDIException {
		Target target = (Target)breakpoint.getTarget();
		List bList = (List)breakMap.get(target);
		if (bList == null) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
		}
		int number = 0;
		if (bList.contains(breakpoint)) {
			number = ((Breakpoint) breakpoint).getMIBreakpoint().getNumber();
		} else {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
		}

		Session session = (Session)target.getSession();
		boolean state = suspendInferior(target);
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();

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
			MIBreakCondition breakCondition = factory.createMIBreakCondition(number, exprCond);
			miSession.postCommand(breakCondition);
			MIInfo info = breakCondition.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			MIBreakAfter breakAfter =
				factory.createMIBreakAfter(number, ignoreCount);
			miSession.postCommand(breakAfter);
			info = breakAfter.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			resumeInferior(target, state);
		}
		// Fire a changed Event.
		miSession.fireEvent(new MIBreakpointChangedEvent(miSession, ((Breakpoint)breakpoint).getMIBreakpoint().getNumber()));
	}

	/**
	 * @deprecated
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#update()
	 */
	public void update() throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		update(target);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#update()
	 */
	public void update(Target target) throws CDIException {
		MISession miSession = target.getMISession();
		MIBreakpoint[] newMIBreakpoints = getMIBreakpoints(miSession);
		List bList = getBreakpointsList(target);
		List eventList = new ArrayList(newMIBreakpoints.length);
		for (int i = 0; i < newMIBreakpoints.length; i++) {
			int no = newMIBreakpoints[i].getNumber();
			Breakpoint bp = getBreakpoint(target, no);
			if (bp != null) {
				if (hasBreakpointChanged(bp, newMIBreakpoints[i])) {
					// Fire ChangedEvent
					bp.setMIBreakpoint(newMIBreakpoints[i]);
					eventList.add(new MIBreakpointChangedEvent(miSession, no)); 
				}
			} else {
				// add the new breakpoint and fire CreatedEvent
				if (newMIBreakpoints[i].isWatchpoint()) {
					bList.add(new Watchpoint(target, newMIBreakpoints[i]));
				} else {
					bList.add(new Breakpoint(target, newMIBreakpoints[i]));
				}
				eventList.add(new MIBreakpointCreatedEvent(miSession, no)); 
			}
		}
		// Check if any breakpoint was removed.
		Breakpoint[] oldBreakpoints = (Breakpoint[]) bList.toArray(new Breakpoint[0]);
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
				eventList.add(new MIBreakpointDeletedEvent(miSession, no)); 
			}
		}
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		miSession.fireEvents(events);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#allowProgramInterruption()
	 */
	public void allowProgramInterruption(boolean e) {
		allowInterrupt = e;
	}

	public void deleteFromDeferredList(Breakpoint bkpt) {
		List dList = (List)deferredMap.get(bkpt.getTarget());
		if (dList != null) {
			dList.remove(bkpt);
		}
	}

	public void addToBreakpointList(Breakpoint bkpt) {
		List bList = (List)breakMap.get(bkpt.getTarget());
		if (bList != null) {
			bList.add(bkpt);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#deleteAllBreakpoints()
	 */
	public void deleteAllBreakpoints() throws CDIException {
		deleteBreakpoints(getBreakpoints());
	}

	public void deleteAllBreakpoints(Target target) throws CDIException {
		List bList = (List)breakMap.get(target);
		if (bList != null) {
			ICDIBreakpoint[] bps = new ICDIBreakpoint[bList.size()];
			bList.toArray(bps);
			deleteBreakpoints(target, bps);
		}
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#deleteBreakpoint(ICDIBreakpoint)
	 */
	public void deleteBreakpoint(ICDIBreakpoint breakpoint) throws CDIException {
		deleteBreakpoints((Target)breakpoint.getTarget(), new ICDIBreakpoint[] { breakpoint });
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#deleteBreakpoints(ICDIBreakpoint[])
	 */
	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
		for (int i = 0; i < breakpoints.length; ++i) {
			deleteBreakpoint(breakpoints[i]);
		}
	}
	
	public void deleteBreakpoints(Target target, ICDIBreakpoint[] breakpoints) throws CDIException {
		int[] numbers = new int[breakpoints.length];
		List bList = (List)breakMap.get(target);
		if (bList == null) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
		}
		for (int i = 0; i < numbers.length; i++) {
			if (breakpoints[i] instanceof Breakpoint
				&& bList.contains(breakpoints[i])) {
				numbers[i] =
					((Breakpoint) breakpoints[i]).getMIBreakpoint().getNumber();
			} else {
				throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
			}
		}
		boolean state = suspendInferior(target);
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIBreakDelete breakDelete = factory.createMIBreakDelete(numbers);
		try {
			miSession.postCommand(breakDelete);
			MIInfo info = breakDelete.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			resumeInferior(target, state);
		}
		List eventList = new ArrayList(breakpoints.length);
		for (int i = 0; i < breakpoints.length; i++) {
			int no = ((Breakpoint)breakpoints[i]).getMIBreakpoint().getNumber();
			eventList.add(new MIBreakpointDeletedEvent(miSession, no));
		}
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		miSession.fireEvents(events);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#getBreakpoints(ICDITarget)
	 */
	public ICDIBreakpoint[] getBreakpoints(Target target) throws CDIException {
		List list = (List)breakMap.get(target);
		if (list != null) {
			ICDIBreakpoint[] bps = new ICDIBreakpoint[list.size()];
			list.toArray(bps);
			return bps;
		}
		return EMPTY_BREAKPOINTS;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#getBreakpoints()
	 */
	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		Collection col = breakMap.values();
		Iterator itr = breakMap.values().iterator();
		ICDIBreakpoint[] bps = new ICDIBreakpoint[col.size()];
		col.toArray(bps);
		return bps;
	}

	public ICDIBreakpoint[] getDeferredBreakpoints(Target target) throws CDIException {
		List dlist = (List)deferredMap.get(target);
		if (dlist != null) {
			ICDIBreakpoint[] bps = new ICDIBreakpoint[dlist.size()];
			dlist.toArray(bps);
			return bps;
		}
		return EMPTY_BREAKPOINTS;
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

		Session session = (Session)getSession();
		Target target = (Target)session.getCurrentTarget();
		return setLocationBreakpoint(target, type, location, condition, threadId, deferred);
	}

	public ICDILocationBreakpoint setLocationBreakpoint(Target target, int type, ICDILocation location,
			ICDICondition condition, String threadId, boolean deferred) throws CDIException {

		MISession miSession = target.getMISession();
		Breakpoint bkpt = new Breakpoint(target, type, location, condition, threadId);
		try {
			setLocationBreakpoint(bkpt);
			List blist = getBreakpointsList(target);
			blist.add(bkpt);

			// Fire a created Event.
			miSession.fireEvent(new MIBreakpointCreatedEvent(miSession, bkpt.getMIBreakpoint().getNumber()));
		} catch (CDIException e) {
			if (!deferred) {
				throw e;
			}
			Session session = (Session)target.getSession();
			ICDISharedLibraryManager sharedMgr  = session.getSharedLibraryManager();
			if (sharedMgr instanceof SharedLibraryManager) {
				SharedLibraryManager mgr = (SharedLibraryManager)sharedMgr;
				if (mgr.isDeferredBreakpoint()) {
					List dList = (List)deferredMap.get(target);
					if (dList == null) {
						dList = Collections.synchronizedList(new ArrayList());
						deferredMap.put(target, dList);
					}
					dList.add(bkpt);
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
		String threadId = bkpt.getThreadId();
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

		int tid = 0;
		if (threadId != null && threadId.length() > 0) {
			try {
				tid = Integer.parseInt(threadId);
			} catch (NumberFormatException e) {
			}
		}
		MISession miSession = ((Target)bkpt.getTarget()).getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		return factory.createMIBreakInsert(temporary, hardware, exprCond, ignoreCount, line.toString(), tid);
	}

	void setLocationBreakpoint (Breakpoint bkpt) throws CDIException {
		Target target = (Target)bkpt.getTarget();
		MISession miSession = target.getMISession();
		boolean state = suspendInferior(target);
		MIBreakInsert breakInsert = createMIBreakInsert(bkpt);
		MIBreakpoint[] points = null;
		try {
			miSession.postCommand(breakInsert);
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
			resumeInferior(target, state);
		}

		bkpt.setMIBreakpoint(points[0]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager#setWatchpoint(int, int, String, ICDICondition, boolean)
	 */
	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression,
		ICDICondition condition) throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		return setWatchpoint(target, type, watchType, expression, condition);
	}
	public ICDIWatchpoint setWatchpoint(Target target, int type, int watchType, String expression,
			ICDICondition condition) throws CDIException {

		boolean access = ( (watchType & ICDIWatchpoint.WRITE) == ICDIWatchpoint.WRITE && 
						   (watchType & ICDIWatchpoint.READ) == ICDIWatchpoint.READ );
		boolean read = ( !((watchType & ICDIWatchpoint.WRITE) == ICDIWatchpoint.WRITE) && 
						  (watchType & ICDIWatchpoint.READ) == ICDIWatchpoint.READ );

		MISession miSession = target.getMISession();
		boolean state = suspendInferior(target);
		CommandFactory factory = miSession.getCommandFactory();
		MIBreakWatch breakWatch =
			factory.createMIBreakWatch(access, read, expression);
		MIBreakpoint[] points = null;
		try {
			miSession.postCommand(breakWatch);
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
			resumeInferior(target, state);
		}
		Watchpoint bkpt = new Watchpoint(target, points[0]);
		List bList = getBreakpointsList(target);
		bList.add(bkpt);

		// Fire a created Event.
		miSession.fireEvent(new MIBreakpointCreatedEvent(miSession, bkpt.getMIBreakpoint().getNumber()));
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
