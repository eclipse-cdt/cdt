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
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFunctionType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIVoidType;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SourceManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.IncompleteType;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;

/**
 */
public class VariableObject extends CObject implements ICDIVariableObject {

	// Casting info.
	String castingType;
	int castingIndex;
	int castingLength;

	String name;
	int position;
	ICDIStackFrame frame;
	int stackdepth;

	String qualifiedName = null;
	String fullName = null;
	ICDIType type = null;
	String typename = null;
	String sizeof = null;

	/**
	 * Copy constructor.
	 * @param obj
	 */
	public VariableObject(VariableObject obj) {
		super((Target)obj.getTarget());
		name = obj.getName();
		fullName = obj.fullName;
		sizeof = obj.sizeof;
		type = obj.type;
		try {
			frame = obj.getStackFrame();
		} catch (CDIException e) {
		}
		position = obj.getPosition();
		stackdepth = obj.getStackDepth();
		castingIndex = obj.getCastingArrayStart();
		castingLength = obj.getCastingArrayEnd();
		castingType = obj.getCastingType();
	}

	public VariableObject(Target target, String n, ICDIStackFrame stack, int pos, int depth) {
		this(target, n, null, stack, pos, depth);
	}

	public VariableObject(Target target, String n, String fn, ICDIStackFrame stack, int pos, int depth) {
		super(target);
		name = n;
		fullName = fn;
		frame = stack;
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

	public void setCastingType(String t) {
		castingType = t;
	}
	public String getCastingType() {
		return castingType;
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
			if (castingIndex != 0) {
				buffer.append('+').append(castingIndex);
			}
			buffer.append(')');
			buffer.append('@').append(castingLength);
			fn = buffer.toString();
		} else if (castingType != null && castingType.length() > 0) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("((").append(castingType).append(')'); //$NON-NLS-1$
			buffer.append(fn).append(')');
			fn = buffer.toString();
		}
		return fn;
	}

	public String getFullName() {
		if (fullName == null) {
			fullName = getName();
		}
		return fullName;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableObject#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getType()
	 */
	public ICDIType getType() throws CDIException {
		if (type == null) {
			Target target = (Target)getTarget();
			Session session = (Session) (target.getSession());
			SourceManager sourceMgr = (SourceManager) session.getSourceManager();
			String nametype = sourceMgr.getTypeName(target, getQualifiedName());
			try {
				type = sourceMgr.getType(target, nametype);
			} catch (CDIException e) {
				// Try with ptype.
				try {
					String ptype = sourceMgr.getDetailTypeName(target, nametype);
					type = sourceMgr.getType(target, ptype);
				} catch (CDIException ex) {
					// Some version of gdb does not work woth the name of the class
					// ex: class data foo --> ptype data --> fails
					// ex: class data foo --> ptype foo --> succeed
					try {
						String ptype = sourceMgr.getDetailTypeName(target, getQualifiedName());
						type = sourceMgr.getType(target, ptype);
					} catch (CDIException e2) {
						// give up.
					}
				}
			}
			if (type == null) {
				type = new IncompleteType(target, nametype);
			}
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#sizeof()
	 */
	public int sizeof() throws CDIException {
		if (sizeof == null) {
			Target target = (Target) getTarget();
			Session session = (Session) (target.getSession());
			MISession mi = target.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			String exp = "sizeof(" + getTypeName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			MIDataEvaluateExpression evaluate = factory.createMIDataEvaluateExpression(exp);
			try {
				mi.postCommand(evaluate);
				MIDataEvaluateExpressionInfo info = evaluate.getMIDataEvaluateExpressionInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.model.VariableObject.Target_not_responding")); //$NON-NLS-1$
				}
				sizeof = info.getExpression();
			} catch (MIException e) {
				throw new MI2CDIException(e);
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
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#isEdiTable()
	 */
	public boolean isEditable() throws CDIException {
		ICDIType t = getType();
		if (t instanceof ICDIArrayType
			|| t instanceof ICDIStructType
			|| t instanceof ICDIVoidType
			|| t instanceof ICDIFunctionType) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getStackFrame()
	 */
	public ICDIStackFrame getStackFrame() throws CDIException {
		return frame;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		if (typename == null) {
			ICDIType theType = getType();
			typename = theType.getTypeName();
		}
		return typename;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#hasChildren()
	 */
	public boolean hasChildren() throws CDIException {
		ICDIType t = getType();

		// For reference we need to get the referenced type
		// to make a decision.
		if (t instanceof ICDIReferenceType) {
			t = ((ICDIReferenceType) t).getComponentType();
		}

		if (t instanceof ICDIArrayType || t instanceof ICDIStructType) {
			return true;
		} else if (t instanceof ICDIPointerType) {
			ICDIType sub = ((ICDIPointerType) t).getComponentType();
			if (sub instanceof ICDIVoidType) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getQualifiedName()
	 */
	public String getQualifiedName() throws CDIException {
		if (qualifiedName == null) {
			qualifiedName = encodeVariable();
		}
		return qualifiedName;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#equals(ICDIVariableObject)
	 */
	public boolean equals(ICDIVariableObject varObj) {
		if (varObj instanceof VariableObject) {
			VariableObject var = (VariableObject) varObj;
			if (var.getName().equals(getName())
				&& var.getCastingArrayStart() == getCastingArrayStart()
				&& var.getCastingArrayEnd() == getCastingArrayEnd()
				&& ((var.getCastingType() == null && getCastingType() == null)
					|| (var.getCastingType() != null && getCastingType() != null && var.getCastingType().equals(getCastingType())))) {
				ICDIStackFrame varFrame = null;
				ICDIStackFrame ourFrame = null;
				try {
					varFrame = var.getStackFrame();
					ourFrame = getStackFrame();
				} catch (CDIException e) {
				}
				if (ourFrame == null && varFrame == null) {
					return true;
				} else if (varFrame != null && ourFrame != null && varFrame.equals(ourFrame)) {
					if (var.getStackDepth() == getStackDepth()) {
						if (var.getPosition() == getPosition()) {
							return true;
						}
					}
				}
			}
		}
		return super.equals(varObj);
	}
}
