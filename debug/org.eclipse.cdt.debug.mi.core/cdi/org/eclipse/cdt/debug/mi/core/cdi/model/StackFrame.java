/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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
	int level;
	ICDIArgument[] args;
	ICDIVariable[] locals;

	/*
 	 * 
		GDB/MI does not keep the stack level, from what we expect.  In gdb, the
		highest stack is level 0 and lower stack as the highest level:
		-stack-list-frames
		^done,stack=[frame={level="0 ",addr="0x0804845b",func="main",file="hello.c",line="24"},
	 	            frame={level="1 ",addr="0x42017499",func="__libc_start_main",from="/lib/i686/libc.so.6"}]

		-stack-list-frames
		^done,stack=[frame={level="0 ",addr="0x08048556",func="main2",file="hello.c",line="58"},
	    	         frame={level="1 ",addr="0x08048501",func="main",file="hello.c",line="41"},
	        	     frame={level="2 ",addr="0x42017499",func="__libc_start_main",from="/lib/i686/libc.so.6"}]

		This is of no use to us since the level is always "0".  The level is necessary for example when
		doing recursive calls to make a distinction between frames.
		So in CDT this reverse the hidghest frame will have the highest number. In CDT:
		stack=[frame={level="2 ",addr="0x0804845b",func="main",file="hello.c",line="24"},
		       frame={level="1 ",addr="0x42017499",func="__libc_start_main",from="/lib/i686/libc.so.6"}]

		stack=[frame={level="3 ",addr="0x08048556",func="main2",file="hello.c",line="58"},
		       frame={level="2 ",addr="0x08048501",func="main",file="hello.c",line="41"},
	    	   frame={level="1 ",addr="0x42017499",func="__libc_start_main",from="/lib/i686/libc.so.6"}]
	*/
	public StackFrame(Thread thread, MIFrame f, int l) {
		super((Target)thread.getTarget());
		cthread = thread;
		frame = f;
		level = l;
	}

	public MIFrame getMIFrame() {
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
		if (args == null) {
			Session session = (Session)getTarget().getSession();
			VariableManager mgr = (VariableManager)session.getVariableManager();
			ICDIArgumentObject[] argObjs = mgr.getArgumentObjects(this);
			args = new ICDIArgument[argObjs.length];
			for (int i = 0; i < args.length; i++) {
				try {
					args[i] = mgr.createArgument(argObjs[i]);
				} catch (CDIException e) {
					args = null;
					throw e;
				}
			}
		}
		return args;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocalVariables()
	 */
	public ICDIVariable[] getLocalVariables() throws CDIException {
		if (locals == null) {
			Session session = (Session)getTarget().getSession();
			VariableManager mgr = (VariableManager)session.getVariableManager();
			ICDIVariableObject[] varObjs = mgr.getLocalVariableObjects(this);
			locals = new ICDIVariable[varObjs.length];
			for (int i = 0; i < locals.length; i++) {
				try {
					locals[i] = mgr.createVariable(varObjs[i]);
				} catch (CDIException e) {
					locals = null;
					throw e;
				}
			}
		}
		return locals;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocation()
	 */
	public ICDILocation getLocation() {
		if (frame != null) {
			return new Location(frame.getFile(), frame.getFunction(),
					frame.getLine(), frame.getAddress());
		}
		return new Location("", "", 0, 0); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLevel()
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#equals(ICDIStackFrame)
	 */
	public boolean equals(ICDIStackFrame stackframe) {
		if (stackframe instanceof StackFrame) {
			StackFrame stack = (StackFrame)stackframe;
			return  cthread != null &&
				cthread.equals(stack.getThread()) &&
				getLevel() == stack.getLevel() &&
				getLocation().equals(stack.getLocation());
		}
		return super.equals(stackframe);
	}

}
