/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.Location;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;

/**
 */
public class StackFrame extends CObject implements ICDIStackFrame {

	MIFrame frame;
	Thread cthread;

	public StackFrame(Thread thread, MIFrame f) {
		super(thread.getTarget());
		cthread = thread;
		frame = f;
	}

	MIFrame getMIFrame() {
		return frame;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getThread()
	 */
	public ICDIThread getThread() {
		return cthread;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getArguments()
	 */
	public ICDIArgument[] getArguments() throws CDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = (VariableManager)session.getVariableManager();
		ICDIArgumentObject[] argObjs = mgr.getArgumentObjects(this);
		ICDIArgument[] args = new ICDIArgument[argObjs.length];
		for (int i = 0; i < args.length; i++) {
			args[i] = mgr.createArgument(argObjs[i]);
		}
		return args;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocalVariables()
	 */
	public ICDIVariable[] getLocalVariables() throws CDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = (VariableManager)session.getVariableManager();
		ICDIVariableObject[] varObjs = mgr.getVariableObjects(this);
		ICDIVariable[] vars = new ICDIVariable[varObjs.length];
		for (int i = 0; i < vars.length; i++) {
			vars[i] = mgr.createVariable(varObjs[i]);
		}
		return vars;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocation()
	 */
	public ICDILocation getLocation() {
		if (frame != null) {
			return new Location(frame.getFile(), frame.getFunction(),
					frame.getLine(), frame.getAddress());
		}
		return new Location("", "", 0, 0);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLevel()
	 */
	public int getLevel() {
		if (frame != null) {
			return frame.getLevel();
		}
		return 0;
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#equals(ICDIStackFrame)
	 */
	public boolean equals(ICDIStackFrame stackframe) {
		if (stackframe instanceof StackFrame) {
			StackFrame stack = (StackFrame)stackframe;
			return  cthread != null &&
				cthread.equals(stack.getThread()) &&
				frame != null &&
				frame.getLevel() == stack.getMIFrame().getLevel() &&
				frame.getFile().equals(stack.getMIFrame().getFile()) &&
				frame.getFunction().equals(stack.getMIFrame().getFunction());
		}
		return super.equals(stackframe);
	}

}
