/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIVariableManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.command.MIExecContinue;
import org.eclipse.cdt.debug.mi.core.command.MIExecFinish;
import org.eclipse.cdt.debug.mi.core.command.MIExecInterrupt;
import org.eclipse.cdt.debug.mi.core.command.MIExecNext;
import org.eclipse.cdt.debug.mi.core.command.MIExecNextInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecRun;
import org.eclipse.cdt.debug.mi.core.command.MIExecStep;
import org.eclipse.cdt.debug.mi.core.command.MIExecStepInstruction;
import org.eclipse.cdt.debug.mi.core.command.MITargetDetach;
import org.eclipse.cdt.debug.mi.core.command.MIThreadListIds;
import org.eclipse.cdt.debug.mi.core.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIThreadListIdsInfo;

/**
 */
public class CTarget  implements ICDITarget {

	List threadList;
	CSession session;
	CThread dummyThread; // Dummy for non multi-thread programs.
	CThread currentThread;
	
	public CTarget(CSession s) {
		session = s;
		threadList = new ArrayList(1);
		dummyThread = new CThread(this, 0);
		currentThread = dummyThread;
		threadList.add(dummyThread);
	}
	
	CSession getCSession() {
		return session;
	}

	void addCThread(CThread cthread) {
		threadList.add(cthread);
	}

	void removeCThread(CThread cthread) {
		threadList.remove(cthread);
	}

	void setCurrentThread(int id) {
		for (int i = 0; i < threadList.size(); i++) {
			CThread cthread = (CThread)threadList.get(i);
			if (cthread.getId() == id) {
				currentThread = cthread;
				return ;
			}
		}
	}

	boolean containsCThread(int id) {
		for (int i = 0; i < threadList.size(); i++) {
			CThread cthread = (CThread)threadList.get(i);
			if (cthread.getId() == id) {
				return true;
			}
		}
		return false;
	}

	CThread[] getCThreads() {
		return (CThread[])threadList.toArray(new CThread[threadList.size()]);
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
				throw new CDIException("Timedout");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
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
				throw new CDIException("Timedout");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getGlobalVariables()
	 */
	public ICDIGlobalVariable[] getGlobalVariables() throws CDIException {
		return new ICDIGlobalVariable[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getRegisterGroups()
	 */
	public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
		return new ICDIRegisterGroup[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getSharedLibraries()
	 */
	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		return new ICDISharedLibrary[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getCurrentThread()
	 */
	public ICDIThread getCurrentThread() throws CDIException {
		return currentThread;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getThreads()
	 */
	public ICDIThread[] getThreads() throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIThreadListIds tids = factory.createMIThreadListIds();
		try {
			mi.postCommand(tids);
			MIThreadListIdsInfo info = tids.getMIThreadListIdsInfo();
			int[] ids = info.getThreadIds();
			if (ids != null && ids.length > 0) {
				// Ok that means it is a multiThreaded, remove the dummy Thread
				removeCThread(dummyThread);
				for (int i = 0; i < ids.length; i++) {
					if (! containsCThread(ids[i])) {
						addCThread(new CThread(this, ids[i]));
					}
				}
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
		return (ICDIThread[])getCThreads();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isDisconnected()
	 */
	public boolean isDisconnected() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isSuspended()
	 */
	public boolean isSuspended() {
		return session.getMISession().getMIInferior().isSuspended();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isTerminated()
	 */
	public boolean isTerminated() {
		return session.getMISession().getMIInferior().isTerminated();
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
				throw new CDIException("Timedout");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
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
					throw new CDIException("Timedout");
				}
			} catch (MIException e) {
				throw new CDIException(e.toString());
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
				throw new CDIException("Timedout");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
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
				throw new CDIException("Timedout");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
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
				throw new CDIException("Timedout");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
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
				throw new CDIException("Timedout");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#suspend()
	 */
	public void suspend() throws CDIException {
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecInterrupt interrupt = factory.createMIExecInterrupt();
		try {
			mi.postCommand(interrupt);
			MIInfo info = interrupt.getMIInfo();
			if (info == null) {
				throw new CDIException("Timedout");
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#terminate()
	 */
	public void terminate() throws CDIException {
		session.getMISession().getMIInferior().destroy();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
	 */
	public ICDITarget getTarget() {
		return this;
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
				throw new CDIException("Timedout");
			}
			return info.getExpression();
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#evaluateExpressionToValue(String)
	 */
	public ICDIValue evaluateExpressionToValue(String expressionText)
		throws CDIException {
		ICDIVariableManager mgr = session.getVariableManager();
		ICDIExpression cexp = mgr.createExpression(expressionText);
		ICDIValue value = cexp.getValue();
		mgr.removeExpression(cexp);
		return value;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getSession()
	 */
	public ICDISession getSession() {
		return session;
	}
	
}
