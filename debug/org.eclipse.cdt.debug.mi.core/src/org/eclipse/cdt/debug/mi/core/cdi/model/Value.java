/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.CSession;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.command.MIVarListChildren;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarEvaluateExpressionInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarListChildrenInfo;

/**
 */
public class Value extends CObject implements ICDIValue {

	Variable variable;

	public Value(Variable v) {
		super(v.getCTarget());
		variable = v;
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
		String result = "";
		MISession mi = getCTarget().getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarEvaluateExpression var =
			factory.createMIVarEvaluateExpression(variable.getMIVar().getVarName());
		try {
			mi.postCommand(var);
			MIVarEvaluateExpressionInfo info = var.getMIVarEvaluateExpressionInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			result = info.getValue();
		} catch (MIException e) {
			//throw new CDIException(e.getMessage());
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
		Variable[] variables = null;
		CSession session = getCTarget().getCSession();
		MISession mi = session.getMISession();
		VariableManager mgr = (VariableManager)session.getVariableManager();
		CommandFactory factory = mi.getCommandFactory();
		MIVarListChildren var = 
			factory.createMIVarListChildren(variable.getMIVar().getVarName());
		try {
			mi.postCommand(var);
			MIVarListChildrenInfo info = var.getMIVarListChildrenInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			MIVar[] vars = info.getMIVars();
			variables = new Variable[vars.length];
			for (int i = 0; i < vars.length; i++) {
				variables[i] = mgr.createVariable(variable.getStackFrame(),
						vars[i].getExp(), vars[i]);

			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return variables;
	}

}
