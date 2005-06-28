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

	protected Variable variable;

	public Value(Variable v) {
		super((Target)v.getTarget());
		variable = v;
	}

	protected Variable getVariable() {
		return variable;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		return variable.getTypeName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getValueString()
	 */
	public String getValueString() throws CDIException {
		String result = ""; //$NON-NLS-1$
		MISession mi = ((Target)getTarget()).getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarEvaluateExpression var =
			factory.createMIVarEvaluateExpression(variable.getMIVar().getVarName());
		try {
			mi.postCommand(var);
			MIVarEvaluateExpressionInfo info = var.getMIVarEvaluateExpressionInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			result = info.getValue();
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
		return result;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getVariables()
	 */	
	public int getChildrenNumber() throws CDIException {
		return variable.getMIVar().getNumChild();
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
		return variable.getChildren();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getType()
	 */
	public ICDIType getType() throws CDIException {
		return variable.getType();
	}

}
