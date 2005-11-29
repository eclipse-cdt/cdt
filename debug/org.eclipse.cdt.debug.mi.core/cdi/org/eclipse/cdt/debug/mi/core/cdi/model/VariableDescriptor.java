/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SourceManager;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.IncompleteType;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;

/**
 */
public abstract class VariableDescriptor extends CObject implements ICDIVariableDescriptor {

	// Casting info.
	String[] castingTypes;
	int castingIndex;
	int castingLength;

	String fName;
	int position;
	StackFrame fStackFrame;
	Thread fThread;
	int stackdepth;

	String qualifiedName = null;
	String fFullName = null;
	protected ICDIType fType = null;
	protected String fTypename = null;
	String sizeof = null;

	/**
	 * Copy constructor.
	 * @param desc
	 */
	public VariableDescriptor(VariableDescriptor desc) {
		super((Target)desc.getTarget());
		fName = desc.getName();
		fFullName = desc.fFullName;
		sizeof = desc.sizeof;
		fType = desc.fType;
		try {
			fStackFrame = (StackFrame)desc.getStackFrame();
			fThread = (Thread)desc.getThread();
		} catch (CDIException e) {
		}
		position = desc.getPosition();
		stackdepth = desc.getStackDepth();
		castingIndex = desc.getCastingArrayStart();
		castingLength = desc.getCastingArrayEnd();
		castingTypes = desc.getCastingTypes();
	}

	public VariableDescriptor(Target target, Thread thread, StackFrame stack, String n, String fn, int pos, int depth) {
		super(target);
		fName = n;
		fFullName = fn;
		fStackFrame = stack;
		fThread = thread;
		position = pos;
		stackdepth = depth;
	}

	public int getPosition() {
		return position;
	}

	public int getStackDepth() {
		return stackdepth;
	}

	public void setCastingArrayStart(int start) {
		castingIndex = start;
	}
	public int getCastingArrayStart() {
		return castingIndex;
	}

	public void setCastingArrayEnd(int end) {
		castingLength = end;
	}
	public int getCastingArrayEnd() {
		return castingLength;
	}

	public void setCastingTypes(String[] t) {
		castingTypes = t;
	}
	public String[] getCastingTypes() {
		return castingTypes;
	}

