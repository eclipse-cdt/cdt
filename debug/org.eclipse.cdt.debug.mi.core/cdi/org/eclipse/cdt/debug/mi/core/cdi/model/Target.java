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
package org.eclipse.cdt.debug.mi.core.cdi.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.command.MIExecContinue;
import org.eclipse.cdt.debug.mi.core.command.MIExecNext;
import org.eclipse.cdt.debug.mi.core.command.MIExecNextInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecRun;
import org.eclipse.cdt.debug.mi.core.command.MIExecStep;
import org.eclipse.cdt.debug.mi.core.command.MIExecStepInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecUntil;
import org.eclipse.cdt.debug.mi.core.command.MIInfoThreads;
import org.eclipse.cdt.debug.mi.core.command.MIJump;
import org.eclipse.cdt.debug.mi.core.command.MISignal;
import org.eclipse.cdt.debug.mi.core.command.MITargetDetach;
import org.eclipse.cdt.debug.mi.core.command.MIThreadSelect;
import org.eclipse.cdt.debug.mi.core.event.MIDetachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadExitEvent;
import org.eclipse.cdt.debug.mi.core.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfoThreadsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIThreadSelectInfo;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;

/**
 */
public class Target  implements ICDITarget {

	Session session;
	MISession miSession;
	Thread[] noThreads = new Thread[0];
	Thread[] currentThreads;
	int currentThreadId;
	
	public Target(Session s, MISession mi) {
		session = s;
		miSession = mi;
		currentThreads = noThreads;
	}

