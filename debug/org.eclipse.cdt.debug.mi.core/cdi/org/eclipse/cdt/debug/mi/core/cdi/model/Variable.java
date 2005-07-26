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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIBoolType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIEnumType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFunctionType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongLongType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIShortType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIWCharType;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;
import org.eclipse.cdt.debug.mi.core.cdi.ExpressionManager;
import org.eclipse.cdt.debug.mi.core.cdi.Format;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.ArrayValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.BoolValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.CharValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.DoubleValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.EnumValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.FloatValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.FunctionValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.IntValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.LongLongValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.LongValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.PointerValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.ReferenceValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.ShortValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.StructValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.WCharValue;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarAssign;
import org.eclipse.cdt.debug.mi.core.command.MIVarInfoExpression;
import org.eclipse.cdt.debug.mi.core.command.MIVarInfoType;
import org.eclipse.cdt.debug.mi.core.command.MIVarListChildren;
import org.eclipse.cdt.debug.mi.core.command.MIVarSetFormat;
import org.eclipse.cdt.debug.mi.core.command.MIVarShowAttributes;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarInfoExpressionInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarInfoTypeInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarListChildrenInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarShowAttributesInfo;

/**
 */
public abstract class Variable extends VariableDescriptor implements ICDIVariable {

	MIVar fMiVar;
	Value value;
	public ICDIVariable[] children = new ICDIVariable[0];
	String editable = null;
	String language;
	boolean isFake = false;
	boolean isUpdated = true;

	public Variable(VariableDescriptor obj, MIVar v) {
		super(obj);
		fMiVar = v;
	}

	public Variable(Target target, Thread thread, StackFrame frame, String n, String q, int pos, int depth, MIVar v) {
		super(target, thread, frame, n, q, pos, depth);
		fMiVar = v;
	}

	public void setUpdated(boolean update) {
		isUpdated = update;
	}

	public boolean isUpdated() {
		return isUpdated;
	}

