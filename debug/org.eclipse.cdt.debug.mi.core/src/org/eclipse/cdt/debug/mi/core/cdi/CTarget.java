/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
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
public class CTarget extends SessionObject implements ICDITarget {

	public CTarget(CSession session) {
		super(session);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#disconnect()
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#evaluateExpression(ICDIExpression)
	 */
	public void evaluateExpression(ICDIExpression expression)
		throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#evaluateExpression(String)
	 */
	public ICDIExpression evaluateExpression(String expressionText)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#finish()
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getCMemoryBlock(long, long)
	 */
	public ICDIMemoryBlock getCMemoryBlock(long startAddress, long length)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getErrorStream()
	 */
	public InputStream getErrorStream() {
		return getCSession().getMISession().getMIProcess().getErrorStream();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getInputStream()
	 */
	public InputStream getInputStream() {
		return getCSession().getMISession().getMIProcess().getInputStream();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		return getCSession().getMISession().getMIProcess().getOutputStream();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getGlobalVariables()
	 */
	public ICDIGlobalVariable[] getGlobalVariables() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getRegisterGroups()
	 */
	public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getSharedLibraries()
	 */
	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		return new ICDISharedLibrary[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getThread(String)
	 */
	public ICDIThread getThread(String id) throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getThreads()
	 */
	public ICDIThread[] getThreads() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isDisconnected()
	 */
	public boolean isDisconnected() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isStepping()
	 */
	public boolean isStepping() {
		return getCSession().getMISession().getMIProcess().isRunning();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isSuspended()
	 */
	public boolean isSuspended() {
		return getCSession().getMISession().getMIProcess().isSuspended();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isTerminated()
	 */
	public boolean isTerminated() {
		return getCSession().getMISession().getMIProcess().isTerminated();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#restart()
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#resume()
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepInto()
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepIntoInstruction()
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOver()
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOverInstruction()
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#suspend()
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#terminate()
	 */
	public void terminate() throws CDIException {
		getCSession().getMISession().getMIProcess().destroy();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getCDITarget()
	 */
	public ICDITarget getCDITarget() {
		return this;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getId()
	 */
	public String getId() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getParent()
	 */
	public ICDIObject getParent() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#evaluateExpressionToString(String)
	 */
	public String evaluateExpressionToString(String expressionText)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#evaluateExpressionToValue(String)
	 */
	public ICDIValue evaluateExpressionToValue(String expressionText)
		throws CDIException {
		return null;
	}

}
