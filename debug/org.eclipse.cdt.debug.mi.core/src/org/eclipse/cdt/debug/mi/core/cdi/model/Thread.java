/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
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

	static StackFrame[] noStack = new StackFrame[0];
	int id;
	StackFrame currentFrame;
	int stackdepth = 0;
	
	public Thread(ICDITarget target, int threadId) {
		super(target);
		id = threadId;
	}
	
	int getId() {
		return id;
	}

	public void clearState() {
		stackdepth = 0;
		currentFrame = null;
	}

	public String toString() {
		return Integer.toString(id);
	}

	public ICDIStackFrame getCurrentStackFrame() throws CDIException {
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

		StackFrame[] stacks = noStack;
		Session session = (Session)getTarget().getSession();
		Target currentTarget = (Target)session.getCurrentTarget();
		ICDIThread currentThread = currentTarget.getCurrentThread();
		currentTarget.setCurrentThread(this, false);
		try {
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIStackListFrames frames = factory.createMIStackListFrames();
			mi.postCommand(frames);
			MIStackListFramesInfo info = frames.getMIStackListFramesInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			MIFrame[] miFrames = info.getMIFrames();
			stacks = new StackFrame[miFrames.length];
			for (int i = 0; i < stacks.length; i++) {
				stacks[i] = new StackFrame(this, miFrames[i]);
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
				if (stacks[i].getLevel() == 0) {
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
		StackFrame[] stacks = noStack;
		Session session = (Session)getTarget().getSession();
		Target currentTarget = (Target)session.getCurrentTarget();
		ICDIThread currentThread = currentTarget.getCurrentThread();
		currentTarget.setCurrentThread(this, false);
		try {
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIStackListFrames frames = factory.createMIStackListFrames(low, high);
			mi.postCommand(frames);
			MIStackListFramesInfo info = frames.getMIStackListFramesInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			MIFrame[] miFrames = info.getMIFrames();
			stacks = new StackFrame[miFrames.length];
			for (int i = 0; i < stacks.length; i++) {
				stacks[i] = new StackFrame(this, miFrames[i]);
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
				if (stacks[i].getLevel() == 0) {
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
		if (stackframe instanceof  StackFrame) {
			setCurrentStackFrame((StackFrame)stackframe);
		} else {
			throw new CDIException("Unknown stackframe");
		}
	}

	public void setCurrentStackFrame(StackFrame stackframe) throws CDIException {
		setCurrentStackFrame(stackframe, true);
	}

	public void setCurrentStackFrame(StackFrame stackframe, boolean doUpdate) throws CDIException {
		int frameNum = 0;
		if (stackframe != null) {
			frameNum = stackframe.getLevel();
		}
		
		// Check to see if we are already at this level
		ICDIStackFrame current = getCurrentStackFrame();
		if (current != null && current.getLevel() == frameNum) {
			// noop
			return;
		}

		try {
			Session session = (Session)getTarget().getSession();
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();		
			MIStackSelectFrame frame = factory.createMIStackSelectFrame(frameNum);
			// Set ourself as the current thread first.
			((Target)getTarget()).setCurrentThread(this, doUpdate);
			mi.postCommand(frame);
			MIInfo info = frame.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			currentFrame = stackframe;
			// Resetting stackframe may change the value of
			// some variables like registers.  Send an update
			// To generate changeEvents.
			if (doUpdate) {
				RegisterManager regMgr = (RegisterManager)session.getRegisterManager();
				regMgr.update();
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#finish()
	 */
	public void finish() throws CDIException {
		getTarget().setCurrentThread(this);
		getTarget().finish();
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
		getTarget().setCurrentThread(this);
		getTarget().suspend();
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
