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
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.command.Command;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.command.MIExecContinue;
import org.eclipse.cdt.debug.mi.core.command.MIExecFinish;
import org.eclipse.cdt.debug.mi.core.command.MIExecNext;
import org.eclipse.cdt.debug.mi.core.command.MIExecNextInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecReturn;
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
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfoThreadsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIThreadSelectInfo;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;

/**
 */
public class Target  implements ICDITarget {

	Session session;
	Thread[] noThreads = new Thread[0];
	Thread[] currentThreads;
	int currentThreadId;
	Command lastExecutionCommand;
	
	public Target(Session s) {
		session = s;
		currentThreads = noThreads;
	}
	
	public Session getCSession() {
		return session;
	}

	public Command getLastExecutionCommand() {
		return lastExecutionCommand;
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
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIThreadSelect select = factory.createMIThreadSelect(id);
			try {
				mi.postCommand(select);
				MIThreadSelectInfo info = select.getMIThreadSelectInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
				}
				currentThreadId = info.getNewThreadId();
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}

			// Resetting threads may change the value of
			// some variables like Register.  Call an update()
			// To generate changeEvents.
			if (doUpdate) {
				RegisterManager regMgr = (RegisterManager)session.getRegisterManager();
				if (regMgr.isAutoUpdate()) {
					regMgr.update();
				}
				VariableManager varMgr = (VariableManager)session.getVariableManager();
				if (varMgr.isAutoUpdate()) {
					varMgr.update();
				}
			}
		}

		// We should be allright now.
		if (currentThreadId != id) {
			// thread is gone.  Generate a Thread destroyed.
			MISession mi = session.getMISession();
			mi.fireEvent(new MIThreadExitEvent(id));
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
				events[j] = new MIThreadCreatedEvent(id);
			}
			MISession miSession = session.getMISession();
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
				events[j] = new MIThreadExitEvent(id);
			}
			MISession miSession = session.getMISession();
			miSession.fireEvents(events);
		}
	}

	/**
	 * Do the real work of call -thread-list-ids.
	 */
	public Thread[] getCThreads() throws CDIException {
		Thread[] cthreads = noThreads;
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIInfoThreads tids = factory.createMIInfoThreads();
		try {
			// HACK/FIXME: gdb/mi thread-list-ids does not
			// show any newly create thread, we workaround by
			// issuing "info threads" instead.
			//MIThreadListIds tids = factory.createMIThreadListIds();
			//MIThreadListIdsInfo info = tids.getMIThreadListIdsInfo();
			mi.postCommand(tids);
			MIInfoThreadsInfo info = tids.getMIInfoThreadsInfo();
			int [] ids;
			if (info == null) {
				ids = new int[0];
			} else {
				ids = info.getThreadIds();
			}
			if (ids != null && ids.length > 0) {
				cthreads = new Thread[ids.length];
				// Ok that means it is a multiThreaded.
				for (int i = 0; i < ids.length; i++) {
					cthreads[i] = new Thread(this, ids[i]);
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
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecRun run = factory.createMIExecRun(new String[0]);
		lastExecutionCommand = run;
		try {
			mi.postCommand(run);
			MIInfo info = run.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#resume()
	 */
	public void resume() throws CDIException {
		MISession mi = session.getMISession();
		if (mi.getMIInferior().isRunning()) {
			throw new CDIException(CdiResources.getString("cdi.model.Target.Inferior_already_running")); //$NON-NLS-1$
		} else if (mi.getMIInferior().isSuspended()) {
			CommandFactory factory = mi.getCommandFactory();
			MIExecContinue cont = factory.createMIExecContinue();
			lastExecutionCommand = cont;
			try {
				mi.postCommand(cont);
				MIInfo info = cont.getMIInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		} else if (mi.getMIInferior().isTerminated()) {
			restart();
		} else {
			restart();
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepInto()
	 */
	public void stepInto() throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecStep step = factory.createMIExecStep();
		lastExecutionCommand = step;
		try {
			mi.postCommand(step);
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
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecStepInstruction stepi = factory.createMIExecStepInstruction();
		lastExecutionCommand = stepi;
		try {
			mi.postCommand(stepi);
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
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecNext next = factory.createMIExecNext();
		lastExecutionCommand = next;
		try {
			mi.postCommand(next);
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
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecNextInstruction nexti = factory.createMIExecNextInstruction();
		lastExecutionCommand = nexti;
		try {
			mi.postCommand(nexti);
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
		stepReturn(true);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepReturn(boolean)
	 */
	public void stepReturn(boolean execute) throws CDIException {
		if (execute) {
			finish();
		} else {
			execReturn();
		}
	}

	/**
	 */
	protected void finish() throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecFinish finish = factory.createMIExecFinish();
		lastExecutionCommand = finish;
		try {
			mi.postCommand(finish);
			MIInfo info = finish.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 */
	protected void execReturn() throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecReturn ret = factory.createMIExecReturn();
		lastExecutionCommand = ret;
		try {
			mi.postCommand(ret);
			MIInfo info = ret.getMIInfo();
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
		MISession mi = session.getMISession();
		try {
			mi.getMIInferior().interrupt();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#disconnect()
	 */
	public void disconnect() throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MITargetDetach detach = factory.createMITargetDetach();
		try {
			mi.postCommand(detach);
			MIInfo info = detach.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		// Unfortunately -target-detach does not generate an
		// event so we do it here.
		MISession miSession = session.getMISession();
		miSession.fireEvent(new MIDetachedEvent(detach.getToken()));
		session.getMISession().getMIInferior().setDisconnected();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#runUntil(ICDILocation)
	 */
	public void runUntil(ICDILocation location) throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String loc = ""; //$NON-NLS-1$
		if (location.getFile() != null && location.getFile().length() > 0) {
			loc = location.getFile() + ":" + location.getLineNumber(); //$NON-NLS-1$
		} else if (location.getFunction() != null && location.getFunction().length() > 0) {
			loc = location.getFunction();
		} else if (location.getAddress() != 0) {
			loc = "*" + location.getAddress(); //$NON-NLS-1$
		}
		MIExecUntil until = factory.createMIExecUntil(loc);
		lastExecutionCommand = until;
		try {
			mi.postCommand(until);
			MIInfo info = until.getMIInfo();
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
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String loc = ""; //$NON-NLS-1$
		if (location.getFile() != null && location.getFile().length() > 0) {
			loc = location.getFile() + ":" + location.getLineNumber(); //$NON-NLS-1$
		} else if (location.getFunction() != null && location.getFunction().length() > 0) {
			loc = location.getFunction();
		} else if (location.getAddress() != 0) {
			loc = "*" + location.getAddress(); //$NON-NLS-1$
		}
		MIJump jump = factory.createMIJump(loc);
		lastExecutionCommand = jump;
		try {
			mi.postCommand(jump);
			MIInfo info = jump.getMIInfo();
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
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataEvaluateExpression evaluate = 
			factory.createMIDataEvaluateExpression(expressionText);
		try {
			mi.postCommand(evaluate);
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
			session.getMISession().getMIInferior().terminate();
		} catch (MIException e) {
			session.terminate();
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isTerminated()
	 */
	public boolean isTerminated() {
		return session.getMISession().getMIInferior().isTerminated();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isDisconnected()
	 */
	public boolean isDisconnected() {
		return !session.getMISession().getMIInferior().isConnected();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isSuspended()
	 */
	public boolean isSuspended() {
		return session.getMISession().getMIInferior().isSuspended();
	}

	public boolean isRunning() {
		return session.getMISession().getMIInferior().isRunning();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getProcess()
	 */
	public Process getProcess() {
		return session.getMISession().getMIInferior();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal()
	 */
	public void signal() throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MISignal signal = factory.createMISignal("0"); //$NON-NLS-1$
		try {
			mi.postCommand(signal);
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
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MISignal sig = factory.createMISignal(signal.getName());
		try {
			mi.postCommand(sig);
			MIInfo info = sig.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

}
