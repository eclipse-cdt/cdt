/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDIRegisterObject;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.CSession;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.command.MIExecContinue;
import org.eclipse.cdt.debug.mi.core.command.MIExecFinish;
import org.eclipse.cdt.debug.mi.core.command.MIExecNext;
import org.eclipse.cdt.debug.mi.core.command.MIExecNextInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecRun;
import org.eclipse.cdt.debug.mi.core.command.MIExecStep;
import org.eclipse.cdt.debug.mi.core.command.MIExecStepInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecUntil;
import org.eclipse.cdt.debug.mi.core.command.MIInfoThreads;
import org.eclipse.cdt.debug.mi.core.command.MITargetDetach;
import org.eclipse.cdt.debug.mi.core.command.MIThreadSelect;
import org.eclipse.cdt.debug.mi.core.event.MIDetachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadExitEvent;
import org.eclipse.cdt.debug.mi.core.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfoThreadsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIThreadSelectInfo;

/**
 */
public class CTarget  implements ICDITarget {

	CSession session;
	CThread[] noThreads = new CThread[0];
	CThread[] currentThreads;
	int currentThreadId;
	int lastExecutionToken;
	
	public CTarget(CSession s) {
		session = s;
		currentThreads = noThreads;
	}
	
	public CSession getCSession() {
		return session;
	}

	public int getLastExecutionToken() {
		return lastExecutionToken;
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
		setCurrentThread(cthread, true);
	}
	
	public void setCurrentThread(ICDIThread cthread, boolean doUpdate) throws CDIException {
		if (cthread instanceof CThread) {
			setCurrentThread((CThread)cthread, doUpdate);
		}
	}

