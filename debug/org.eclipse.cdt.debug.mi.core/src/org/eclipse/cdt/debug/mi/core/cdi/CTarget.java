/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICObject;
import org.eclipse.cdt.debug.core.cdi.model.ICRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICSharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICTarget;
import org.eclipse.cdt.debug.core.cdi.model.ICThread;
import org.eclipse.cdt.debug.core.cdi.model.ICValue;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIExecContinue;
import org.eclipse.cdt.debug.mi.core.command.MIExecFinish;
import org.eclipse.cdt.debug.mi.core.command.MIExecInterrupt;
import org.eclipse.cdt.debug.mi.core.command.MIExecNext;
import org.eclipse.cdt.debug.mi.core.command.MIExecNextInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecRun;
import org.eclipse.cdt.debug.mi.core.command.MIExecStep;
import org.eclipse.cdt.debug.mi.core.command.MIExecStepInstruction;
import org.eclipse.cdt.debug.mi.core.command.MITargetDetach;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CTarget extends SessionObject implements ICTarget {

	public CTarget(CSession session) {
		super(session);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#disconnect()
	 */
	public void disconnect() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MITargetDetach detach = factory.createMITargetDetach();
		try {
			mi.postCommand(detach);
			MIInfo info = detach.getMIInfo();
			if (info == null) {
				// throw new CDIException();
			}
		} catch (MIException e) {
			//throw new CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#evaluateExpression(ICExpression)
	 */
	public void evaluateExpression(ICExpression expression)
		throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#evaluateExpression(String)
	 */
	public ICExpression evaluateExpression(String expressionText)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#finish()
	 */
	public void finish() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecFinish finish = factory.createMIExecFinish();
		try {
			mi.postCommand(finish);
			MIInfo info = finish.getMIInfo();
			if (info == null) {
				// throw new CDIException();
			}
		} catch (MIException e) {
			//throw new CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getCMemoryBlock(long, long)
	 */
	public ICMemoryBlock getCMemoryBlock(long startAddress, long length)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getErrorStream()
	 */
	public InputStream getErrorStream() {
		return getCSession().getMISession().getMIProcess().getErrorStream();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getInputStream()
	 */
	public InputStream getInputStream() {
		return getCSession().getMISession().getMIProcess().getInputStream();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		return getCSession().getMISession().getMIProcess().getOutputStream();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getGlobalVariables()
	 */
	public ICGlobalVariable[] getGlobalVariables() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getRegisterGroups()
	 */
	public ICRegisterGroup[] getRegisterGroups() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getSharedLibraries()
	 */
	public ICSharedLibrary[] getSharedLibraries() throws CDIException {
		return new ICSharedLibrary[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getThread(String)
	 */
	public ICThread getThread(String id) throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getThreads()
	 */
	public ICThread[] getThreads() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#isDisconnected()
	 */
	public boolean isDisconnected() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#isStepping()
	 */
	public boolean isStepping() {
		return getCSession().getMISession().getMIProcess().isRunning();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#isSuspended()
	 */
	public boolean isSuspended() {
		return getCSession().getMISession().getMIProcess().isSuspended();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#isTerminated()
	 */
	public boolean isTerminated() {
		return getCSession().getMISession().getMIProcess().isTerminated();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#restart()
	 */
	public void restart() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecRun run = factory.createMIExecRun(new String[0]);
		try {
			mi.postCommand(run);
			MIInfo info = run.getMIInfo();
			if (info == null) {
				// throw new CDIException();
			}
		} catch (MIException e) {
			//throw new CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#resume()
	 */
	public void resume() throws CDIException {
		MISession mi = getCSession().getMISession();
		if (mi.getMIProcess().isSuspended()) {
			CommandFactory factory = mi.getCommandFactory();
			MIExecContinue cont = factory.createMIExecContinue();
			try {
				mi.postCommand(cont);
				MIInfo info = cont.getMIInfo();
				if (info == null) {
					// throw new CDIException();
				}
			} catch (MIException e) {
				//throw new CDIException(e);
			}
		} else {
			restart();
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#stepInto()
	 */
	public void stepInto() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecStep step = factory.createMIExecStep();
		try {
			mi.postCommand(step);
			MIInfo info = step.getMIInfo();
			if (info == null) {
				// throw new CDIException();
			}
		} catch (MIException e) {
			//throw new CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#stepIntoInstruction()
	 */
	public void stepIntoInstruction() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecStepInstruction stepi = factory.createMIExecStepInstruction();
		try {
			mi.postCommand(stepi);
			MIInfo info = stepi.getMIInfo();
			if (info == null) {
				// throw new CDIException();
			}
		} catch (MIException e) {
			//throw new CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#stepOver()
	 */
	public void stepOver() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecNext next = factory.createMIExecNext();
		try {
			mi.postCommand(next);
			MIInfo info = next.getMIInfo();
			if (info == null) {
				// throw new CDIException();
			}
		} catch (MIException e) {
			//throw new CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#stepOverInstruction()
	 */
	public void stepOverInstruction() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecNextInstruction nexti = factory.createMIExecNextInstruction();
		try {
			mi.postCommand(nexti);
			MIInfo info = nexti.getMIInfo();
			if (info == null) {
				// throw new CDIException();
			}
		} catch (MIException e) {
			//throw new CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#suspend()
	 */
	public void suspend() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIExecInterrupt interrupt = factory.createMIExecInterrupt();
		try {
			mi.postCommand(interrupt);
			MIInfo info = interrupt.getMIInfo();
			if (info == null) {
				// throw new CDIException();
			}
		} catch (MIException e) {
			//throw new CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#terminate()
	 */
	public void terminate() throws CDIException {
		getCSession().getMISession().getMIProcess().destroy();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICObject#getCDITarget()
	 */
	public ICTarget getCDITarget() {
		return this;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICObject#getId()
	 */
	public String getId() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICObject#getParent()
	 */
	public ICObject getParent() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#evaluateExpressionToString(String)
	 */
	public String evaluateExpressionToString(String expressionText)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#evaluateExpressionToValue(String)
	 */
	public ICValue evaluateExpressionToValue(String expressionText)
		throws CDIException {
		return null;
	}

}