	public MISession getMISession() {
		return miSession;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getSession()
	 */
	public ICDISession getSession() {
		return session;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
	 */
	public ICDITarget getTarget() {
		return this;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#setCurrentThread(ICDIThread)
	 */
	public void setCurrentThread(ICDIThread cthread) throws CDIException {
		if (cthread instanceof Thread) {
			setCurrentThread(cthread, true);
		} else {
			throw new CDIException(CdiResources.getString("cdi.model.Target.Unknown_thread")); //$NON-NLS-1$
		}
	}
	
	public void setCurrentThread(ICDIThread cthread, boolean doUpdate) throws CDIException {
		if (cthread instanceof Thread) {
			setCurrentThread((Thread)cthread, doUpdate);
		} else {
			throw new CDIException(CdiResources.getString("cdi.model.Target.Unknown_thread")); //$NON-NLS-1$
		}
	}

	/**
	 */
	public void setCurrentThread(Thread cthread, boolean doUpdate) throws CDIException {
		// set us as the current target.
		session.setCurrentTarget(this);

		int id = cthread.getId();
		// No need to set thread id 0, it is a dummy thread.
		if (id == 0) {
			return;
		}
		// already the current thread?
		if (currentThreadId != id) {
			CommandFactory factory = miSession.getCommandFactory();
			MIThreadSelect select = factory.createMIThreadSelect(id);
			try {
				miSession.postCommand(select);
				MIThreadSelectInfo info = select.getMIThreadSelectInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
				}
				currentThreadId = info.getNewThreadId();

				// @^&#@^$*^$
				// GDB reset the currentFrame to  some other level 0 when switching thread.
				// we need to reposition the current stack level.
				MIFrame miFrame = info.getFrame();
				if (miFrame != null) {
					int depth = cthread.getStackFrameCount();
					cthread.currentFrame = new StackFrame(cthread, miFrame, depth - miFrame.getLevel());
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}

			// Resetting threads may change the value of
			// some variables like Register.  Call an update()
			// To generate changeEvents.
			if (doUpdate) {
				RegisterManager regMgr = (RegisterManager)session.getRegisterManager();
				if (regMgr.isAutoUpdate()) {
					regMgr.update(this);
				}
				VariableManager varMgr = (VariableManager)session.getVariableManager();
				if (varMgr.isAutoUpdate()) {
					varMgr.update(this);
				}
			}
		}

		// We should be allright now.
		if (currentThreadId != id) {
			// thread is gone.  Generate a Thread destroyed.
			miSession.fireEvent(new MIThreadExitEvent(miSession, id));
			throw new CDIException(CdiResources.getString("cdi.model.Target.Cannot_switch_to_thread") + id); //$NON-NLS-1$
		}
	}

	/**
	 * Called when stopping because of breakpoints etc ..
	 */
	public void updateState(int newThreadId) {
		Thread[] oldThreads = currentThreads;

		// If we use "info threads" in getCThreads() this
		// will be overwritten.  However if we use -stack-list-threads
		// it does not provide to the current thread
		currentThreadId = newThreadId;

		// get the new Threads.
		try {
			currentThreads = getCThreads();
		} catch (CDIException e) {
			currentThreads = noThreads;
		}

		// Fire CreatedEvent for new threads.
		// Replace the new threads with the old thread object
		// User may have old on to the old Thread object.
		List cList = new ArrayList(currentThreads.length);
		for (int i = 0; i < currentThreads.length; i++) {
			boolean found = false;
			for (int j = 0; j < oldThreads.length; j++) {
				if (currentThreads[i].getId() == oldThreads[j].getId()) {
					oldThreads[j].clearState();
					currentThreads[i] = oldThreads[j];
					found = true;
					break;
				}
			}
			if (!found) {
				cList.add(new Integer(currentThreads[i].getId()));
			}
		}
		if (!cList.isEmpty()) {
			MIThreadCreatedEvent[] events = new MIThreadCreatedEvent[cList.size()];
			for (int j = 0; j < events.length; j++) {
				int id = ((Integer)cList.get(j)).intValue();
				events[j] = new MIThreadCreatedEvent(miSession, id);
			}
			miSession.fireEvents(events);
		}

		// Fire destroyedEvent for old threads.
		List dList = new ArrayList(oldThreads.length);
		for (int i = 0; i < oldThreads.length; i++) {
			boolean found = false;
			for (int j = 0; j < currentThreads.length; j++) {
				if (currentThreads[j].getId() == oldThreads[i].getId()) {
					found = true;
					break;
				}
			}
			if (!found) {
				dList.add(new Integer(oldThreads[i].getId()));
			}
		}
		if (!dList.isEmpty()) {
			MIThreadExitEvent[] events = new MIThreadExitEvent[dList.size()];
			for (int j = 0; j < events.length; j++) {
				int id = ((Integer)dList.get(j)).intValue();
				events[j] = new MIThreadExitEvent(miSession, id);
			}
			miSession.fireEvents(events);
		}
	}

	/**
	 * Do the real work of call -thread-list-ids.
	 */
	public Thread[] getCThreads() throws CDIException {
		Thread[] cthreads = noThreads;
		CommandFactory factory = miSession.getCommandFactory();
		MIInfoThreads tids = factory.createMIInfoThreads();
		try {
			// HACK/FIXME: gdb/mi thread-list-ids does not
			// show any newly create thread, we workaround by
			// issuing "info threads" instead.
			//MIThreadListIds tids = factory.createMIThreadListIds();
			//MIThreadListIdsInfo info = tids.getMIThreadListIdsInfo();
			miSession.postCommand(tids);
			MIInfoThreadsInfo info = tids.getMIInfoThreadsInfo();
			int [] ids;
			String[] names;
			if (info == null) {
				ids = new int[0];
				names = new String[0];
			} else {
				ids = info.getThreadIds();
				names = info.getThreadNames();
			}
			if (ids != null && ids.length > 0) {
				cthreads = new Thread[ids.length];
				// Ok that means it is a multiThreaded.
				if (names != null && names.length == ids.length) {
					for (int i = 0; i < ids.length; i++) {
						cthreads[i] = new Thread(this, ids[i], names[i]);
					}
				} else {
					for (int i = 0; i < ids.length; i++) {
						cthreads[i] = new Thread(this, ids[i]);
					}
				}
			} else {
				// Provide a dummy.
				cthreads = new Thread[]{new Thread(this, 0)};
			}
			currentThreadId = info.getCurrentThread();
			//FIX: When attaching there is no thread selected
			// We will choose the first one as a workaround.
			if (currentThreadId == 0 && cthreads.length > 0) {
				currentThreadId = cthreads[0].getId();
			}
		} catch (MIException e) {
			// Do not throw anything in this case.
			throw new CDIException(e.getMessage());
		}
		return cthreads;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getCurrentThread()
	 */
	public ICDIThread getCurrentThread() throws CDIException {
		ICDIThread[] threads = getThreads();
		for (int i = 0; i < threads.length; i++) {
			Thread cthread = (Thread)threads[i];
			if (cthread.getId() == currentThreadId) {
				return cthread;
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getThreads()
	 */
	public ICDIThread[] getThreads() throws CDIException {
		if (currentThreads.length == 0) {
			currentThreads = getCThreads();
		}
		return currentThreads;
	}

	public ICDIThread getThread(int tid) {
		Thread th = null;
		if (currentThreads != null) {
			for (int i = 0; i < currentThreads.length; i++) {
				Thread cthread = currentThreads[i];
				if (cthread.getId() == tid) {
					th = cthread;
					break;
				}
			}
		}
		return th;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#restart()
	 */
	public void restart() throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MIExecRun run = factory.createMIExecRun(new String[0]);
		try {
			miSession.postCommand(run);
			MIInfo info = run.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepInto()
	 */
	public void stepInto() throws CDIException {
		stepInto(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepInto(int)
	 */
	public void stepInto(int count) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MIExecStep step = factory.createMIExecStep(count);
		try {
			miSession.postCommand(step);
			MIInfo info = step.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}		
	}


	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepIntoInstruction()
	 */
	public void stepIntoInstruction() throws CDIException {
		stepIntoInstruction(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepIntoInstruction(int)
	 */
	public void stepIntoInstruction(int count) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MIExecStepInstruction stepi = factory.createMIExecStepInstruction(count);
		try {
			miSession.postCommand(stepi);
			MIInfo info = stepi.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOver()
	 */
	public void stepOver() throws CDIException {
		stepOver(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepOver(int)
	 */
	public void stepOver(int count) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MIExecNext next = factory.createMIExecNext(count);
		try {
			miSession.postCommand(next);
			MIInfo info = next.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}		
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOverInstruction()
	 */
	public void stepOverInstruction() throws CDIException {
		stepOverInstruction(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepOverInstruction(int)
	 */
	public void stepOverInstruction(int count) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MIExecNextInstruction nexti = factory.createMIExecNextInstruction(count);
		try {
			miSession.postCommand(nexti);
			MIInfo info = nexti.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepReturn()
	 */
	public void stepReturn() throws CDIException {
		((Thread)getCurrentThread()).getCurrentStackFrame().stepReturn();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#runUntil(ICDILocation)
	 */
	public void runUntil(ICDILocation location) throws CDIException {
		stepUntil(location);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepUntil(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void stepUntil(ICDILocation location) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		String loc = ""; //$NON-NLS-1$
		if (location.getFile() != null && location.getFile().length() > 0) {
			loc = location.getFile() + ":" + location.getLineNumber(); //$NON-NLS-1$
		} else if (location.getFunction() != null && location.getFunction().length() > 0) {
			loc = location.getFunction();
		} else if (location.getAddress() != 0) {
			loc = "*" + location.getAddress(); //$NON-NLS-1$
		}
		MIExecUntil until = factory.createMIExecUntil(loc);
		try {
			miSession.postCommand(until);
			MIInfo info = until.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}		
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#suspend()
	 */
	public void suspend() throws CDIException {
		try {
			miSession.getMIInferior().interrupt();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#disconnect()
	 */
	public void disconnect() throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MITargetDetach detach = factory.createMITargetDetach();
		try {
			miSession.postCommand(detach);
			MIInfo info = detach.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		// Unfortunately -target-detach does not generate an
		// event so we do it here.
		miSession.fireEvent(new MIDetachedEvent(miSession, detach.getToken()));
		miSession.getMIInferior().setDisconnected();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#resume()
	 */
	public void resume() throws CDIException {
		resume(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void resume(ICDILocation location) throws CDIException {
		jump(location);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
	 */
	public void resume(ICDISignal signal) throws CDIException {
		signal(signal);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(boolean)
	 */
	public void resume(boolean passSignal) throws CDIException {
		if (miSession.getMIInferior().isRunning()) {
			throw new CDIException(CdiResources.getString("cdi.model.Target.Inferior_already_running")); //$NON-NLS-1$
		} else if (miSession.getMIInferior().isSuspended()) {
			if (passSignal) {
				signal();
			} else {
				continuation();
			}
		} else if (miSession.getMIInferior().isTerminated()) {
			restart();
		} else {
			restart();
		}
	}

	public void continuation() throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MIExecContinue cont = factory.createMIExecContinue();
		try {
			miSession.postCommand(cont);
			MIInfo info = cont.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}		
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#jump(ICDILocation)
	 */
	public void jump(ICDILocation location) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		String loc = ""; //$NON-NLS-1$
		if (location.getFile() != null && location.getFile().length() > 0) {
			loc = location.getFile() + ":" + location.getLineNumber(); //$NON-NLS-1$
		} else if (location.getFunction() != null && location.getFunction().length() > 0) {
			loc = location.getFunction();
		} else if (location.getAddress() != 0) {
			loc = "*" + location.getAddress(); //$NON-NLS-1$
		}
		MIJump jump = factory.createMIJump(loc);
		try {
			miSession.postCommand(jump);
			MIInfo info = jump.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal()
	 */
	public void signal() throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MISignal signal = factory.createMISignal("0"); //$NON-NLS-1$
		try {
			miSession.postCommand(signal);
			MIInfo info = signal.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal(ICDISignal)
	 */
	public void signal(ICDISignal signal) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MISignal sig = factory.createMISignal(signal.getName());
		try {
			miSession.postCommand(sig);
			MIInfo info = sig.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#evaluateExpressionToString(String)
	 */
	public String evaluateExpressionToString(String expressionText)
		throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MIDataEvaluateExpression evaluate = 
			factory.createMIDataEvaluateExpression(expressionText);
		try {
			miSession.postCommand(evaluate);
			MIDataEvaluateExpressionInfo info =
				evaluate.getMIDataEvaluateExpressionInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
			return info.getExpression();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#terminate()
	 */
	public void terminate() throws CDIException {
		try {
			miSession.getMIInferior().terminate();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			miSession.terminate();
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isTerminated()
	 */
	public boolean isTerminated() {
		return miSession.getMIInferior().isTerminated();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isDisconnected()
	 */
	public boolean isDisconnected() {
		return !miSession.getMIInferior().isConnected();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isSuspended()
	 */
	public boolean isSuspended() {
		return miSession.getMIInferior().isSuspended();
	}

	public boolean isRunning() {
		return miSession.getMIInferior().isRunning();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getProcess()
	 */
	public Process getProcess() {
		return miSession.getMIInferior();
	}

	// Implementaton of ICDIBreapointManagement.

	
	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.getBreakpoints(this);
	}

	public ICDILocationBreakpoint setLocationBreakpoint(int type, ICDILocation location,
			ICDICondition condition, String threadId, boolean deferred) throws CDIException {		
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.setLocationBreakpoint(this, type, location, condition, threadId, deferred);
	}

	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression,
			ICDICondition condition) throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.setWatchpoint(this, type, watchType, expression, condition);
	}

	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		bMgr.deleteBreakpoints(this, breakpoints);
	}

	public void deleteAllBreakpoints() throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		bMgr.deleteAllBreakpoints(this);		
	}

//	public ICDIExceptionBreakpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch)
//	throws CDIException {
//	throw new CDIException("Not Implemented"); //$NON-NLS-1$
//}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createCondition(int, java.lang.String)
	 */
	public ICDICondition createCondition(int ignoreCount, String expression) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createCondition(ignoreCount, expression);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createLocation(java.lang.String, java.lang.String, int)
	 */
	public ICDILocation createLocation(String file, String function, int line) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createLocation(file, function, line);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createLocation(long)
	 */
	public ICDILocation createLocation(long address) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createLocation(address);
	}

	
}
