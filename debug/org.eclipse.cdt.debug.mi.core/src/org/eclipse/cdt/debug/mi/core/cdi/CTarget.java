/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIRegisterObject;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MICommand;
import org.eclipse.cdt.debug.mi.core.command.MIDataEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.command.MIDataListRegisterNames;
import org.eclipse.cdt.debug.mi.core.command.MIExecContinue;
import org.eclipse.cdt.debug.mi.core.command.MIExecFinish;
import org.eclipse.cdt.debug.mi.core.command.MIExecNext;
import org.eclipse.cdt.debug.mi.core.command.MIExecNextInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecRun;
import org.eclipse.cdt.debug.mi.core.command.MIExecStep;
import org.eclipse.cdt.debug.mi.core.command.MIExecStepInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIInfoThreads;
import org.eclipse.cdt.debug.mi.core.command.MITargetDetach;
import org.eclipse.cdt.debug.mi.core.command.MIThreadSelect;
import org.eclipse.cdt.debug.mi.core.event.MIThreadExitEvent;
import org.eclipse.cdt.debug.mi.core.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.debug.mi.core.output.MIDataListRegisterNamesInfo;
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
	
	public CTarget(CSession s) {
		session = s;
		currentThreads = noThreads;
	}
	
	CSession getCSession() {
		return session;
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
		if (cthread instanceof CThread) {
			setCurrentThread((CThread)cthread);
		}
	}

	/**
	 */
	public void setCurrentThread(CThread cthread) throws CDIException {
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
				currentThreadId = info.getNewThreadId();
			} catch (MIException e) {
				throw new CDIException(e.getMessage());
			}
		}

		// We should be allright now.
		if (currentThreadId != id) {
			// thread is gone.  Generate a Thread destroyed.
			MISession mi = session.getMISession();
			mi.fireEvent(new MIThreadExitEvent(id));
			throw new CDIException("Thread destroyed");
		}
	}

	/**
	 * Called when stopping because of breakpoints etc ..
	 */
	void updateState(int newThreadId) {
		CThread[] oldThreads = currentThreads;
		// get the new Threads.
		CThread[] newThreads = getCThreads();

		// Fire destroyedEvent for old threads.
		if (oldThreads != null && oldThreads.length > 0) {
			List dList = new ArrayList(oldThreads.length);
			for (int i = 0; i < oldThreads.length; i++) {
				boolean found = false;
				for (int j = 0; j < newThreads.length; j++) {
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
		currentThreads = newThreads;
		currentThreadId = newThreadId;
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
			int[] ids = info.getThreadIds();
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
		if (currentThreads == null) {
			currentThreads = getCThreads();
		}
		return currentThreads;
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
			throw new CDIException(e.getMessage());
		}
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
				throw new CDIException(e.getMessage());
			}
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
			throw new CDIException(e.getMessage());
		}
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
			throw new CDIException(e.getMessage());
		}
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
			throw new CDIException(e.getMessage());
		}
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
			throw new CDIException(e.getMessage());
		}
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
			throw new CDIException(e.getMessage());
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#suspend()
	 */
	public void suspend() throws CDIException {
		session.getMISession().getMIInferior().interrupt();
		// send a noop to see if we get an aswer.
		MISession mi = session.getMISession();
		MICommand noop = new MICommand("");
		try {
			mi.postCommand(noop);
			MIInfo info = noop.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
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
			throw new CDIException(e.getMessage());
		}
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
			throw new CDIException(e.getMessage());
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
				throw new CDIException("No answer");
			}
			return info.getExpression();
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
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
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataListRegisterNames registers = 
			factory.createMIDataListRegisterNames();
		try {
			mi.postCommand(registers);
			MIDataListRegisterNamesInfo info =
				registers.getMIDataListRegisterNamesInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			String[] names = info.getRegisterNames();
			RegisterObject[] regs = new RegisterObject[names.length];
			for (int i = 0; i < names.length; i++) {
				regs[i] = new RegisterObject(names[i], i);
			}
			return regs;
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getSharedLibraries()
	 */
	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		return new ICDISharedLibrary[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getCMemoryBlock(long, long)
	 */
	public ICDIMemoryBlock getCMemoryBlock(long startAddress, long length)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getProcess()
	 */
	public Process getProcess() {
		return session.getMISession().getMIInferior();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#evaluateExpressionToValue(String)
	 */
	public ICDIValue evaluateExpressionToValue(String expressionText)
		throws CDIException {
		return null;
	}

}
