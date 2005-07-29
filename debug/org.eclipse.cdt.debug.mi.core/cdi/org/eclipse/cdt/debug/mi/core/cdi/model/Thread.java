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
package org.eclipse.cdt.debug.mi.core.cdi.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorage;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorageDescriptor;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIStackInfoDepth;
import org.eclipse.cdt.debug.mi.core.command.MIStackListFrames;
import org.eclipse.cdt.debug.mi.core.command.MIStackSelectFrame;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIStackInfoDepthInfo;
import org.eclipse.cdt.debug.mi.core.output.MIStackListFramesInfo;

/**
 */
public class Thread extends CObject implements ICDIThread {

	static ICDIStackFrame[] noStack = new ICDIStackFrame[0];
	int id;
	String name;
	StackFrame currentFrame;
	List currentFrames;
	int stackdepth = 0;

	final public static int STACKFRAME_DEFAULT_DEPTH = 200;

	public Thread(Target target, int threadId) {
		this(target, threadId, null);
	}

	public Thread(Target target, int threadId, String threadName) {
		super(target);
		id = threadId;
		name = threadName;
	}

	public int getId() {
		return id;
	}

	public void clearState() {
		stackdepth = 0;
		currentFrame = null;
		currentFrames = null;
	}

	public String toString() {
		String str = Integer.toString(id);
		if (name != null) {
			str += " " + name; //$NON-NLS-1$
		}
		return str;
	}

	public void updateState() {
		try {
			getCurrentStackFrame();
		} catch (CDIException e) {
		}
	}

