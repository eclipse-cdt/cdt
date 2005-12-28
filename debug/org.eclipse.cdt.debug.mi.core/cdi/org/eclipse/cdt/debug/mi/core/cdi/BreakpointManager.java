/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExceptionpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.AddressBreakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.AddressLocation;
import org.eclipse.cdt.debug.mi.core.cdi.model.Breakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.Exceptionpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.FunctionBreakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.FunctionLocation;
import org.eclipse.cdt.debug.mi.core.cdi.model.LineBreakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.LocationBreakpoint;
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
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetBreakpointPending;
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
		if (allowInterrupt && target.isRunning()) {
			// Disable events.
			((EventManager)getSession().getEventManager()).allowProcessingEvents(false);
			target.suspend();
			shouldRestart = true;
		}
		return shouldRestart;
	}

	void resumeInferior(Target target, boolean shouldRestart) throws CDIException {
		if (shouldRestart) {
			target.resume();
			// Enable events again.
			((EventManager)getSession().getEventManager()).allowProcessingEvents(true);
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

		// Check if the breakpoint is in the deffered list
		List dList = (List)deferredMap.get(target);
		if (dList != null) {
			if (dList.contains(breakpoint)) {
				breakpoint.setEnabled0(true);
				return; // bail out here, our work is done.
			}
		}

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

		boolean restart = false;
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIBreakEnable breakEnable = factory.createMIBreakEnable(numbers);
		try {
			restart = suspendInferior(target);
			miSession.postCommand(breakEnable);
			MIInfo info = breakEnable.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			// Resume the program and enable events.
			resumeInferior(target, restart);
		}
		for (int i = 0; i < miBreakpoints.length; i++) {
			miBreakpoints[i].setEnabled(true);
		}
		breakpoint.setEnabled0(true);
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
		
		// Check if the breakpoint is in the deffered list
		List dList = (List)deferredMap.get(target);
		if (dList != null) {
			if (dList.contains(breakpoint)) {
				breakpoint.setEnabled0(false);
				return; // bail out here, our work is done.
			}
		}

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

		boolean restart = false;
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIBreakDisable breakDisable = factory.createMIBreakDisable(numbers);
		try {
			restart = suspendInferior(target);
			miSession.postCommand(breakDisable);
			MIInfo info = breakDisable.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			resumeInferior(target, restart);
		}
		for (int i = 0; i < miBreakpoints.length; i++) {
			miBreakpoints[i].setEnabled(false);
		}
		breakpoint.setEnabled0(false);
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
	public void setCondition(Breakpoint breakpoint, ICDICondition newCondition) throws CDIException {
		Target target = (Target)breakpoint.getTarget();

		// Check if the breakpoint is in the deffered list
		List dList = (List)deferredMap.get(target);
		if (dList != null) {
			if (dList.contains(breakpoint)) {
				breakpoint.setCondition0(newCondition);
				return; // bail out here, our work is done.
			}
		}

		List bList = (List)breakMap.get(target);
		if (bList == null) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
		}
		if (!bList.contains(breakpoint)) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
		}

		MIBreakpoint[] miBreakpoints = breakpoint.getMIBreakpoints();
		deleteMIBreakpoints(target, miBreakpoints);
		ICDICondition oldCondition = breakpoint.getCondition();
		boolean success = false;
		try {
			breakpoint.setCondition0(newCondition);
			if (breakpoint instanceof LocationBreakpoint) {
				setLocationBreakpoint((LocationBreakpoint)breakpoint);
			} else if (breakpoint instanceof Watchpoint) {
				setWatchpoint((Watchpoint)breakpoint);
			} else {
				throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$
			}
			success = true;
		} finally {
			if (!success) {
				breakpoint.setCondition0(oldCondition);
				if (breakpoint instanceof LocationBreakpoint) {
					setLocationBreakpoint((LocationBreakpoint)breakpoint);
				} else if (breakpoint instanceof Watchpoint) {
					setWatchpoint((Watchpoint)breakpoint);
				}
			}
		}

		// Fire a changed Event.
		miBreakpoints = breakpoint.getMIBreakpoints();
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
						watchType |= ICDIWatchpoint.READ;
					}
					if (allMIBreakpoints[i].isAccessWatchpoint() || allMIBreakpoints[i].isWriteWatchpoint()) {
						watchType |= ICDIWatchpoint.WRITE;
					}
					Watchpoint wpoint = new Watchpoint(target, allMIBreakpoints[i].getWhat(), type, watchType, condition);
					wpoint.setMIBreakpoints(new MIBreakpoint[] {allMIBreakpoints[i]});
					bList.add(wpoint);
				} else {
					String function = allMIBreakpoints[i].getFunction();
					String file = allMIBreakpoints[i].getFile();
					int line = allMIBreakpoints[i].getLine();
					String addr = allMIBreakpoints[i].getAddress();

					if (file != null && file.length() > 0 && line > 0) {
						LineLocation location = createLineLocation (allMIBreakpoints[i].getFile(),
								allMIBreakpoints[i].getLine());
						// By default new breakpoint are LineBreakpoint
						Breakpoint newBreakpoint = new LineBreakpoint(target, type, location, condition);
						newBreakpoint.setMIBreakpoints(new MIBreakpoint[] {allMIBreakpoints[i]});
						bList.add(newBreakpoint);
					} else if (function != null && function.length() > 0) {
						FunctionLocation location = createFunctionLocation(file, function);
						// By default new breakpoint are LineBreakpoint
						Breakpoint newBreakpoint = new FunctionBreakpoint(target, type, location, condition);
						newBreakpoint.setMIBreakpoints(new MIBreakpoint[] {allMIBreakpoints[i]});
						bList.add(newBreakpoint);
					} else if (addr != null && addr.length() > 0) {
						BigInteger big = MIFormat.getBigInteger(addr);
						AddressLocation location = createAddressLocation (big);
						// By default new breakpoint are LineBreakpoint
						Breakpoint newBreakpoint = new AddressBreakpoint(target, type, location, condition);
						newBreakpoint.setMIBreakpoints(new MIBreakpoint[] {allMIBreakpoints[i]});
						bList.add(newBreakpoint);
					}
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
		List dList = (List)deferredMap.get(target);

		// Do the sanity check first, we will accept all or none
		if (bList == null) {
			throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
		}
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof Breakpoint && (bList.contains(breakpoints[i]) || (dList != null && dList.contains(breakpoints[i]))))) {
				throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Not_a_CDT_breakpoint")); //$NON-NLS-1$			
			}
		}

		MISession miSession = target.getMISession();
		List eventList = new ArrayList(breakpoints.length);
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(dList != null && dList.remove(breakpoints[i]))) {
				MIBreakpoint[] miBreakpoints = ((Breakpoint)breakpoints[i]).getMIBreakpoints();
				if (miBreakpoints.length > 0) {
					deleteMIBreakpoints(target, miBreakpoints);
					eventList.add(new MIBreakpointDeletedEvent(miSession, miBreakpoints[0].getNumber()));
				}
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
		boolean restart = false;
		try {
			restart = suspendInferior(target);
			deleteMIBreakpoints(miSession, numbers);
		} finally {
			resumeInferior(target, restart);
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setLineBreakpoint(int, org.eclipse.cdt.debug.core.cdi.ICDILineLocation, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
	 */
	public ICDILineBreakpoint setLineBreakpoint(Target target, int type, ICDILineLocation location,
			ICDICondition condition, boolean deferred) throws CDIException {		
		LineBreakpoint bkpt = new LineBreakpoint(target, type, location, condition);
		setNewLocationBreakpoint(bkpt, deferred);
		return bkpt;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setFunctionBreakpoint(int, org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
	 */
	public ICDIFunctionBreakpoint setFunctionBreakpoint(Target target, int type, ICDIFunctionLocation location,
			ICDICondition condition, boolean deferred) throws CDIException {		
		FunctionBreakpoint bkpt = new FunctionBreakpoint(target, type, location, condition);
		setNewLocationBreakpoint(bkpt, deferred);
		return bkpt;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setAddressBreakpoint(int, org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
	 */
	public ICDIAddressBreakpoint setAddressBreakpoint(Target target, int type, ICDIAddressLocation location,
			ICDICondition condition, boolean deferred) throws CDIException {		
		AddressBreakpoint bkpt = new AddressBreakpoint(target, type, location, condition);
		setNewLocationBreakpoint(bkpt, deferred);
		return bkpt;
	}


	protected void setNewLocationBreakpoint(LocationBreakpoint bkpt, boolean deferred) throws CDIException {
		Target target = (Target)bkpt.getTarget();
		MISession miSession = target.getMISession();
		try {
			setLocationBreakpoint(bkpt);
			List blist = getBreakpointsList(target);
			blist.add(bkpt);

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
	}

	public ICDIWatchpoint setWatchpoint(Target target, int type, int watchType, String expression,
			ICDICondition condition) throws CDIException {

		// HACK: for the IDE,
		try {
			// Check if this an address watchpoint, and add a '*'
			Integer.decode(expression);
			expression = '*' + expression;
		} catch (NumberFormatException e) {
			//
		}
		Watchpoint bkpt = new Watchpoint(target, expression, type, watchType, condition);

		setWatchpoint(bkpt);
		List bList = getBreakpointsList(target);
		bList.add(bkpt);

		// Fire a created Event.
		MIBreakpoint[] miBreakpoints = bkpt.getMIBreakpoints();
		if (miBreakpoints != null && miBreakpoints.length > 0) {
			MISession miSession = target.getMISession();
			miSession.fireEvent(new MIBreakpointCreatedEvent(miSession, miBreakpoints[0].getNumber()));
		}
		return bkpt;
	}

	public void setLocationBreakpoint (LocationBreakpoint bkpt) throws CDIException {
		Target target = (Target)bkpt.getTarget();
		MISession miSession = target.getMISession();
		MIBreakInsert[] breakInserts = createMIBreakInsert(bkpt);
		List pointList = new ArrayList();
		boolean restart = false;
		try {
			restart = suspendInferior(target);
			CommandFactory factory = miSession.getCommandFactory();
			boolean enable = bkpt.isEnabled();
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
				// Make sure that if the breakpoint was disable we create them disable.
				if (!enable) {
					int[] numbers = new int[points.length];
					for (int j = 0; j < points.length; j++) {
						numbers[j] = points[j].getNumber();
					}
					MIBreakDisable breakDisable = factory.createMIBreakDisable(numbers);
					try {
						miSession.postCommand(breakDisable);
						MIInfo disableInfo = breakDisable.getMIInfo();
						if (disableInfo == null) {
							throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
						}
					} catch (MIException e) {
						throw new MI2CDIException(e);
					}
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
			resumeInferior(target, restart);
		}
		MIBreakpoint[] allPoints = (MIBreakpoint[]) pointList.toArray(new MIBreakpoint[pointList.size()]);
		bkpt.setMIBreakpoints(allPoints);
	}

	public void setWatchpoint(Watchpoint watchpoint) throws CDIException {
		Target target = (Target)watchpoint.getTarget();
		boolean access = watchpoint.isReadType() && watchpoint.isWriteType();
		boolean read = ! watchpoint.isWriteType() && watchpoint.isReadType();
		String expression = watchpoint.getWatchExpression();

		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIBreakWatch breakWatch =
			factory.createMIBreakWatch(access, read, expression);
		MIBreakpoint[] points = null;
		boolean restart = false;
		try {
			restart = suspendInferior(target);
			miSession.postCommand(breakWatch);
			MIBreakWatchInfo winfo = breakWatch.getMIBreakWatchInfo();
			if (winfo == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			points = winfo.getMIBreakpoints();
			if (points == null || points.length == 0) {
				throw new CDIException(CdiResources.getString("cdi.BreakpointManager.Parsing_Error")); //$NON-NLS-1$
			}

			int no = points[0].getNumber();

			// Put the condition now.
			String exprCond = null;
			int ignoreCount = 0;

			ICDICondition condition = watchpoint.getCondition();
			if (condition != null) {
				exprCond = condition.getExpression();
				ignoreCount = condition.getIgnoreCount();
			}
			if (exprCond != null && exprCond.length() > 0) {
				MIBreakCondition breakCondition = factory.createMIBreakCondition(no, exprCond);				
				miSession.postCommand(breakCondition);
				MIInfo info = breakCondition.getMIInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
			}
			if (ignoreCount > 0) {
				MIBreakAfter breakAfter = factory.createMIBreakAfter(no, ignoreCount);
				miSession.postCommand(breakAfter);
				MIInfo info = breakAfter.getMIInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}				
			}
			// how to deal with threads ???
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			resumeInferior(target, restart);
		}
		watchpoint.setMIBreakpoints(points);
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
					FunctionLocation location = new FunctionLocation(null, EXCEPTION_FUNCS[id]);
					FunctionBreakpoint bp = new FunctionBreakpoint(target, ICDIBreakpoint.REGULAR, location, null);
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
					FunctionLocation location = new FunctionLocation(null, EXCEPTION_FUNCS[id]);
					FunctionBreakpoint bp = new FunctionBreakpoint(target, ICDIBreakpoint.REGULAR, location, null);
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

		Exceptionpoint excp = new Exceptionpoint(target, clazz, stopOnThrow, stopOnCatch, null);
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

	/**
	 * Call -gdb-set breakpoint pending set
	 * @param target
	 * @param set
	 * @throws CDIException
	 */
	public void setBreakpointPending(Target target, boolean set) throws CDIException { 
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIGDBSetBreakpointPending bpp = factory.createMIGDBSetBreakpointPending(set);
		try {
			miSession.postCommand(bpp);
			MIInfo info = bpp.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public Condition createCondition(int ignoreCount, String expression, String[] tids) {
		return new Condition(ignoreCount, expression, tids);
	}

	public LineLocation createLineLocation(String file, int line) {
		return new LineLocation(file, line);
	}
	
	public FunctionLocation createFunctionLocation(String file, String function) {
		return new FunctionLocation(file, function);
	}

	public AddressLocation createAddressLocation(BigInteger address) {
		return new AddressLocation(address);
	}

	MIBreakInsert[] createMIBreakInsert(LocationBreakpoint bkpt) throws CDIException {
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

		if (bkpt.getLocator() != null) {
			ICDILocator locator = bkpt.getLocator();
			String file = locator.getFile();
			String function = locator.getFunction();
			int no = locator.getLineNumber();
			if (bkpt instanceof LineBreakpoint) {
				if (file != null && file.length() > 0) {
					line.append(file).append(':');
				}
				line.append(no);				
			} else if (bkpt instanceof FunctionBreakpoint) {
				if (function != null && function.length() > 0) {
					// if the function contains :: assume the user
					// knows the exact funciton
					int colon = function.indexOf("::"); //$NON-NLS-1$
					if (colon != -1) {
						line.append(function);
					} else {
						if (file != null && file.length() > 0) {
							line.append(file).append(':');
						}
						// GDB does not seem to accept function arguments when
						// we use file name:
						// (gdb) break file.c:Test(int)
						// Will fail, altought it can accept this
						// (gdb) break file.c:main
						// so fall back to the line number or
						// just the name of the function if lineno is invalid.
						int paren = function.indexOf('(');
						if (paren != -1) {
							if (no <= 0) {
								String func = function.substring(0, paren);
								line.append(func);
							} else {
								line.append(no);
							}
						} else {
							line.append(function);
						}
					}
				} else {
					// ???
					if (file != null && file.length() > 0) {
						line.append(file).append(':');
					}
					if (no > 0) {
						line.append(no);
					}
				}
			} else if (bkpt instanceof AddressBreakpoint) {
				line.append('*').append(locator.getAddress());				
			} else {
				// ???
				if (file != null && file.length() > 0) {
					line.append(file).append(':');
				}
				line.append(no);
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
}
