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

import java.math.BigInteger;
import java.util.HashMap;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;
import org.eclipse.cdt.debug.mi.core.cdi.Locator;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIExecFinish;
import org.eclipse.cdt.debug.mi.core.command.MIExecReturn;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;

/**
 */
public class StackFrame extends CObject implements ICDIStackFrame {

	MIFrame frame;
	Thread cthread;
	int level;
	ICDIArgumentDescriptor[] argDescs;
	ICDILocalVariableDescriptor[] localDescs;
	Locator fLocator;
	/**
	 * 
	 */
	private HashMap fTypeCache;

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
		fTypeCache = new HashMap();
	}

	public MIFrame getMIFrame() {
		return frame;
	}

	public ICDIType getFromTypeCache(String typeName) {
		return (ICDIType)fTypeCache.get(typeName);
	}
	public void addToTypeCache(String typeName, ICDIType typeDefinition) {
		fTypeCache.put(typeName, typeDefinition);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getThread()
	 */
	public ICDIThread getThread() {
		return cthread;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getArgumentDescriptors()
	 */
	public ICDIArgumentDescriptor[] getArgumentDescriptors() throws CDIException {
		if (argDescs == null) {
			Session session = (Session)getTarget().getSession();
			VariableManager mgr = session.getVariableManager();
			argDescs = mgr.getArgumentDescriptors(this);
		}
		return argDescs;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocalVariableDescriptors()
	 */
	public ICDILocalVariableDescriptor[] getLocalVariableDescriptors() throws CDIException {
		if (localDescs == null) {
			Session session = (Session)getTarget().getSession();
			VariableManager mgr = session.getVariableManager();
			localDescs = mgr.getLocalVariableDescriptors(this);
		}
		return localDescs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#createArgument(org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor)
	 */
	public ICDIArgument createArgument(ICDIArgumentDescriptor varDesc) throws CDIException {
		if (varDesc instanceof ArgumentDescriptor) {
			Session session = (Session)getTarget().getSession();
			VariableManager mgr = session.getVariableManager();
			return mgr.createArgument((ArgumentDescriptor)varDesc);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#createLocalVariable(org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor)
	 */
	public ICDILocalVariable createLocalVariable(ICDILocalVariableDescriptor varDesc) throws CDIException {
		if (varDesc instanceof ArgumentDescriptor) {
			return createArgument((ICDIArgumentDescriptor)varDesc);
		} else if (varDesc instanceof LocalVariableDescriptor) {
			Session session = (Session)getTarget().getSession();
			VariableManager mgr = session.getVariableManager();
			return mgr.createLocalVariable((LocalVariableDescriptor)varDesc);			
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocation()
	 */
	public ICDILocator getLocator() {
		BigInteger addr = BigInteger.ZERO;
		if (frame != null) {
			if (fLocator == null) {
				String a = frame.getAddress();
				if (a != null) {
					addr = MIFormat.getBigInteger(a);
				}
				fLocator = new Locator(frame.getFile(), 
					            frame.getFunction(),
					            frame.getLine(),  
								addr);
			}
			return fLocator;
		}
		return new Locator("", "", 0, addr); //$NON-NLS-1$ //$NON-NLS-2$
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
			boolean equal =  cthread != null &&
				cthread.equals(stack.getThread()) &&
				getLevel() == stack.getLevel();
			if (equal) {
				ICDILocator otherLocator = stack.getLocator();
				ICDILocator myLocator = getLocator();
				if (Locator.equalString(myLocator.getFile(), otherLocator.getFile())) {
					if (Locator.equalString(myLocator.getFunction(), otherLocator.getFunction())) {
						return true;
					}
				}
			}
		}
		return super.equals(stackframe);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStepReturn#stepReturn()
	 */
	public void stepReturn() throws CDIException {
		finish();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStepReturn#stepReturn(org.eclipse.cdt.debug.core.cdi.model.ICDIValue)
	 */
	public void stepReturn(ICDIValue value) throws CDIException {
		execReturn(value.toString());
	}

	/**
	 */
	protected void finish() throws CDIException {
		
		((Thread)getThread()).setCurrentStackFrame(this, false);

		Target target = (Target)getTarget();
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIExecFinish finish = factory.createMIExecFinish();
		try {
			miSession.postCommand(finish);
			MIInfo info = finish.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 */
	protected void execReturn(String value) throws CDIException {

		((Thread)getThread()).setCurrentStackFrame(this, false);

		Target target = (Target)getTarget();
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIExecReturn ret;
		if (value == null) {
			ret = factory.createMIExecReturn();
		} else {
			ret = factory.createMIExecReturn(value);
		}
		try {
			miSession.postCommand(ret);
			MIInfo info = ret.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.model.Target.Target_not_responding")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

}