	public StackFrame getCurrentStackFrame() throws CDIException {
		if (currentFrame == null) {
			ICDIStackFrame[] frames = getStackFrames(0, 0);
			if (frames.length > 0) {
				currentFrame = (StackFrame)frames[0];
			}
		}
		return currentFrame;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getStackFrames()
	 */
	public ICDIStackFrame[] getStackFrames() throws CDIException {

		// get the frames depth
		int depth = getStackFrameCount();

		// refresh if we have nothing or if we have just a subset get everything.
		if (currentFrames == null || currentFrames.size() < depth) {
			currentFrames = new ArrayList();
			Target target = (Target)getTarget();
			ICDIThread currentThread = target.getCurrentThread();
			target.setCurrentThread(this, false);
			try {
				MISession mi = target.getMISession();
				CommandFactory factory = mi.getCommandFactory();
				MIStackListFrames frames = factory.createMIStackListFrames();
				mi.postCommand(frames);
				MIStackListFramesInfo info = frames.getMIStackListFramesInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
				MIFrame[] miFrames = info.getMIFrames();
				for (int i = 0; i < miFrames.length; i++) {
					currentFrames.add(new StackFrame(this, miFrames[i], depth - miFrames[i].getLevel()));
				}
			} catch (MIException e) {
				//throw new CDIException(e.getMessage());
				//System.out.println(e);
			} catch (CDIException e) {
				//throw e;
				//System.out.println(e);
			} finally {
				target.setCurrentThread(currentThread, false);
			}
			// assign the currentFrame if it was not done yet.
			if (currentFrame == null) {
				for (int i = 0; i < currentFrames.size(); i++) {
					ICDIStackFrame stack = (ICDIStackFrame) currentFrames.get(i);
					if (stack.getLevel() == depth) {
						currentFrame = (StackFrame)stack;
					}
				}
			}
		}
		return (ICDIStackFrame[]) currentFrames.toArray(noStack);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getStackFrames()
	 */
	public int getStackFrameCount() throws CDIException {
		if (stackdepth == 0) {
			Target target = (Target)getTarget();
			ICDIThread currentThread = target.getCurrentThread();
			target.setCurrentThread(this, false);
			try {
				MISession mi = target.getMISession();
				CommandFactory factory = mi.getCommandFactory();
				MIStackInfoDepth depth = factory.createMIStackInfoDepth();
				mi.postCommand(depth);
				MIStackInfoDepthInfo info = null;
				try {
					// Catch the first exception gdb can recover the second time.
					info = depth.getMIStackInfoDepthInfo();
					if (info == null) {
						throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
					}
					stackdepth = info.getDepth();
				} catch (MIException e) {
					// First try fails, retry. gdb patches up the corrupt frame
					// so retry should give us a frame count that is safe.
					mi.postCommand(depth);
					info = depth.getMIStackInfoDepthInfo();
					if (info == null) {
						throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
					}
					stackdepth = info.getDepth();
					if (stackdepth > 0) {
						stackdepth--;
					}
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			} finally {
				target.setCurrentThread(currentThread, false);
			}
		}
		return stackdepth;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getStackFrames()
	 */
	public ICDIStackFrame[] getStackFrames(int low, int high) throws CDIException {
		if (currentFrames == null || currentFrames.size() < high) {
			currentFrames = new ArrayList();
			Target target = (Target) getTarget();
			ICDIThread currentThread = target.getCurrentThread();
			target.setCurrentThread(this, false);
			try {
				int depth = getStackFrameCount();
				int upperBound;
				// try to get the largest subset.
				// if what the user asks is smaller then the depth
				// try to cache things by getting the min(depth,STACKFRAME_DEFAULT_DEPTH)
				// else give fetch the entire thing.
				if (high < depth) {
					upperBound = Math.min(depth, STACKFRAME_DEFAULT_DEPTH);
				} else {
					upperBound = depth;
				}
				MISession mi = target.getMISession();
				CommandFactory factory = mi.getCommandFactory();
				MIStackListFrames frames = factory.createMIStackListFrames(0, upperBound);
				mi.postCommand(frames);
				MIStackListFramesInfo info = frames.getMIStackListFramesInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
				MIFrame[] miFrames = info.getMIFrames();
				for (int i = 0; i < miFrames.length; i++) {
					currentFrames.add(new StackFrame(this, miFrames[i], depth - miFrames[i].getLevel()));
				}
			} catch (MIException e) {
				//throw new CDIException(e.getMessage());
				//System.out.println(e);
			} catch (CDIException e) {
				//throw e;
				//System.out.println(e);
			} finally {
				target.setCurrentThread(currentThread, false);
			}
			// take time to assign the currentFrame, if it is in the set
			if (currentFrame == null) {
				for (int i = 0; i < currentFrames.size(); i++) {
					StackFrame f = (StackFrame) currentFrames.get(i);
					if (f.getMIFrame().getLevel() == 0) {
						currentFrame =f;
					}
				}
			}
		}
		List list = ((high - low + 1) <= currentFrames.size()) ? currentFrames.subList(low, high + 1) : currentFrames;
		return (ICDIStackFrame[])list.toArray(noStack);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#setCurrentStackFrame(ICDIStackFrame, boolean)
	 */
	public void setCurrentStackFrame(StackFrame stackframe, boolean doUpdate) throws CDIException {
		
		// Assert we should assert that the stackframe is one of our frames.

		int frameLevel = 0;
		if (stackframe != null) {
			frameLevel = stackframe.getLevel();
		}

		// Check to see if we are already at this level
		if (currentFrame != null && currentFrame.getLevel() == frameLevel) {
			if (stackframe != null) {
				Thread aThread = (Thread)stackframe.getThread();
				if (aThread != null && aThread.getId() == getId()) {
					// noop
					return;
				}
			}
		}

		try {
			Target target = (Target)getTarget();
			MISession mi = target.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			// Need the GDB/MI view of level which is the reverse, i.e. the highest level is 0
			// See comment in StackFrame constructor.
			int miLevel = getStackFrameCount() - frameLevel;
			MIStackSelectFrame frame = factory.createMIStackSelectFrame(miLevel);
			// Set ourself as the current thread first.
			target.setCurrentThread(this, doUpdate);
			mi.postCommand(frame);
			MIInfo info = frame.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			currentFrame = stackframe;
			// Resetting stackframe may change the value of
			// some variables like registers. Call an update()
			// To generate changeEvents.
			if (doUpdate) {
				Session session = (Session) target.getSession();
				RegisterManager regMgr = session.getRegisterManager();
				if (regMgr.isAutoUpdate()) {
					regMgr.update(target);
				}
				VariableManager varMgr = session.getVariableManager();
				if (varMgr.isAutoUpdate()) {
					varMgr.update(target);
				}
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepInto()
	 */
	public void stepInto() throws CDIException {
		stepInto(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepInto(int)
	 */
	public void stepInto(int count) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepInto(count);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepIntoInstruction()
	 */
	public void stepIntoInstruction() throws CDIException {
		stepIntoInstruction(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepIntoInstruction(int)
	 */
	public void stepIntoInstruction(int count) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepIntoInstruction(count);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepOver()
	 */
	public void stepOver() throws CDIException {
		stepOver(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepOver(int)
	 */
	public void stepOver(int count) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepOver(count);
		
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepOverInstruction()
	 */
	public void stepOverInstruction() throws CDIException {
		stepOverInstruction(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepOverInstruction(int)
	 */
	public void stepOverInstruction(int count) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepOverInstruction(count);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepReturn()
	 */
	public void stepReturn() throws CDIException {
		getCurrentStackFrame().stepReturn();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#runUntil(ICDILocation)
	 */
	public void runUntil(ICDILocation location) throws CDIException {
		stepUntil(location);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepUntil(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void stepUntil(ICDILocation location) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepUntil(location);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#isSuspended()
	 */
	public boolean isSuspended() {
		return getTarget().isSuspended();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#suspend()
	 */
	public void suspend() throws CDIException {
		getTarget().suspend();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#resume()
	 */
	public void resume() throws CDIException {
		resume(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(boolean)
	 */

	public void resume(boolean passSignal) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().resume(passSignal);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void resume(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub
		((Target)getTarget()).setCurrentThread(this);
		getTarget().resume(location);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
	 */
	public void resume(ICDISignal signal) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().resume(signal);
		
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#jump(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void jump(ICDILocation location) throws CDIException {
		resume(location);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#signal()
	 */
	public void signal() throws CDIException {
		resume(false);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#signal(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
	 */
	public void signal(ICDISignal signal) throws CDIException {
		resume(signal);
	}


	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#equals(ICDIThread)
	 */
	public boolean equals(ICDIThread thread) {
		if (thread instanceof Thread) {
			Thread cthread = (Thread) thread;
			return id == cthread.getId();
		}
		return super.equals(thread);
	}

	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		Target target = (Target)getTarget();
		ICDIBreakpoint[] bps = target.getBreakpoints();
		ArrayList list = new ArrayList(bps.length);
		for (int i = 0; i < bps.length; i++) {
			ICDICondition condition = bps[i].getCondition();
			if (condition == null) {
				continue;
			}
			String[] threadIds = condition.getThreadIds();
			for (int j = 0; j < threadIds.length; j++) {
				int tid = 0;
				try {
					tid = Integer.parseInt(threadIds[j]);
				} catch (NumberFormatException e) {
					//
				}
				if (tid == getId()) {
					list.add(bps[i]);
				}
			}
		}
		return (ICDIBreakpoint[]) list.toArray(new ICDIBreakpoint[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getThreadStorageDescriptors()
	 */
	public ICDIThreadStorageDescriptor[] getThreadStorageDescriptors() throws CDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager varMgr = session.getVariableManager();
		return varMgr.getThreadStorageDescriptors(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#createThreadStorage(org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorageDescriptor)
	 */
	public ICDIThreadStorage createThreadStorage(ICDIThreadStorageDescriptor varDesc) throws CDIException {
		if (varDesc instanceof ThreadStorageDescriptor) {
			Session session = (Session)getTarget().getSession();
			VariableManager varMgr = session.getVariableManager();
			return varMgr.createThreadStorage((ThreadStorageDescriptor)varDesc);
		}
		return null;
	}

}