	/**
	 * If the variable was a cast encode the string appropriately for GDB.
	 * For example castin to an array is of 2 elements:
	 *  (foo)@2
	 * @return
	 */
	public String encodeVariable() {
		String fn = getFullName();
		if (castingLength > 0 || castingIndex > 0) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("*("); //$NON-NLS-1$
			buffer.append('(').append(fn).append(')');
			buffer.append('+').append(castingIndex).append(')');
			buffer.append('@').append(castingLength);
			fn = buffer.toString();
		} else if (castingTypes != null && castingTypes.length > 0) {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < castingTypes.length; ++i) {
				if (castingTypes[i] != null && castingTypes[i].length() > 0) {
					if (buffer.length() == 0) {
						buffer.append('(').append(castingTypes[i]).append(')');
						buffer.append(fn);
					} else {
						buffer.insert(0, '(');
						buffer.append(')');
						StringBuffer b = new StringBuffer();
						b.append('(').append(castingTypes[i]).append(')');
						buffer.insert(0, b.toString());
					}
				}
			}
			fn = buffer.toString();
		}
		return fn;
	}

	public String getFullName() {
		if (fFullName == null) {
			fFullName = getName();
		}
		return fFullName;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableDescriptor#getName()
	 */
	public String getName() {
		return fName;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getType()
	 */
	public ICDIType getType() throws CDIException {
		if (fType == null) {
			String nametype = getTypeName();
			Target target = (Target)getTarget();
			Session session = (Session) target.getSession();
			SourceManager sourceMgr = session.getSourceManager();
			try {
				fType = sourceMgr.getType(target, nametype);
			} catch (CDIException e) {
				// Try with ptype.
				try {
					String ptype = sourceMgr.getDetailTypeName(target, nametype);
					fType = sourceMgr.getType(target, ptype);
				} catch (CDIException ex) {
					// Some version of gdb does not work on the name of the class
					// ex: class data foo --> ptype data --> fails
					// ex: class data foo --> ptype foo --> succeed
					StackFrame frame = (StackFrame)getStackFrame();
					if (frame == null) {
						Thread thread = (Thread)getThread();
						if (thread != null) {
							frame = thread.getCurrentStackFrame();
						} else {
							frame = ((Thread)target.getCurrentThread()).getCurrentStackFrame();
						}
					}
					try {
						String ptype = sourceMgr.getDetailTypeNameFromVariable(frame, getQualifiedName());
						fType = sourceMgr.getType(target, ptype);
					} catch (CDIException e2) {
						// give up.
					}
				}
			}
			if (fType == null) {
				fType = new IncompleteType(target, nametype);
			}
		}
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#sizeof()
	 */
	public int sizeof() throws CDIException {
		if (sizeof == null) {
			Target target = (Target) getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			StackFrame frame = (StackFrame)getStackFrame();
			Thread thread = (Thread)getThread();
			if (frame != null) {
				target.setCurrentThread(frame.getThread(), false);				
				((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
			} else if (thread != null) {
				target.setCurrentThread(thread, false);				
			}
			MISession mi = target.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			String exp = "sizeof(" + getTypeName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			MIDataEvaluateExpression evaluate = factory.createMIDataEvaluateExpression(exp);
			try {
				mi.postCommand(evaluate);
				MIDataEvaluateExpressionInfo info = evaluate.getMIDataEvaluateExpressionInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.model.VariableDescriptor.Target_not_responding")); //$NON-NLS-1$
				}
				sizeof = info.getExpression();
			} catch (MIException e) {
				throw new MI2CDIException(e);
			} finally {
				if (frame != null) {
					target.setCurrentThread(currentThread, false);
					currentThread.setCurrentStackFrame(currentFrame, false);
				} else if (thread != null) {
					target.setCurrentThread(currentThread, false);
				}
			}
		}

		if (sizeof != null) {
			try {
				return Integer.parseInt(sizeof);
			} catch (NumberFormatException e) {
				throw new CDIException(e.getMessage());
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getStackFrame()
	 */
	public ICDIStackFrame getStackFrame() throws CDIException {
		return fStackFrame;
	}

	public ICDIThread getThread() throws CDIException {
		return fThread;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		if (fTypename == null) {
			Target target = (Target)getTarget();
			StackFrame frame = (StackFrame)getStackFrame();
			if (frame == null) {
				Thread thread = (Thread)getThread();
				if (thread != null) {
					frame = thread.getCurrentStackFrame();
				} else {
					frame = ((Thread)target.getCurrentThread()).getCurrentStackFrame();
				}
			}
			Session session = (Session) target.getSession();
			SourceManager sourceMgr = session.getSourceManager();
			if (frame != null) {
				fTypename = sourceMgr.getTypeNameFromVariable(frame, getQualifiedName());
			} else {
				fTypename = sourceMgr.getTypeName(target, getQualifiedName());
			}
		}
		return fTypename;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getQualifiedName()
	 */
	public String getQualifiedName() throws CDIException {
		if (qualifiedName == null) {
			qualifiedName = encodeVariable();
		}
		return qualifiedName;
	}

	public static boolean equalsCasting(VariableDescriptor var1, VariableDescriptor var2) {
		String[] castings1 = var1.getCastingTypes();
		String[] castings2 = var2.getCastingTypes();
		if (castings1 == null && castings2 == null) {
			return true;
		} else if (castings1 != null && castings2 != null && castings1.length == castings2.length) {
			for (int i = 0; i < castings1.length; ++i) {
				if (!castings1[i].equals(castings2[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#equals(ICDIVariableDescriptor)
	 */
	public boolean equals(ICDIVariableDescriptor varDesc) {
		if (varDesc instanceof VariableDescriptor) {
			VariableDescriptor desc = (VariableDescriptor) varDesc;
			if (desc.getFullName().equals(getFullName())
				&& desc.getName().equals(getName()) // see bug #113364
				&& desc.getCastingArrayStart() == getCastingArrayStart()
				&& desc.getCastingArrayEnd() == getCastingArrayEnd()
				&& equalsCasting(desc, this)) {

				// Check the threads
				ICDIThread varThread = null;
				ICDIThread ourThread = null;
				try {
					varThread = desc.getThread();
					ourThread = getThread();
				} catch (CDIException e) {
					// ignore
				}
				if ((ourThread == null && varThread == null) ||
						(varThread != null && ourThread != null && varThread.equals(ourThread))) {
					// check the stackFrames
					ICDIStackFrame varFrame = null;
					ICDIStackFrame ourFrame = null;
					try {
						varFrame = desc.getStackFrame();
						ourFrame = getStackFrame();
					} catch (CDIException e) {
						// ignore
					}
					if (ourFrame == null && varFrame == null) {
						return true;
					} else if (varFrame != null && ourFrame != null && varFrame.equals(ourFrame)) {
						if (desc.getStackDepth() == getStackDepth()) {
							if (desc.getPosition() == getPosition()) {
								return true;
							}
						}
					}
				}
				return false;
			}
		}
		return super.equals(varDesc);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsArray(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor, int, int)
	 */
	public ICDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws CDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		return mgr.getVariableDescriptorAsArray(this, start, length);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsType(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor, java.lang.String)
	 */
	public ICDIVariableDescriptor getVariableDescriptorAsType(String type) throws CDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		return mgr.getVariableDescriptorAsType(this, type);
	}

//	/* (non-Javadoc)
//	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#createVariable()
//	 */
//	public ICDIVariable createVariable() throws CDIException {
//		Session session = (Session)getTarget().getSession();
//		VariableManager mgr = session.getVariableManager();
//		return mgr.createVariable(this);
//	}

}
