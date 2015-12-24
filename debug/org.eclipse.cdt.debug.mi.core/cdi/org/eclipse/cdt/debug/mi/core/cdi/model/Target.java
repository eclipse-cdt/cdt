/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ken Ryall (Nokia) - 175532 support the address to source location API
 *     Alena Laskavaia (QNX) - Bug 221224
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFileLocation;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressToSource;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement3;
import org.eclipse.cdt.debug.core.cdi.model.ICDIEventBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExceptionpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteMoveInstructionPointer;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.mi.core.CoreProcess;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIInferior;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.RxThread;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;
import org.eclipse.cdt.debug.mi.core.cdi.EventManager;
import org.eclipse.cdt.debug.mi.core.cdi.ExpressionManager;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.MemoryManager;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SessionObject;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.SignalManager;
import org.eclipse.cdt.debug.mi.core.cdi.SourceManager;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.command.CLIInfoLine;
import org.eclipse.cdt.debug.mi.core.command.CLIInfoThreads;
import org.eclipse.cdt.debug.mi.core.command.CLIJump;
import org.eclipse.cdt.debug.mi.core.command.CLISignal;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.command.MIExecContinue;
import org.eclipse.cdt.debug.mi.core.command.MIExecNext;
import org.eclipse.cdt.debug.mi.core.command.MIExecNextInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecRun;
import org.eclipse.cdt.debug.mi.core.command.MIExecStep;
import org.eclipse.cdt.debug.mi.core.command.MIExecStepInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecUntil;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowEndian;
import org.eclipse.cdt.debug.mi.core.command.MITargetDetach;
import org.eclipse.cdt.debug.mi.core.command.MIThreadSelect;
import org.eclipse.cdt.debug.mi.core.event.MIDetachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadExitEvent;
import org.eclipse.cdt.debug.mi.core.output.CLIInfoLineInfo;
import org.eclipse.cdt.debug.mi.core.output.CLIInfoThreadsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowEndianInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIThreadSelectInfo;

/**
 */
public class Target extends SessionObject implements ICDITarget, ICDIBreakpointManagement3, ICDIAddressToSource, ICDIExecuteMoveInstructionPointer {

	MISession miSession;
	ICDITargetConfiguration fConfiguration;
	Thread[] noThreads = new Thread[0];
	Thread[] currentThreads;
	int currentThreadId;
	String fEndian = null;
	boolean suspended = true;
	boolean deferBreakpoints = true;
	final private Object lock = new Object();
	
	public Target(Session s, MISession mi) {
		super(s);
		miSession = mi;
		currentThreads = noThreads;
	}

	/**
	 * Return lock object for target. Replacement for <code>lockTarget</code> and
	 * <code>releaseTarget</code> methods.
	 * <p>
	 * Use as synchronization object:
	 * </p>
	 * new code:
	 * 
	 * <pre>
	 *   synchronized (target.getLock()) {
	 *      ...
	 *   }
	 * </pre>
	 * 
	 * old code:
	 * 
	 * <pre>
	 *   target.lockTarget();
	 *   try {
	 *     ...
	 *   } finally {
	 *     target.releaseTarget();
	 *   }
	 * </pre>
	 * 
	 * @since 5.0
	 */
	public Object getLock() {
		return lock;
	}

	
	public MISession getMISession() {
		return miSession;
	}

	public void setConfiguration(ICDITargetConfiguration configuration) {
		fConfiguration = configuration;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
	 */
	@Override
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

	public synchronized void setSupended(boolean state) {
		suspended = state;
		notifyAll();
	}

	/**
	 */
	public void setCurrentThread(Thread cthread, boolean doUpdate) throws CDIException {

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
				// GDB reset the currentFrame to some other level 0 when switching thread.
				// we need to reposition the current stack level.
				MIFrame miFrame = info.getFrame();
				if (miFrame != null) {
					int depth = cthread.getStackFrameCount();
					cthread.currentFrame = new StackFrame(cthread, miFrame, depth - miFrame.getLevel());
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}

			Session session = (Session)getSession();
			// Resetting threads may change the value of
			// some variables like Register. Call an update()
			// To generate changeEvents.
			if (doUpdate) {
				RegisterManager regMgr = session.getRegisterManager();
				if (regMgr.isAutoUpdate()) {
					regMgr.update(this);
				}
				VariableManager varMgr = session.getVariableManager();
				if (varMgr.isAutoUpdate()) {
					varMgr.update(this);
				}
			}
		}

		// We should be allright now.
		if (currentThreadId != id) {
			// thread is gone. Generate a Thread destroyed.
			miSession.fireEvent(new MIThreadExitEvent(miSession, id));
			throw new CDIException(CdiResources.getString("cdi.model.Target.Cannot_switch_to_thread") + id); //$NON-NLS-1$
		}
	}

