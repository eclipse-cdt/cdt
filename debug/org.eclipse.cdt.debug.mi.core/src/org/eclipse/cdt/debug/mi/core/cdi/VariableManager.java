/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIVariableManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.command.MIVarDelete;
import org.eclipse.cdt.debug.mi.core.output.MIChild;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class VariableManager
	extends SessionObject
	implements ICDIVariableManager {

	List expList;
	public VariableManager(CSession session) {
		super(session);
		expList = new ArrayList();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getExpression(String)
	 */
	public ICDIExpression getExpression(String expressionId)
		throws CDIException {
		ICDIExpression[] expressions = getExpressions();
		for (int i = 0; i < expressions.length; i++) {
			if (expressionId.equals(expressions[i].getName())) {
				return expressions[i];
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getExpressions()
	 */
	public ICDIExpression[] getExpressions() throws CDIException {
		return (ICDIExpression[]) expList.toArray(new ICDIExpression[0]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#removeExpression(ICDIExpression)
	 */
	public void removeExpression(ICDIExpression expression)
		throws CDIException {
		if (expression instanceof Expression) {
			expList.remove(expression);
			MISession mi = getCSession().getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIVarDelete var =
				factory.createMIVarDelete(
					((Expression) expression).getVarName());
			try {
				mi.postCommand(var);
			} catch (MIException e) {
				throw new CDIException(e.toString());
			}
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#removeExpressions(ICDIExpression[])
	 */
	public void removeExpressions(ICDIExpression[] expressions)
		throws CDIException {
		for (int i = 0; i < expressions.length; i++) {
			removeExpression(expressions[i]);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#createExpression(String)
	 */
	public ICDIExpression createExpression(String expressionId)
		throws CDIException {

		Expression cexp = null;

		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarCreate var = factory.createMIVarCreate(expressionId);
		try {
			mi.postCommand(var);
			MIVarCreateInfo info = var.getMIVarCreateInfo();
			MIChild child  = info.getMIChild();
			cexp = new Expression(getCSession().getCTarget(), expressionId, child);
			expList.add(cexp);
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
		return cexp;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#createCondition(int, String)
	 */
	public ICDICondition createCondition(int ignoreCount, String expression) {
		return new Condition(ignoreCount, expression);
	}

}
