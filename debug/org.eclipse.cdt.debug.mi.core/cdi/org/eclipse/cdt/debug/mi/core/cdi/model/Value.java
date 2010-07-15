/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
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
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.output.MIVarEvaluateExpressionInfo;

/**
 */
public class Value extends CObject implements ICDIValue {

	protected Variable fVariable;

	/**
	 * Indicates whether this Value object is for a C++ reference variable. If
	 * it is, then some decoding is needed on the value string we get from gdb,
	 * since it will contain two things: the address of the variable being
	 * referenced and the value.
	 * @since 6.0
	 */
	protected boolean fIsReference;

	public Value(Variable v) {
		super((Target)v.getTarget());
		fVariable = v;
	}

	protected Variable getVariable() throws CDIException {
		return fVariable;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		return getVariable().getTypeName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getValueString()
	 */
	public String getValueString() throws CDIException {
		// make sure the variable is updated.
		if (! getVariable().isUpdated()) {
			getVariable().update();
		}

		String result = ""; //$NON-NLS-1$
		MISession mi = ((Target)getTarget()).getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarEvaluateExpression var =
			factory.createMIVarEvaluateExpression(getVariable().getMIVar().getVarName());
		try {
			mi.postCommand(var);
			MIVarEvaluateExpressionInfo info = var.getMIVarEvaluateExpressionInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			result = info.getValue();
			
			// Reference variables get back a string with two things: the address of the 
			// variable being referenced and the value of the variable. The expected
			// format is, by example (for a float&):	"@0x22cc98: 3.19616001e-39"
			// We need to dig out the latter.
			if (fIsReference) {
				if (result.startsWith("@0x")) { //$NON-NLS-1$
					int index = result.indexOf(':');
					if (index > 0 && ((index + 1) < result.length())) {
						result = result.substring(index+1).trim();
					}
				}
			}
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
		return result;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getVariables()
	 */	
	public int getChildrenNumber() throws CDIException {
		return getVariable().getMIVar().getNumChild();
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getVariables()
	 */
	public boolean hasChildren() throws CDIException {
	/*
		int number = 0;
		MISession mi = getCTarget().getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarInfoNumChildren children = 
			factory.createMIVarInfoNumChildren(variable.getMIVar().getVarName());
		try {
			mi.postCommand(children);
			MIVarInfoNumChildrenInfo info = children.getMIVarInfoNumChildrenInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			number = info.getChildNumber();
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
		return (number > 0);
	*/
		return (getChildrenNumber() > 0);	
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getVariables()
	 */
	public ICDIVariable[] getVariables() throws CDIException {
		return getVariable().getChildren();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getType()
	 */
	public ICDIType getType() throws CDIException {
		return getVariable().getType();
	}

	/**
	 * Call this after construction with 'true' if the Value is for a reference
	 * variable. See {@link #fIsReference}.
	 * 
	 * Ideally, this property would be passed to the constructor. However
	 * introducing it that way at this point in time would cause a lot of churn
	 * in the codebase, since this class is not directly instantiated, and it
	 * has many subclasses.
	 * @since 6.0
	 */
	public void setIsReference(boolean isReference) {
		fIsReference = isReference;
	}

}