	public void update() throws CDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		mgr.update(this);
	}

	public MIVar getMIVar() {
		return fMiVar;
	}

	public Variable getChild(String name) {
		for (int i = 0; i < children.length; i++) {
			Variable variable = (Variable) children[i];
			if (name.equals(variable.getMIVar().getVarName())) {
				return variable;
			}
			// Look also in the grandchildren.
			Variable grandChild = variable.getChild(name);
			if (grandChild != null) {
				return grandChild;
			}
		}
		return null;
	}

	String getLanguage() throws CDIException {
		if (language == null) {
			MISession mi = ((Target)getTarget()).getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIVarInfoExpression var = factory.createMIVarInfoExpression(getMIVar().getVarName());
			try {
				mi.postCommand(var);
				MIVarInfoExpressionInfo info = var.getMIVarInfoExpressionInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
				language = info.getLanguage();
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		}
		return (language == null) ? "" : language; //$NON-NLS-1$
	}

	boolean isCPPLanguage() throws CDIException {
		return getLanguage().equalsIgnoreCase("C++"); //$NON-NLS-1$
	}

	void setIsFake(boolean f) {
		isFake = f;
	}

	boolean isFake() {
		return isFake;
	}

	public ICDIVariable[] getChildren() throws CDIException {
		// Use the default timeout.
		return getChildren(-1);
	}

	/**
	 * This can be a potentially long operation for GDB.
	 * allow the override of the timeout.
	 */
	public ICDIVariable[] getChildren(int timeout) throws CDIException {
		MISession mi = ((Target)getTarget()).getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarListChildren var = factory.createMIVarListChildren(getMIVar().getVarName());
		try {
			if (timeout >= 0) {
				mi.postCommand(var, timeout);
			} else {
				mi.postCommand(var);
			}
			MIVarListChildrenInfo info = var.getMIVarListChildrenInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			MIVar[] vars = info.getMIVars();
			children = new Variable[vars.length];
			for (int i = 0; i < vars.length; i++) {
				String fn= getFullName();
				String childName = vars[i].getExp();
				ICDIType childType = null;
				boolean childFake = false;
				ICDIType t = getType();
				if (t instanceof ICDIArrayType) {
					fn = "(" + fn + ")[" + i + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					// For Array gdb varobj only return the index, override here.
					int index = castingIndex + i;
					childName = getName() + "[" + index + "]"; //$NON-NLS-1$ //$NON-NLS-2$
				} else if (t instanceof ICDIPointerType) {
					ICDIType subType = ((ICDIPointerType)t).getComponentType();
					if (subType instanceof ICDIStructType) {
						if (isCPPLanguage()) {
							if (!isFake()
									|| (isFake() && !(fName.equals("private") || fName.equals("public") || fName.equals("protected")))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								childFake = true;
								childType = t;
							} else {
								fn = "(" + fn + ")->" + vars[i].getExp(); //$NON-NLS-1$ //$NON-NLS-2$
							}
						} else { // If not C++ language
							fn = "(" + fn + ")->" + vars[i].getExp(); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else {
						fn = "*(" + fn + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else if (t instanceof ICDIReferenceType) {
					ICDIType subType = ((ICDIReferenceType)t).getComponentType();
					if (subType instanceof ICDIStructType) {
						if (isCPPLanguage()) {
							if (!isFake()
									|| (isFake() && !(fName.equals("private") || fName.equals("public") || fName.equals("protected")))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								childFake = true;
								childType = t;
							} else {
								fn = "(" + fn + ")." + vars[i].getExp(); //$NON-NLS-1$ //$NON-NLS-2$
							}
						} else { // If not C++ language
							fn = "(" + fn + ")." + vars[i].getExp(); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else {
						fn = "(" + fn + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else if (t instanceof ICDIStructType) {
					if (isCPPLanguage()) {
						// For C++ in GDB the children of the
						// the struture are the scope and the inherited classes.
						// For example:
						// class foo: public bar {
						//   int x;
						//   public: int y;
						// } foobar;
						// This will map to
						// - foobar
						//    + bar
						//    - private
						//       - x
						//    - public
						//       - y
						// So we choose to ignore the first set of children
						// but carry over to those "fake" variables the typename and the qualified name
						if (!isFake()
							|| (isFake() && !(fName.equals("private") || fName.equals("public") || fName.equals("protected")))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							childFake = true;
							childType = t;
						} else {
							fn = "(" + fn + ")." + vars[i].getExp(); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else { // If not C++ language
						fn = "(" + fn + ")." + vars[i].getExp(); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				Variable v = createVariable((Target)getTarget(), (Thread)getThread(), (StackFrame)getStackFrame(),
						childName, fn, getPosition(), getStackDepth(), vars[i]);
				if (childType != null) {
					// Hack to reset the typename to a known value
					v.fType = childType;
				}
				v.setIsFake(childFake);
				children[i] = v;
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return children;
	}

	protected abstract Variable createVariable(Target target, Thread thread, StackFrame frame,
			String name, String fullName, int pos, int depth, MIVar miVar);
	
	public int getChildrenNumber() throws CDIException {
		return fMiVar.getNumChild();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getValue()
	 */
	public ICDIValue getValue() throws CDIException {
		if (value == null) {
			ICDIType t = getType();
			if (t instanceof ICDIBoolType) {
				value = new BoolValue(this);
			} else if (t instanceof ICDICharType) {
				value = new CharValue(this);
			} else if (t instanceof ICDIWCharType) {
				value = new WCharValue(this);
			} else if (t instanceof ICDIShortType) {
				value = new ShortValue(this);
			} else if (t instanceof ICDIIntType) {
				value = new IntValue(this);
			} else if (t instanceof ICDILongType) {
				value = new LongValue(this);
			} else if (t instanceof ICDILongLongType) {
				value = new LongLongValue(this);
			} else if (t instanceof ICDIEnumType) {
				value = new EnumValue(this);
			} else if (t instanceof ICDIFloatType) {
				value = new FloatValue(this);
			} else if (t instanceof ICDIDoubleType) {
				value = new DoubleValue(this);
			} else if (t instanceof ICDIFunctionType) {
				value = new FunctionValue(this);
			} else if (t instanceof ICDIPointerType) {
				value = new PointerValue(this);
			} else if (t instanceof ICDIReferenceType) {
				value = new ReferenceValue(this);
			} else if (t instanceof ICDIArrayType) {
				value = new ArrayValue(this);
			} else if (t instanceof ICDIStructType) {
				value = new StructValue(this);
			} else {
				value = new Value(this);
			}
		}
		return value;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(ICDIValue)
	 */
	public void setValue(ICDIValue value) throws CDIException {
		setValue(value.getValueString());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(String)
	 */
	public void setValue(String expression) throws CDIException {
		Target target = (Target)getTarget();
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIVarAssign var = factory.createMIVarAssign(fMiVar.getVarName(), expression);
		try {
			miSession.postCommand(var);
			MIInfo info = var.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}

		// If the assign was succesfull fire a MIVarChangedEvent() for the variable
		// Note GDB will not fire an event for the changed variable we have to do it manually.
		MIVarChangedEvent change = new MIVarChangedEvent(miSession, var.getToken(), fMiVar.getVarName());
		miSession.fireEvent(change);

		// Changing values may have side effects i.e. affecting other variables
		// if the manager is on autoupdate check all the other variables.
		// Note: This maybe very costly.
		// assigning may have side effects i.e. affecting other registers.

		// If register was on autoupdate, update all the other registers
		RegisterManager regMgr = ((Session)target.getSession()).getRegisterManager();
		if (regMgr.isAutoUpdate()) {
			regMgr.update(target);
		}
		
		// If expression was on autoupdate, update all the other expression
		ExpressionManager expMgr = ((Session)target.getSession()).getExpressionManager();
		if (expMgr.isAutoUpdate()) {
			expMgr.update(target);
		}
		
		// If variable was on autoupdate, update all the variables.
		VariableManager varMgr = ((Session)target.getSession()).getVariableManager();
		if (varMgr.isAutoUpdate()) {
			varMgr.update(target);
		}
	}

	/**
	 * Overload the implementation of VariableDescriptor and let gdb
	 * handle it.
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#isEditable()
	 */
	public boolean isEditable() throws CDIException {
		if (editable == null) {
			MISession mi = ((Target) getTarget()).getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIVarShowAttributes var = factory.createMIVarShowAttributes(fMiVar.getVarName());
			try {
				mi.postCommand(var);
				MIVarShowAttributesInfo info = var.getMIVarShowAttributesInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
				editable = String.valueOf(info.isEditable());
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		}
		return (editable == null) ? false : editable.equalsIgnoreCase("true"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setFormat()
	 */
	public void setFormat(int format) throws CDIException {
		int fmt = Format.toMIFormat(format);
		MISession mi = ((Target) getTarget()).getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarSetFormat var = factory.createMIVarSetFormat(fMiVar.getVarName(), fmt);
		try {
			mi.postCommand(var);
			MIInfo info = var.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#equals()
	 */
	public boolean equals(ICDIVariable var) {
		if (var instanceof Variable) {
			Variable variable = (Variable) var;
			return fMiVar.getVarName().equals(variable.getMIVar().getVarName());
		}
		return super.equals(var);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#dispose()
	 */
	public void dispose() throws CDIException {
		ICDITarget target = getTarget();
		VariableManager varMgr = ((Session)target.getSession()).getVariableManager();
		varMgr.destroyVariable(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		if (fTypename == null) {
			fTypename = fMiVar.getType();
			if (fTypename == null || fTypename.length() == 0) {
				MISession mi = ((Target) getTarget()).getMISession();
				CommandFactory factory = mi.getCommandFactory();
				MIVarInfoType infoType = factory.createMIVarInfoType(fMiVar.getVarName());
				try {
					mi.postCommand(infoType);
					MIVarInfoTypeInfo info = infoType.getMIVarInfoTypeInfo();
					if (info == null) {
						throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
					}
					fTypename = info.getType();
				} catch (MIException e) {
					throw new MI2CDIException(e);
				}
			}
		}
		return fTypename;
	}
}