	/**
	 */
	public void setCurrentThread(CThread cthread, boolean doUpdate) throws CDIException {
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
					throw new CDIException("No Answer");
				}
				currentThreadId = info.getNewThreadId();
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}

			// Resetting threads may change the value of
			// some variables like Register.  Send an update
			// To generate changeEvents.
			if (doUpdate) {
				RegisterManager regMgr = session.getRegisterManager();
				regMgr.update();
			}
		}

		// We should be allright now.
		if (currentThreadId != id) {
			// thread is gone.  Generate a Thread destroyed.
			MISession mi = session.getMISession();
			mi.fireEvent(new MIThreadExitEvent(id));
			throw new CDIException("Can not swith to thread " + id);
		}
	}

	/**
	 * Called when stopping because of breakpoints etc ..
	 */
	public void updateState(int newThreadId) {
		CThread[] oldThreads = currentThreads;

		// If we use "info threads" in getCThreads() this
		// will be overwritten.  However if we use -stack-list-threads
		// it does not provide to the current thread
		currentThreadId = newThreadId;
		// get the new Threads.
		CThread[] newThreads = getCThreads();

		currentThreads = newThreads;

		// Fire CreatedEvent for new threads.
		if (newThreads != null && newThreads.length > 0) {
			List cList = new ArrayList(newThreads.length);
			for (int i = 0; i < newThreads.length; i++) {
				boolean found = false;
				for (int j = 0; oldThreads != null && j < oldThreads.length; j++) {
					if (newThreads[i].getId() == ((CThread)oldThreads[j]).getId()) {
						found = true;
						break;
					}
				}
				if (!found) {
					cList.add(new Integer(newThreads[i].getId()));
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
		}

		// Fire destroyedEvent for old threads.
		if (oldThreads != null && oldThreads.length > 0) {
			List dList = new ArrayList(oldThreads.length);
			for (int i = 0; i < oldThreads.length; i++) {
				boolean found = false;
				for (int j = 0; newThreads != null && j < newThreads.length; j++) {
					if (newThreads[j].getId() == ((CThread)oldThreads[i]).getId()) {
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
	}

	/**
	 * Do the real work of call -thread-list-ids.
	 */
	public CThread[] getCThreads() { //throws CDIException {
		CThread[] cthreads = noThreads;
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
				cthreads = new CThread[ids.length];
				// Ok that means it is a multiThreaded.
				for (int i = 0; i < ids.length; i++) {
					cthreads[i] = new CThread(this, ids[i]);
				}
			} else {
				// Provide a dummy.
				cthreads = new CThread[]{new CThread(this, 0)};
			}
			currentThreadId = info.getCurrentThread();
		} catch (MIException e) {
			// Do not throw anything in this case.
			//throw new CDIException(e.getMessage());
		}
		return cthreads;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getCurrentThread()
	 */
	public ICDIThread getCurrentThread() throws CDIException {
		ICDIThread[] threads = getThreads();
		for (int i = 0; i < threads.length; i++) {
			CThread cthread = (CThread)threads[i];
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
		CThread th = null;
		if (currentThreads != null) {
			for (int i = 0; i < currentThreads.length; i++) {
				CThread cthread = (CThread)currentThreads[i];
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
		try {
			mi.postCommand(run);
			MIInfo info = run.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		lastExecutionToken = run.getToken();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#resume()
	 */
	public void resume() throws CDIException {
		MISession mi = session.getMISession();
		if (mi.getMIInferior().isRunning()) {
			throw new CDIException("Inferior already running");
		} else if (mi.getMIInferior().isSuspended()) {
			CommandFactory factory = mi.getCommandFactory();
			MIExecContinue cont = factory.createMIExecContinue();
			try {
				mi.postCommand(cont);
				MIInfo info = cont.getMIInfo();
				if (info == null) {
					throw new CDIException("No answer");
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
			lastExecutionToken = cont.getToken();
		} else if (mi.getMIInferior().isTerminated()) {
			restart();
		} else {
			restart();
			//throw new CDIException("Unknow state");
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepInto()
	 */
	public void stepInto() throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecStep step = factory.createMIExecStep();
		try {
			mi.postCommand(step);
			MIInfo info = step.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		lastExecutionToken = step.getToken();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepIntoInstruction()
	 */
	public void stepIntoInstruction() throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecStepInstruction stepi = factory.createMIExecStepInstruction();
		try {
			mi.postCommand(stepi);
			MIInfo info = stepi.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		lastExecutionToken = stepi.getToken();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOver()
	 */
	public void stepOver() throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecNext next = factory.createMIExecNext();
		try {
			mi.postCommand(next);
			MIInfo info = next.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		lastExecutionToken = next.getToken();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOverInstruction()
	 */
	public void stepOverInstruction() throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecNextInstruction nexti = factory.createMIExecNextInstruction();
		try {
			mi.postCommand(nexti);
			MIInfo info = nexti.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		lastExecutionToken = nexti.getToken();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepReturn()
	 */
	public void stepReturn() throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecFinish finish = factory.createMIExecFinish();
		try {
			mi.postCommand(finish);
			MIInfo info = finish.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		lastExecutionToken = finish.getToken();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#suspend()
	 */
	public void suspend() throws CDIException {
		// send a noop to see if we get an aswer.
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
				throw new CDIException("No answer");
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#finish()
	 */
	public void finish() throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecFinish finish = factory.createMIExecFinish();
		try {
			mi.postCommand(finish);
			MIInfo info = finish.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		lastExecutionToken = finish.getToken();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#runUntil(ICDILocation)
	 */
	public void runUntil(ICDILocation location) throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String loc = "";
		if (location.getFile() != null) {
			loc = location.getFile() + ":" + location.getLineNumber();
		} else if (location.getFunction() != null) {
			loc = location.getFunction();
		} else if (location.getAddress() != 0) {
			loc = "*" + location.getAddress();
		}
		MIExecUntil until = factory.createMIExecUntil(loc);
		try {
			mi.postCommand(until);
			MIInfo info = until.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		lastExecutionToken = until.getToken();

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
				throw new CDIException("No answer");
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
		session.getMISession().getMIInferior().destroy();
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getGlobalVariables()
	 */
	public ICDIGlobalVariable[] getGlobalVariables() throws CDIException {
		return new ICDIGlobalVariable[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getRegisterObjects()
	 */
	public ICDIRegisterObject[] getRegisterObjects() throws CDIException {
		RegisterManager mgr = session.getRegisterManager();
		return mgr.getRegisterObjects();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getRegisters(ICDIRegisterObject[])
	*/
	public ICDIRegister[] getRegisters(ICDIRegisterObject[] regs) throws CDIException {
		ICDIRegister[] registers = null;
		RegisterManager mgr = session.getRegisterManager();
		registers = new ICDIRegister[regs.length];
		for (int i = 0; i < registers.length; i++) {
				registers[i] = mgr.createRegister(regs[i]);
		}
		return registers;
	}


	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getSharedLibraries()
	 */
	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		return new ICDISharedLibrary[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getProcess()
	 */
	public Process getProcess() {
		return session.getMISession().getMIInferior();
	}

}
