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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExceptionpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Breakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.Exceptionpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.cdi.model.Watchpoint;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
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
public class BreakpointManager extends Manager {

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

	MIBreakpoint[] getAllMIBreakpoints(MISession miSession) throws CDIException {
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

	boolean hasBreakpointChanged(MIBreakpoint miBreak, MIBreakpoint miBreakpoint) {
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
				MIBreakpoint[] miBreakpoints = bkpts[i].getMIBreakpoints();
				for (int j = 0; j < miBreakpoints.length; j++) {
					if (miBreakpoints[j].getNumber() == number) {
						return bkpts[i];
					}
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

	/**
	 * Use in the event classes, the breakpoint is not remove from the list
	 * It is only done in DestroyedEvent class.  Since we need to keep the breakpoint
	 * type around.
	 * @param target
	 * @param no
	 */
	void deleteBreakpoint (Target target, int no) {
		List bList = (List)breakMap.get(target);
		if (bList != null) {
			Breakpoint[] points = (Breakpoint[]) bList.toArray(new Breakpoint[0]);
			for (int i = 0; i < points.length; i++) {
				MIBreakpoint[] miBreakpoints = points[i].getMIBreakpoints();
				for (int j = 0; j < miBreakpoints.length; j++) {
					if (miBreakpoints[j].getNumber() == no) {
						bList.remove(points[i]);
						break;
					}
				}
			}
		}
	}

	/**
	 * Call through the Breakpoint class Breakpoint.setEnabled(boolean)
	 * 
	 * @param breakpoint
	 * @throws CDIException
	 */
	public void enableBreakpoint(Breakpoint breakpoint) throws CDIException {
		Target target = (Target)breakpoint.getTarget();
		List bList = (List)breakMap.get(target);
		if (bList == null) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
		}
		if (!bList.contains(breakpoint)) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
		}
		MIBreakpoint[] miBreakpoints = breakpoint.getMIBreakpoints();
		if (miBreakpoints == null || miBreakpoints.length == 0) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
		}
		
		int[] numbers = new int[miBreakpoints.length];
		for (int i = 0; i < miBreakpoints.length; i++) {
			numbers[i] = miBreakpoints[i].getNumber();
		}
		boolean state = suspendInferior(target);
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIBreakEnable breakEnable = factory.createMIBreakEnable(numbers);
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
		for (int i = 0; i < miBreakpoints.length; i++) {
			miBreakpoints[i].setEnabled(true);
		}
		// Fire a changed Event.
		miSession.fireEvent(new MIBreakpointChangedEvent(miSession, numbers[0]));
	}

