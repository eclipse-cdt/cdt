/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
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
	ICDIStackFrame currentFrame;
	int stackdepth = 0;
	
	public Thread(ICDITarget target, int threadId) {
		super(target);
		id = threadId;
	}
	
	public int getId() {
		return id;
	}

	public void clearState() {
		stackdepth = 0;
		currentFrame = null;
	}

	public String toString() {
		return Integer.toString(id);
	}

	public void updateState() {
		try {
			getCurrentStackFrame();
		} catch (CDIException e) {
		}
	}

	public ICDIStackFrame getCurrentStackFrame() throws CDIException {
		if (currentFrame == null) {
			ICDIStackFrame[] frames = getStackFrames(0, 0);
			if (frames.length > 0) {
				currentFrame = frames[0];
			}
		}
		return currentFrame;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getStackFrames()
	 */
	public ICDIStackFrame[] getStackFrames() throws CDIException {
		int depth = 0;
		ICDIStackFrame[] stacks = noStack;
		Session session = (Session)getTarget().getSession();
		Target currentTarget = (Target)session.getCurrentTarget();
		ICDIThread currentThread = currentTarget.getCurrentThread();
		currentTarget.setCurrentThread(this, false);
		try {
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIStackListFrames frames = factory.createMIStackListFrames();
			depth = getStackFrameCount();
			mi.postCommand(frames);
			MIStackListFramesInfo info = frames.getMIStackListFramesInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			MIFrame[] miFrames = info.getMIFrames();
			stacks = new StackFrame[miFrames.length];
			for (int i = 0; i < stacks.length; i++) {
				stacks[i] = new StackFrame(this, miFrames[i], depth - miFrames[i].getLevel());
			}
		} catch (MIException e) {
			//throw new CDIException(e.getMessage());
			//System.out.println(e);
		} catch (CDIException e) {
			//throw e;
			//System.out.println(e);
		} finally {
			currentTarget.setCurrentThread(currentThread, false);
		}
		if (currentFrame == null) {
			for (int i = 0; i < stacks.length; i++) {
				if (stacks[i].getLevel() == depth) {
					currentFrame = stacks[i];
				}
			}
		}
		return stacks;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getStackFrames()
	 */
	public int getStackFrameCount() throws CDIException {
		if (stackdepth == 0) {
			Session session = (Session)(getTarget().getSession());
			Target currentTarget = (Target)session.getCurrentTarget();
			ICDIThread currentThread = currentTarget.getCurrentThread();
			currentTarget.setCurrentThread(this, false);
			try {
				MISession mi = session.getMISession();
				CommandFactory factory = mi.getCommandFactory();
				MIStackInfoDepth depth = factory.createMIStackInfoDepth();
				mi.postCommand(depth);
				MIStackInfoDepthInfo info = depth.getMIStackInfoDepthInfo();
				if (info == null) {
					throw new CDIException("No answer");
				}
				stackdepth = info.getDepth();
			} catch (MIException e) {
				throw new MI2CDIException(e);
				//System.out.println(e);
			} finally {
				currentTarget.setCurrentThread(currentThread, false);
			}
		}
		return stackdepth;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getStackFrames()
	 */
	public ICDIStackFrame[] getStackFrames(int low, int high) throws CDIException {
		ICDIStackFrame[] stacks = noStack;
		Session session = (Session)getTarget().getSession();
		Target currentTarget = (Target)session.getCurrentTarget();
		ICDIThread currentThread = currentTarget.getCurrentThread();
		currentTarget.setCurrentThread(this, false);
		try {
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIStackListFrames frames = factory.createMIStackListFrames(low, high);
			int depth = getStackFrameCount();
			mi.postCommand(frames);
			MIStackListFramesInfo info = frames.getMIStackListFramesInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			MIFrame[] miFrames = info.getMIFrames();
			stacks = new StackFrame[miFrames.length];
			for (int i = 0; i < stacks.length; i++) {
				stacks[i] = new StackFrame(this, miFrames[i], depth - miFrames[i].getLevel());
			}
		} catch (MIException e) {
			//throw new CDIException(e.getMessage());
			//System.out.println(e);
		} catch (CDIException e) {
			//throw e;
			//System.out.println(e);
		} finally {
			currentTarget.setCurrentThread(currentThread, false);
		}
		if (currentFrame == null) {
			for (int i = 0; i < stacks.length; i++) {
				StackFrame f = (StackFrame)stacks[i];
				if (f.getMIFrame().getLevel() == 0) {
					currentFrame = stacks[i];
				}
			}
		}
		return stacks;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#setCurrentStackFrame(ICDIStackFrame)
	 */
	public void setCurrentStackFrame(ICDIStackFrame stackframe) throws CDIException {
		setCurrentStackFrame(stackframe, true);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#setCurrentStackFrame(ICDIStackFrame, boolean)
	 */
	public void setCurrentStackFrame(ICDIStackFrame stackframe, boolean doUpdate) throws CDIException {
		int frameLevel = 0;
		if (stackframe != null) {
			frameLevel = stackframe.getLevel();
		}
		
		// Check to see if we are already at this level
		ICDIStackFrame current = getCurrentStackFrame();
		if (current != null && current.getLevel() == frameLevel) {
			// noop
			return;
		}

		try {
			Session session = (Session)getTarget().getSession();
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			// Need the GDB/MI view of level which is the reverse, i.e. the highest level is 0
			// See comment in StackFrame constructor.
			int miLevel = getStackFrameCount() - frameLevel;		
			MIStackSelectFrame frame = factory.createMIStackSelectFrame(miLevel);
			// Set ourself as the current thread first.
			((Target)getTarget()).setCurrentThread(this, doUpdate);
			mi.postCommand(frame);
			MIInfo info = frame.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			currentFrame = stackframe;
			// Resetting stackframe may change the value of
			// some variables like registers.  Call an update()
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
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#isSuspended()
	 */
	public boolean isSuspended() {
		return getTarget().isSuspended();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#resume()
	 */
	public void resume() throws CDIException {
		getTarget().setCurrentThread(this);
		getTarget().resume();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepInto()
	 */
	public void stepInto() throws CDIException {
		getTarget().setCurrentThread(this);
		getTarget().stepInto();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepIntoInstruction()
	 */
	public void stepIntoInstruction() throws CDIException {
		getTarget().setCurrentThread(this);
		getTarget().stepIntoInstruction();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepOver()
	 */
	public void stepOver() throws CDIException {
		getTarget().setCurrentThread(this);
		getTarget().stepOver();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepOverInstruction()
	 */
	public void stepOverInstruction() throws CDIException {
		getTarget().setCurrentThread(this);
		getTarget().stepOverInstruction();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepReturn()
	 */
	public void stepReturn() throws CDIException {
		getTarget().setCurrentThread(this);
		getTarget().stepReturn();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepReturn(boolean)
	 */
	public void stepReturn(boolean execute) throws CDIException {
		getTarget().setCurrentThread(this);
		getTarget().stepReturn(execute);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#runUntil(ICDILocation)
	 */
	public void runUntil(ICDILocation location) throws CDIException {
		getTarget().setCurrentThread(this);
		getTarget().runUntil(location);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#suspend()
	 */
	public void suspend() throws CDIException {
		getTarget().suspend();
		getTarget().setCurrentThread(this);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#jump(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void jump(ICDILocation location) throws CDIException {
		getTarget().setCurrentThread(this);
		getTarget().jump(location);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#signal()
	 */
	public void signal() throws CDIException {
		getTarget().setCurrentThread(this);
		getTarget().signal();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#signal(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
	 */
	public void signal(ICDISignal signal) throws CDIException {
		getTarget().setCurrentThread(this);
		getTarget().signal(signal);
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

}