	/**
	 * Called when stopping because of breakpoints etc ..
	 */
	public synchronized void updateState(int newThreadId) {
		Thread[] oldThreads = currentThreads;

		// If we use "info threads" in getCThreads() this
		// will be overwritten. However if we use -stack-list-threads
		// it does not provide to the current thread
		synchronized (lock) {
			try {
				// get the new Threads.
				currentThreadId = newThreadId;
				currentThreads = getCThreads();
			} catch (CDIException e) {
				currentThreads = noThreads;
			}
		}

		// Fire CreatedEvent for new threads.
		// Replace the new threads with the old thread object
		// User may have old on to the old Thread object.
		List<Integer> cList = new ArrayList<Integer>(currentThreads.length);
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
				int id = cList.get(j);
				events[j] = new MIThreadCreatedEvent(miSession, id);
			}
			miSession.fireEvents(events);
		}

		// Fire destroyedEvent for old threads.
		List<Integer> dList = new ArrayList<Integer>(oldThreads.length);
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
				int id = dList.get(j);
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
		synchronized (lock) {
			RxThread rxThread = miSession.getRxThread();
			rxThread.setEnableConsole(false);
			try {
				CommandFactory factory = miSession.getCommandFactory();
				CLIInfoThreads tids = factory.createCLIInfoThreads();
				// HACK/FIXME: gdb/mi thread-list-ids does not
				// show any newly create thread, we workaround by
				// issuing "info threads" instead.
				// MIThreadListIds tids = factory.createMIThreadListIds();
				// MIThreadListIdsInfo info = tids.getMIThreadListIdsInfo();
				miSession.postCommand(tids);
				CLIInfoThreadsInfo info = tids.getMIInfoThreadsInfo();
				int[] ids;
				String[] names;
				if (info == null) {
					ids = new int[0];
					names = new String[0];
				} else {
					ids = info.getThreadIds();
					names = info.getThreadNames();
					currentThreadId = info.getCurrentThread();
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
				// FIX: When attaching there is no thread selected
				// We will choose the first one as a workaround.
				if (currentThreadId == 0 && cthreads.length > 0) {
					setCurrentThread(cthreads[0], false);				
				}
			} catch (MIException e) {
				// Do not throw anything in this case.
				throw new CDIException(e.getMessage());
			} finally {
				rxThread.setEnableConsole(true);
			}
		}
		return cthreads;
	}

	@Override
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
	@Override
	public synchronized ICDIThread[] getThreads() throws CDIException {
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

	public boolean isLittleEndian() throws CDIException {
		if (fEndian == null) {
			CommandFactory factory = miSession.getCommandFactory();
			MIGDBShowEndian endian = factory.createMIGDBShowEndian();
			try {
				miSession.postCommand(endian);
				MIGDBShowEndianInfo info = endian.getMIShowEndianInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
				}
				fEndian = info.isLittleEndian() ? "le" : "be"; //$NON-NLS-1$ //$NON-NLS-2$
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		}
		return fEndian.equals("le"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#restart()
	 */
	@Override
	public void restart() throws CDIException {
		// Reset the inferior pid
		MIInferior inferior = getMISession().getMIInferior();
		int pid = inferior.resetInferiorPID();
		CommandFactory factory = miSession.getCommandFactory();
		MIExecRun run = factory.createMIExecRun(new String[0]);
		try {
			miSession.postCommand(run);
			MIInfo info = run.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			// Replace pid since we probably didn't actually restart
			inferior.setInferiorPID(pid);
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepInto()
	 */
	@Override
	public void stepInto() throws CDIException {
		stepInto(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepInto(int)
	 */
	@Override
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
	@Override
	public void stepIntoInstruction() throws CDIException {
		stepIntoInstruction(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepIntoInstruction(int)
	 */
	@Override
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
	@Override
	public void stepOver() throws CDIException {
		stepOver(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepOver(int)
	 */
	@Override
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
	@Override
	public void stepOverInstruction() throws CDIException {
		stepOverInstruction(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepOverInstruction(int)
	 */
	@Override
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
	@Override
	public void runUntil(ICDILocation location) throws CDIException {
		stepUntil(location);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepUntil(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	@Override
	public void stepUntil(ICDILocation location) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		String loc = null;
		File file = null;
		if (location instanceof ICDIFileLocation) {
			String filePath = ((ICDIFileLocation)location).getFile();
			if (filePath != null && filePath.length() > 0)
				file = new File(filePath);
		}
		if (location instanceof ICDILineLocation) {
			ICDILineLocation lineLocation = (ICDILineLocation)location;
			if (file != null) {
				loc = file.getName() + ":" + lineLocation.getLineNumber(); //$NON-NLS-1$
			}
		} else if (location instanceof ICDIFunctionLocation) {
			ICDIFunctionLocation funcLocation = (ICDIFunctionLocation)location;
			if (funcLocation.getFunction() != null && funcLocation.getFunction().length() > 0) {
				loc = funcLocation.getFunction();
			}
			if (file != null && loc != null) {
				loc = funcLocation.getFile() + ":" + loc; //$NON-NLS-1$
			}
		} else if (location instanceof ICDIAddressLocation) {
			ICDIAddressLocation addrLocation = (ICDIAddressLocation)location;
			if (!addrLocation.getAddress().equals(BigInteger.ZERO)) {
				loc = "*0x" + addrLocation.getAddress().toString(16); //$NON-NLS-1$
			}
		}
		// Throw an exception we do know where to go
		if (loc == null) {
			throw new CDIException(CdiResources.getString("cdi.mode.Target.Bad_location")); //$NON-NLS-1$
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
	@Override
	public void suspend() throws CDIException {
		try {
			miSession.getMIInferior().interrupt();
			// Wait till the EventManager tell us the go ahead
			long maxSec = miSession.getCommandTimeout()/1000 + 1;
			synchronized (this) {
				for (int i = 0; !suspended && i < maxSec; i++) {
					try {
						wait(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#disconnect()
	 */
	@Override
	public void disconnect() throws CDIException {
		// if Target is running try to suspend first.
		if (isRunning()) {
			try {
				((EventManager)getSession().getEventManager()).allowProcessingEvents(false);
				suspend();
			} finally {
				((EventManager)getSession().getEventManager()).allowProcessingEvents(true);
			}
		}
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
	@Override
	public void resume() throws CDIException {
		resume(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	@Override
	public void resume(ICDILocation location) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		String loc = null;
		File file = null;
		if (location instanceof ICDIFileLocation) {
			String filePath = ((ICDIFileLocation)location).getFile();
			if (filePath != null && filePath.length() > 0)
				file = new File(filePath);
		}
		if (location instanceof ICDILineLocation) {
			ICDILineLocation lineLocation = (ICDILineLocation)location;
			if (file != null) {
				loc = file.getName() + ":" + lineLocation.getLineNumber(); //$NON-NLS-1$
			}
		} else if (location instanceof ICDIFunctionLocation) {
			ICDIFunctionLocation funcLocation = (ICDIFunctionLocation)location;
			if (funcLocation.getFunction() != null && funcLocation.getFunction().length() > 0) {
				loc = funcLocation.getFunction();
			}
			if (file != null && loc != null) {
				loc = funcLocation.getFile() + ":" + loc; //$NON-NLS-1$
			}
		} else if (location instanceof ICDIAddressLocation) {
			ICDIAddressLocation addrLocation = (ICDIAddressLocation)location;
			if (!addrLocation.getAddress().equals(BigInteger.ZERO)) {
				loc = "*0x" + addrLocation.getAddress().toString(16); //$NON-NLS-1$
			}
		}
		// Throw an exception we do know where to go
		if (loc == null) {
			throw new CDIException(CdiResources.getString("cdi.mode.Target.Bad_location")); //$NON-NLS-1$
		}

		CLIJump jump = factory.createCLIJump(loc);
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteMoveInstructionPointer#moveInstructionPointer(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	/**
	 * @since 6.0
	 */
	@Override
	public void moveInstructionPointer(ICDILocation location) throws CDIException {
		// Most of this code was taken from our Resume()
		// method. The only differences are that we create a temporary
		// breakpoint for the location and set it before we post
		// the 'jump' command
		CommandFactory factory = miSession.getCommandFactory();
		LocationBreakpoint bkpt = null;
		String loc = null;
		File file = null;
		if (location instanceof ICDIFileLocation) {
			String filePath = ((ICDIFileLocation)location).getFile();
			if (filePath != null && filePath.length() > 0)
				file = new File(filePath);
		}
		if (location instanceof ICDILineLocation) {
			ICDILineLocation lineLocation = (ICDILineLocation)location;
			if (file != null) {
				loc = file.getName() + ":" + lineLocation.getLineNumber(); //$NON-NLS-1$
			}
			bkpt = new LineBreakpoint(this, ICBreakpointType.TEMPORARY, lineLocation, null, true);			
		} else if (location instanceof ICDIAddressLocation) {
			ICDIAddressLocation addrLocation = (ICDIAddressLocation)location;
			if (!addrLocation.getAddress().equals(BigInteger.ZERO)) {
				loc = "*0x" + addrLocation.getAddress().toString(16); //$NON-NLS-1$
			}
			bkpt = new AddressBreakpoint(this, ICBreakpointType.TEMPORARY, addrLocation, null, true);			
		}
		// Throw an exception we do know where to go
		if (loc == null) {
			throw new CDIException(CdiResources.getString("cdi.mode.Target.Bad_location")); //$NON-NLS-1$
		}

		// Set a temporary breakpoint at the location we're going
		// to do a 'jump' (resume from) operation
		Session session = (Session)getSession();
		session.getBreakpointManager().setLocationBreakpoint(bkpt);
		
		CLIJump jump = factory.createCLIJump(loc);
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
	 */
	@Override
	public void resume(ICDISignal signal) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		CLISignal sig = factory.createCLISignal(signal.getName());
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(boolean)
	 */
	@Override
	public void resume(boolean passSignal) throws CDIException {
		if (miSession.getMIInferior().isRunning()) {
			throw new CDIException(CdiResources.getString("cdi.model.Target.Inferior_already_running")); //$NON-NLS-1$
		} else if (miSession.getMIInferior().isSuspended()) {
			if (passSignal) {
				CommandFactory factory = miSession.getCommandFactory();
				CLISignal signal = factory.createCLISignal("0"); //$NON-NLS-1$
				try {
					miSession.postCommand(signal);
					MIInfo info = signal.getMIInfo();
					if (info == null) {
						throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
					}
				} catch (MIException e) {
					throw new MI2CDIException(e);
				}
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
	@Override
	public void jump(ICDILocation location) throws CDIException {
		resume(location);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal()
	 */
	@Override
	public void signal() throws CDIException {
		resume(true);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal(ICDISignal)
	 */
	@Override
	public void signal(ICDISignal signal) throws CDIException {
		resume(signal);
	}

	@Override
	public String evaluateExpressionToString(ICDIStackFrame frame, String expressionText) throws CDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame((StackFrame)frame, false);
		try {
			CommandFactory factory = miSession.getCommandFactory();
			MIDataEvaluateExpression evaluate = 
			factory.createMIDataEvaluateExpression(expressionText);
			miSession.postCommand(evaluate);
			MIDataEvaluateExpressionInfo info =
				evaluate.getMIDataEvaluateExpressionInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
			return info.getExpression();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#terminate()
	 */
	@Override
	public void terminate() throws CDIException {
		try {
			miSession.getMIInferior().terminate();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		return miSession.getMIInferior().isTerminated();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isDisconnected()
	 */
	@Override
	public boolean isDisconnected() {
		return !miSession.getMIInferior().isConnected();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isSuspended()
	 */
	@Override
	public boolean isSuspended() {
		return miSession.getMIInferior().isSuspended();
	}

	public boolean isRunning() {
		return miSession.getMIInferior().isRunning();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getProcess()
	 */
	@Override
	public Process getProcess() {
		if (miSession.isCoreSession()) {
			return new CoreProcess();
		}
		return miSession.getMIInferior();
	}

	// Implementaton of ICDIBreapointManagement.

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setLineBreakpoint(int, org.eclipse.cdt.debug.core.cdi.ICDILineLocation, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
	 */
	@Override
	public ICDILineBreakpoint setLineBreakpoint(int type, ICDILineLocation location,
			ICDICondition condition, boolean deferred) throws CDIException {		
		return this.setLineBreakpoint(type, location, condition, deferred, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setFunctionBreakpoint(int, org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
	 */
	@Override
	public ICDIFunctionBreakpoint setFunctionBreakpoint(int type, ICDIFunctionLocation location,
			ICDICondition condition, boolean deferred) throws CDIException {		
		return this.setFunctionBreakpoint(type, location, condition, deferred, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setAddressBreakpoint(int, org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
	 */
	@Override
	public ICDIAddressBreakpoint setAddressBreakpoint(int type, ICDIAddressLocation location,
			ICDICondition condition, boolean deferred) throws CDIException {		
		return this.setAddressBreakpoint(type, location, condition, deferred, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setWatchpoint(int, int, java.lang.String, org.eclipse.cdt.debug.core.cdi.ICDICondition)
	 */
	@Override
	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression,
		ICDICondition condition) throws CDIException {
		return this.setWatchpoint(type, watchType, expression, condition, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement2#setWatchpoint(int, int, java.lang.String, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
	 */
	@Override
	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression,
			ICDICondition condition, boolean enabled) throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		Watchpoint bkpt = new Watchpoint(this, expression, type, watchType, condition, enabled);
		bMgr.setWatchpoint(bkpt);
		return bkpt;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement2#setWatchpoint(int, int, java.lang.String, java.math.BigInteger, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
	 */
	@Override
	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression,
			String memorySpace, BigInteger range, ICDICondition condition, boolean enabled) throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		Watchpoint bkpt = new Watchpoint(this, expression, memorySpace, range, type, watchType, condition, enabled);
		bMgr.setWatchpoint(bkpt);
		return bkpt;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setExceptionBreakpoint(java.lang.String, boolean, boolean)
	 */
	@Override
	public ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch)
		throws CDIException {
		throw new CDIException(CdiResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#getBreakpoints()
	 */
	@Override
	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.getBreakpoints(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#deleteBreakpoints(org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint[])
	 */
	@Override
	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		bMgr.deleteBreakpoints(this, breakpoints);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#deleteAllBreakpoints()
	 */
	@Override
	public void deleteAllBreakpoints() throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		bMgr.deleteAllBreakpoints(this);
	}

	/*
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createCondition(int, java.lang.String, String)
	 */
	@Override
	public ICDICondition createCondition(int ignoreCount, String expression) {
		return createCondition(ignoreCount, expression, null);
	}

	 /* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createCondition(int, java.lang.String, String)
	 */
	@Override
	public ICDICondition createCondition(int ignoreCount, String expression, String[] tids) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createCondition(ignoreCount, expression, tids);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createLineLocation(java.lang.String, int)
	 */
	@Override
	public ICDILineLocation createLineLocation(String file, int line) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createLineLocation(file, line);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createFunctionLocation(java.lang.String, java.lang.String)
	 */
	@Override
	public ICDIFunctionLocation createFunctionLocation(String file, String function) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createFunctionLocation(file, function);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createLocation(long)
	 */
	@Override
	public ICDIAddressLocation createAddressLocation(BigInteger address) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createAddressLocation(address);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getRuntimeOptions()
	 */
	@Override
	public ICDIRuntimeOptions getRuntimeOptions() {
		return new RuntimeOptions(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpressionManagement#createExpression(java.lang.String)
	 */
	@Override
	public ICDIExpression createExpression(String code) throws CDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		return expMgr.createExpression(this, code);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpressionManagement#getExpressions()
	 */
	@Override
	public ICDIExpression[] getExpressions() throws CDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		return expMgr.getExpressions(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpressionManagement#destroyExpression(org.eclipse.cdt.debug.core.cdi.model.ICDIExpression[])
	 */
	@Override
	public void destroyExpressions(ICDIExpression[] expressions) throws CDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		expMgr.destroyExpressions(this, expressions);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpressionManagement#destroyAllExpression()
	 */
	@Override
	public void destroyAllExpressions() throws CDIException {
		ExpressionManager expMgr = ((Session)getSession()).getExpressionManager();
		expMgr.destroyAllExpressions(this);
	}

	/**
	 * Returns the array of signals defined for this target.
	 * 
	 * @return the array of signals
	 * @throws CDIException on failure. Reasons include:
	 */
	@Override
	public ICDISignal[] getSignals() throws CDIException {
		SignalManager sigMgr = ((Session)getSession()).getSignalManager();
		return sigMgr.getSignals(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#setSourcePaths(java.lang.String[])
	 */
	@Override
	public void setSourcePaths(String[] srcPaths) throws CDIException {
		SourceManager srcMgr = ((Session)getSession()).getSourceManager();
		srcMgr.setSourcePaths(this, srcPaths);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getSourcePaths()
	 */
	@Override
	public String[] getSourcePaths() throws CDIException {
		SourceManager srcMgr = ((Session)getSession()).getSourceManager();
		return srcMgr.getSourcePaths(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getInstructions(java.math.BigInteger, java.math.BigInteger)
	 */
	@Override
	public ICDIInstruction[] getInstructions(BigInteger startAddress, BigInteger endAddress) throws CDIException {
		SourceManager srcMgr = ((Session)getSession()).getSourceManager();
		return srcMgr.getInstructions(this, startAddress, endAddress);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getInstructions(java.lang.String, int)
	 */
	@Override
	public ICDIInstruction[] getInstructions(String filename, int linenum) throws CDIException {
		SourceManager srcMgr = ((Session)getSession()).getSourceManager();
		return srcMgr.getInstructions(this, filename, linenum);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getInstructions(java.lang.String, int, int)
	 */
	@Override
	public ICDIInstruction[] getInstructions(String filename, int linenum, int lines) throws CDIException {
		SourceManager srcMgr = ((Session)getSession()).getSourceManager();
		return srcMgr.getInstructions(this, filename, linenum, lines);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getMixedInstructions(java.math.BigInteger, java.math.BigInteger)
	 */
	@Override
	public ICDIMixedInstruction[] getMixedInstructions(BigInteger startAddress, BigInteger endAddress) throws CDIException {
		SourceManager srcMgr = ((Session)getSession()).getSourceManager();
		return srcMgr.getMixedInstructions(this, startAddress, endAddress);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getMixedInstructions(java.lang.String, int)
	 */
	@Override
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum) throws CDIException {
		SourceManager srcMgr = ((Session)getSession()).getSourceManager();
		return srcMgr.getMixedInstructions(this, filename, linenum);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement#getMixedInstructions(java.lang.String, int, int)
	 */
	@Override
	public ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws CDIException {
		SourceManager srcMgr = ((Session)getSession()).getSourceManager();
		return srcMgr.getMixedInstructions(this, filename, linenum, lines);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlockManagement#createMemoryBlock(java.lang.String, int)
	 */
	@Override
	public ICDIMemoryBlock createMemoryBlock(String address, int units, int wordSize) throws CDIException {
		MemoryManager memMgr = ((Session)getSession()).getMemoryManager();
		return memMgr.createMemoryBlock(this, address, units, wordSize);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlockManagement#removeBlocks(org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock[])
	 */
	@Override
	public void removeBlocks(ICDIMemoryBlock[] memoryBlocks) throws CDIException {
		MemoryManager memMgr = ((Session)getSession()).getMemoryManager();
		memMgr.removeBlocks(this, memoryBlocks);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlockManagement#removeAllBlocks()
	 */
	@Override
	public void removeAllBlocks() throws CDIException {
		MemoryManager memMgr = ((Session)getSession()).getMemoryManager();
		memMgr.removeAllBlocks(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlockManagement#getMemoryBlocks()
	 */
	@Override
	public ICDIMemoryBlock[] getMemoryBlocks() throws CDIException {
		MemoryManager memMgr = ((Session)getSession()).getMemoryManager();
		return memMgr.getMemoryBlocks(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibraryManagement#getSharedLibraries()
	 */
	@Override
	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		SharedLibraryManager sharedMgr = ((Session)getSession()).getSharedLibraryManager();
		return sharedMgr.getSharedLibraries(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getGlobalVariableDescriptors(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ICDIGlobalVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name) throws CDIException {
		VariableManager varMgr = ((Session)getSession()).getVariableManager();
		return varMgr.getGlobalVariableDescriptor(this, filename, function, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getRegisterGroups()
	 */
	@Override
	public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
		RegisterManager regMgr = ((Session)getSession()).getRegisterManager();
		return regMgr.getRegisterGroups(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getConfiguration()
	 */
	@Override
	public ICDITargetConfiguration getConfiguration() {
		if (fConfiguration == null) {
			if (miSession.isProgramSession()) {
				fConfiguration = new TargetConfiguration(this);
			} else if (miSession.isAttachSession()) {
				fConfiguration = new TargetConfiguration(this);
			} else if (miSession.isCoreSession()) {
				fConfiguration = new CoreFileConfiguration(this);
			} else {
				fConfiguration = new TargetConfiguration(this);
			}
		}
		return fConfiguration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createGlobalVariable(org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariableDescriptor)
	 */
	@Override
	public ICDIGlobalVariable createGlobalVariable(ICDIGlobalVariableDescriptor varDesc) throws CDIException {
		if (varDesc instanceof GlobalVariableDescriptor) {
			VariableManager varMgr = ((Session)getSession()).getVariableManager();
			return varMgr.createGlobalVariable((GlobalVariableDescriptor)varDesc);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createRegister(org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor)
	 */
	@Override
	public ICDIRegister createRegister(ICDIRegisterDescriptor varDesc) throws CDIException {
		if (varDesc instanceof RegisterDescriptor) {
			Session session = (Session)getTarget().getSession();
			RegisterManager mgr = session.getRegisterManager();
			return mgr.createRegister((RegisterDescriptor)varDesc);
		}
		return null;
	}

	public void deferBreakpoints(boolean defer) {
		this.deferBreakpoints = defer;
	}

	public boolean areBreakpointsDeferred() {
		return this.deferBreakpoints;
	}

	public void enableVerboseMode(boolean enabled) {
		miSession.enableVerboseMode(enabled);
	}

	public boolean isVerboseModeEnabled() {
		return miSession.isVerboseModeEnabled();
	}

	@Override
	public ICDIAddressBreakpoint setAddressBreakpoint(int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred, boolean enabled) throws CDIException {		
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.setAddressBreakpoint(this, type, location, condition, deferred, enabled);
	}

	@Override
	public ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch, boolean enabled) throws CDIException {
		throw new CDIException(CdiResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	@Override
	public ICDIFunctionBreakpoint setFunctionBreakpoint(int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred, boolean enabled) throws CDIException {		
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.setFunctionBreakpoint(this, type, location, condition, deferred, enabled);
	}

	@Override
	public ICDILineBreakpoint setLineBreakpoint(int type, ICDILineLocation location, ICDICondition condition, boolean deferred, boolean enabled) throws CDIException {		
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.setLineBreakpoint(this, type, location, condition, deferred, enabled);
	}

	@Override
	public IMappedSourceLocation getSourceForAddress(IAddress address) throws CDIException {
		// Ask gdb for info for this address, use the module list
		// to determine the executable.
		CommandFactory factory = miSession.getCommandFactory();
		CLIInfoLine cmd = factory.createCLIInfoLine(address);
		try {
			miSession.postCommand(cmd);
			CLIInfoLineInfo info = cmd.getMIInfoLineInfo();
			String fileName = ""; //$NON-NLS-1$
			ICDISharedLibrary[] libs = getSharedLibraries();
			BigInteger sourceAddress = address.getValue();
			for (int i = 0; i < libs.length; i++) {
				if (sourceAddress.compareTo(libs[i].getStartAddress()) > 0 && sourceAddress.compareTo(libs[i].getEndAddress()) < 0)
				{
					fileName = libs[i].getFileName();
				}
			}
			return new MappedSourceLocation(address, info, fileName);
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	@Override
	public ICDIEventBreakpoint setEventBreakpoint(String type, String arg, int cdiType, ICDICondition condition, boolean deferred,
			boolean enabled) throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.setEventBreakpoint(this,type,arg,condition,enabled);
	}
}