	/**
	 * Call through the Breakpoint class.  Breakpoint.disable
	 * 
	 * @param breakpoint
	 * @throws CDIException
	 */
	public void disableBreakpoint(Breakpoint breakpoint) throws CDIException {
		Target target = (Target)breakpoint.getTarget();
		List bList = (List)breakMap.get(target);
		if (bList == null) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
		}
		if (!bList.contains(breakpoint)) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
		}

		MIBreakpoint[] miBreakpoints = breakpoint.getMIBreakpoints();
		if (miBreakpoints == null || miBreakpoints.length == 0) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
		}
		int[] numbers = new int[miBreakpoints.length];
		for (int i = 0; i < miBreakpoints.length; i++) {
			numbers[i] = miBreakpoints[i].getNumber();
		}

		boolean state = suspendInferior(target);
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIBreakDisable breakDisable = factory.createMIBreakDisable(numbers);
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
		for (int i = 0; i < miBreakpoints.length; i++) {
			miBreakpoints[i].setEnabled(false);
		}
		// Fire a changed Event.
		miSession.fireEvent(new MIBreakpointChangedEvent(miSession, numbers[0]));
	}

	/**
	 * Use by the Breakpoint class, Breakpoint.setCondition(Condition cond)
	 * In this case we will not try to change the condition with -break-condition.
	 * Since condition may contains new thread-id it is simpler to remove the breakpoints
	 * and make a new breakpoints with the new conditions.
	 * @param breakpoint
	 * @param newCondition
	 * @throws CDIException
	 */
	public void setCondition(ICDIBreakpoint breakpoint, ICDICondition newCondition) throws CDIException {
		Target target = (Target)breakpoint.getTarget();
		List bList = (List)breakMap.get(target);
		if (bList == null) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
		}
		if (!bList.contains(breakpoint)) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
		}

		Breakpoint bpt = (Breakpoint)breakpoint;
		MIBreakpoint[] miBreakpoints = bpt.getMIBreakpoints();
		deleteMIBreakpoints(target, miBreakpoints);
		ICDICondition oldCondition = bpt.getCondition();
		boolean success = false;
		try {
			bpt.setCondition0(newCondition);
			setLocationBreakpoint(bpt);
			success = true;
		} finally {
			if (!success) {
				bpt.setCondition0(oldCondition);
				setLocationBreakpoint(bpt);
			}
		}
		
		// Fire a changed Event.
		miBreakpoints = bpt.getMIBreakpoints();
		if (miBreakpoints != null && miBreakpoints.length > 0) {
			MISession miSession = target.getMISession();
			miSession.fireEvent(new MIBreakpointChangedEvent(miSession, miBreakpoints[0].getNumber()));
		}
	}

	/**
	 */
	public void update(Target target) throws CDIException {
		MISession miSession = target.getMISession();
		MIBreakpoint[] allMIBreakpoints = getAllMIBreakpoints(miSession);
		List bList = getBreakpointsList(target);
		List eventList = new ArrayList(allMIBreakpoints.length);
		for (int i = 0; i < allMIBreakpoints.length; i++) {
			int no = allMIBreakpoints[i].getNumber();
			Breakpoint bp = getBreakpoint(target, no);
			if (bp != null) {
				MIBreakpoint[] miBps = bp.getMIBreakpoints();
				for (int j = 0; j < miBps.length; j++) {
					if (miBps[j].getNumber() == no) {
						if (hasBreakpointChanged(miBps[j], allMIBreakpoints[i])) {
							miBps[j] = allMIBreakpoints[i];
							bp.setEnabled0(allMIBreakpoints[i].isEnabled());
							// FIXME: We have a problem if the thread id change.
							ICDICondition oldCond = bp.getCondition();
							String[] tids = oldCond.getThreadIds();
							Condition newCondition = new Condition(allMIBreakpoints[i].getIgnoreCount(),
									allMIBreakpoints[i].getCondition(), tids);
							bp.setCondition0(newCondition);
							// Fire ChangedEvent
							eventList.add(new MIBreakpointChangedEvent(miSession, no)); 
						}
					}
				}
			} else {
				// add the new breakpoint and fire CreatedEvent
				int type = ICDIBreakpoint.REGULAR;
				if (allMIBreakpoints[i].isHardware()) {
					type = ICDIBreakpoint.HARDWARE;
				} else if (allMIBreakpoints[i].isTemporary()) {
					type = ICDIBreakpoint.TEMPORARY;
				}
				String[] tids = null;
				String tid = allMIBreakpoints[i].getThreadId();
				if (tid != null && tid.length() > 0) {
					tids = new String[] { tid };
				}
				Condition condition = new Condition(allMIBreakpoints[i].getIgnoreCount(),
						allMIBreakpoints[i].getCondition(), tids);
				
				if (allMIBreakpoints[i].isWatchpoint()) {
					int watchType = 0;
					if (allMIBreakpoints[i].isAccessWatchpoint() || allMIBreakpoints[i].isReadWatchpoint()) {
						watchType &= ICDIWatchpoint.READ;
					}
					if (allMIBreakpoints[i].isAccessWatchpoint() || allMIBreakpoints[i].isWriteWatchpoint()) {
						watchType &= ICDIWatchpoint.WRITE;
					}
					Watchpoint wpoint = new Watchpoint(target, allMIBreakpoints[i].getWhat(), type, watchType, condition);
					bList.add(wpoint);
				} else {
					Location location = new Location (allMIBreakpoints[i].getFile(),
							allMIBreakpoints[i].getFunction(),
							allMIBreakpoints[i].getLine(),
							MIFormat.getBigInteger(allMIBreakpoints[i].getAddress()));
					
					Breakpoint newBreakpoint = new Breakpoint(target, type, location, condition);
					newBreakpoint.setMIBreakpoints(new MIBreakpoint[] {allMIBreakpoints[i]});
					bList.add(newBreakpoint);
				}
				eventList.add(new MIBreakpointCreatedEvent(miSession, no)); 
			}
		}
		// Check if any breakpoint was removed.
		Breakpoint[] oldBreakpoints = (Breakpoint[]) bList.toArray(new Breakpoint[0]);
		for (int i = 0; i < oldBreakpoints.length; i++) {
			boolean found = false;
			MIBreakpoint[] miBreakpoints = oldBreakpoints[i].getMIBreakpoints();
			for (int j = 0; j < miBreakpoints.length; j++) {
				int no = miBreakpoints[j].getNumber();
				for (int k = 0; k < allMIBreakpoints.length; k++) {
					if (no == allMIBreakpoints[k].getNumber()) {
						found = true;
						break;
					}
				}
				if (!found) {
					// Fire destroyed Events.
					eventList.add(new MIBreakpointDeletedEvent(miSession, no)); 
				}
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

	/**
	 * Use by the EventManager when checking for deferred breapoints.
	 * @param bkpt
	 */
	public void addToBreakpointList(Breakpoint bkpt) {
		List bList = (List)breakMap.get(bkpt.getTarget());
		if (bList != null) {
			bList.add(bkpt);
		}
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

	public void deleteBreakpoints(Target target, ICDIBreakpoint[] breakpoints) throws CDIException {
		List bList = (List)breakMap.get(target);

		// Do the sanity check first, we will accept all or none
		if (bList == null) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
		}
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof Breakpoint && bList.contains(breakpoints[i]))) {
				throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
			}
		}

		MISession miSession = target.getMISession();
		List eventList = new ArrayList(breakpoints.length);
		for (int i = 0; i < breakpoints.length; i++) {
			MIBreakpoint[] miBreakpoints = ((Breakpoint)breakpoints[i]).getMIBreakpoints();
			if (miBreakpoints.length > 0) {
				deleteMIBreakpoints(target, miBreakpoints);
				eventList.add(new MIBreakpointDeletedEvent(miSession, miBreakpoints[0].getNumber()));
			}
		}
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		miSession.fireEvents(events);
	}

	void deleteMIBreakpoints(Target target, MIBreakpoint[] miBreakpoints) throws CDIException {
		MISession miSession = target.getMISession();
		int[] numbers = new int[miBreakpoints.length];
		for (int i = 0; i < miBreakpoints.length; ++i) {
			numbers[i] = miBreakpoints[i].getNumber();
		}
		boolean state = suspendInferior(target);
		try {
			deleteMIBreakpoints(miSession, numbers);
		} finally {
			resumeInferior(target, state);
		}
	}

	void deleteMIBreakpoints(MISession miSession, int[] numbers) throws CDIException {
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
		}
	}

	public ICDIBreakpoint[] getBreakpoints(Target target) throws CDIException {
		List list = (List)breakMap.get(target);
		if (list != null) {
			ICDIBreakpoint[] bps = new ICDIBreakpoint[list.size()];
			list.toArray(bps);
			return bps;
		}
		return EMPTY_BREAKPOINTS;
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

	public ICDILocationBreakpoint setLocationBreakpoint(Target target, int type, ICDILocation location,
				ICDICondition condition, boolean deferred) throws CDIException {
		MISession miSession = target.getMISession();
		Breakpoint bkpt = new Breakpoint(target, type, location, condition);
		try {
			setLocationBreakpoint(bkpt);
			List blist = getBreakpointsList(target);
			blist.add(bkpt);
			// Force the reset of the location.
			bkpt.setLocation(null);

			// Fire a created Event.
			MIBreakpoint[] miBreakpoints = bkpt.getMIBreakpoints();
			if (miBreakpoints != null && miBreakpoints.length > 0) {
				miSession.fireEvent(new MIBreakpointCreatedEvent(miSession, miBreakpoints[0].getNumber()));
			}
		} catch (CDIException e) {
			if (!deferred) {
				throw e;
			}
			Session session = (Session)target.getSession();
			SharedLibraryManager sharedMgr  = session.getSharedLibraryManager();
			if (sharedMgr.isDeferredBreakpoint()) {
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
		return bkpt;
	}

	MIBreakInsert[] createMIBreakInsert(Breakpoint bkpt) throws CDIException {
		boolean hardware = bkpt.isHardware();
		boolean temporary = bkpt.isTemporary();
		String exprCond = null;
		int ignoreCount = 0;
		String[] threadIds = null;
		StringBuffer line = new StringBuffer();

		if (bkpt.getCondition() != null) {
			ICDICondition condition = bkpt.getCondition();
			exprCond = condition.getExpression();
			ignoreCount = condition.getIgnoreCount();
			threadIds = condition.getThreadIds();
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

		MIBreakInsert[] miBreakInserts;
		MISession miSession = ((Target)bkpt.getTarget()).getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		if (threadIds == null || threadIds.length == 0) {
			MIBreakInsert bi = factory.createMIBreakInsert(temporary, hardware, exprCond, ignoreCount, line.toString(), 0);
			miBreakInserts = new MIBreakInsert[] { bi } ;
		} else {
			List list = new ArrayList(threadIds.length);
			for (int i = 0; i < threadIds.length; i++) {
				String threadId = threadIds[i];
				int tid = 0;
				if (threadId != null && threadId.length() > 0) {
					try {
						tid = Integer.parseInt(threadId);
						list.add(factory.createMIBreakInsert(temporary, hardware, exprCond, ignoreCount, line.toString(), tid));
					} catch (NumberFormatException e) {
					}
				}
			}
			miBreakInserts = (MIBreakInsert[]) list.toArray(new MIBreakInsert[list.size()]);
		}
		return miBreakInserts;
	}

	public void setLocationBreakpoint (Breakpoint bkpt) throws CDIException {
		Target target = (Target)bkpt.getTarget();
		MISession miSession = target.getMISession();
		boolean state = suspendInferior(target);
		MIBreakInsert[] breakInserts = createMIBreakInsert(bkpt);
		List pointList = new ArrayList();
		try {
			for (int i = 0; i < breakInserts.length; i++) {
				miSession.postCommand(breakInserts[i]);
				MIBreakInsertInfo info = breakInserts[i].getMIBreakInsertInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
				MIBreakpoint[] points = info.getMIBreakpoints();
				if (points == null || points.length == 0) {
					throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Parsing_Error")); //$NON-NLS-1$
				}
				pointList.addAll(Arrays.asList(points));
			}
		} catch (MIException e) {
			try {
				// Things did not go well remove all the breakpoints we've set before.
				MIBreakpoint[] allPoints = (MIBreakpoint[]) pointList.toArray(new MIBreakpoint[pointList.size()]);
				if (allPoints != null && allPoints.length > 0) {
					deleteMIBreakpoints(target, allPoints);
				}
			} catch (CDIException cdie) {
				// ignore this one;
			}
			throw new MI2CDIException(e);
		} finally {
			resumeInferior(target, state);
		}
		MIBreakpoint[] allPoints = (MIBreakpoint[]) pointList.toArray(new MIBreakpoint[pointList.size()]);
		bkpt.setMIBreakpoints(allPoints);
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
		Watchpoint bkpt = new Watchpoint(target, expression, type, watchType, condition);
		bkpt.setMIBreakpoints(points);
		List bList = getBreakpointsList(target);
		bList.add(bkpt);

		// Fire a created Event.
		MIBreakpoint[] miBreakpoints = bkpt.getMIBreakpoints();
		if (miBreakpoints != null && miBreakpoints.length > 0)
		miSession.fireEvent(new MIBreakpointCreatedEvent(miSession, miBreakpoints[0].getNumber()));
		return bkpt;
	}

	Breakpoint[] exceptionBps = new Breakpoint[2];
	final int EXCEPTION_THROW_IDX = 0;
	final int EXCEPTION_CATCH_IDX = 1;
	final static String[] EXCEPTION_FUNCS = new String[] {"__cxa_throw", "__cxa_begin_catch"}; //$NON-NLS-1$ //$NON-NLS-2$


	public ICDIExceptionpoint setExceptionpoint(Target target, String clazz, boolean stopOnThrow,
			boolean stopOnCatch) throws CDIException {

		if (!stopOnThrow && !stopOnCatch) {
			throw new CDIException("Must suspend on throw or catch"); //$NON-NLS-1$
		}

		MIBreakpoint[] miBreakpoints = null;

		if (stopOnThrow) {
			synchronized(exceptionBps) {
				int id = EXCEPTION_THROW_IDX;
				if (exceptionBps[EXCEPTION_THROW_IDX] == null) {
					Location location = new Location(null, EXCEPTION_FUNCS[id], 0);
					Breakpoint bp = new Breakpoint(target, ICDIBreakpoint.REGULAR, location, null);
					setLocationBreakpoint(bp);
					exceptionBps[id] = bp;
					miBreakpoints = bp.getMIBreakpoints();
				}
			}
		}
		if (stopOnCatch) {
			synchronized(exceptionBps) {
				int id = EXCEPTION_THROW_IDX;
				if (exceptionBps[id] == null) {
					Location location = new Location(null, EXCEPTION_FUNCS[id], 0);
					Breakpoint bp = new Breakpoint(target, ICDIBreakpoint.REGULAR, location, null);
					setLocationBreakpoint(bp);
					exceptionBps[id] = bp;
					if (miBreakpoints != null) {
						MIBreakpoint[] mibp = bp.getMIBreakpoints();
						MIBreakpoint[] temp = new MIBreakpoint[miBreakpoints.length + mibp.length];
						System.arraycopy(miBreakpoints, 0, temp, 0, miBreakpoints.length);
						System.arraycopy(mibp, 0, temp, miBreakpoints.length, mibp.length);
					} else {
						miBreakpoints = bp.getMIBreakpoints();
					}
				}
			}
		}

		Exceptionpoint excp = new Exceptionpoint(target, clazz, stopOnThrow, stopOnCatch);
		if (miBreakpoints != null && miBreakpoints.length > 0) {
			excp.setMIBreakpoints(miBreakpoints);
			List blist = getBreakpointsList(target);
			blist.add(excp);

			// Fire a created Event.
			MISession miSession = target.getMISession();
			miSession.fireEvent(new MIBreakpointCreatedEvent(miSession, miBreakpoints[0].getNumber()));
		}
		return excp;
	}

	public Condition createCondition(int ignoreCount, String expression, String[] tids) {
		return new Condition(ignoreCount, expression, tids);
	}

	public Location createLocation(String file, String function, int line) {
		return new Location(file, function, line);
	}
	
	public Location createLocation(BigInteger address) {
		return new Location(address);
	}

}
