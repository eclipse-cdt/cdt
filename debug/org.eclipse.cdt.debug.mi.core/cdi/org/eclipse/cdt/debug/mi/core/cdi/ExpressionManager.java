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
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Expression;
import org.eclipse.cdt.debug.mi.core.cdi.model.LocalVariable;
import org.eclipse.cdt.debug.mi.core.cdi.model.StackFrame;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.cdi.model.Thread;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.command.MIVarDelete;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarChange;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;

/**
 */
public class ExpressionManager extends Manager {

	final static ICDIExpression[] EMPTY_EXPRESSIONS = {};
	Map expMap;
	Map varMap;
	MIVarChange[] noChanges = new MIVarChange[0];

	public ExpressionManager(Session session) {
		super(session, true);
		expMap = new Hashtable();
		varMap = new Hashtable();
	}

	synchronized List getExpressionList(Target target) {
		List expList = (List)expMap.get(target);
		if (expList == null) {
			expList = Collections.synchronizedList(new ArrayList());
			expMap.put(target, expList);
		}
		return expList;
	}

	synchronized List getVariableList(Target target) {
		List varList = (List)varMap.get(target);
		if (varList == null) {
			varList = Collections.synchronizedList(new ArrayList());
			varMap.put(target, varList);
		}
		return varList;
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#createExpression(String)
	 */
	public ICDIExpression createExpression(Target target, String name) throws CDIException {
		Expression expression = new Expression(target, name);
		List exprList = getExpressionList(target);
		exprList.add(expression);
		return expression;
	}

	public ICDIExpression[] getExpressions(Target target) throws CDIException {
		List expList = (List) expMap.get(target);
		if (expList != null) {
			return (ICDIExpression[])expList.toArray(EMPTY_EXPRESSIONS);
		}
		return EMPTY_EXPRESSIONS;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#removeExpression(ICDIExpression)
	 */
	public void destroyExpressions(Target target, ICDIExpression[] expressions) throws CDIException {
		List expList = getExpressionList(target);
		for (int i = 0; i < expressions.length; ++i) {
			expList.remove(expressions[i]);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#removeExpression(ICDIExpression)
	 */
	public void destroyAllExpressions(Target target) throws CDIException {
		ICDIExpression[] expressions = getExpressions(target);
		destroyExpressions(target, expressions);
	}

	public void update(Target target) throws CDIException {
		deleteAllVariables(target);
	}

	/**
	 * @param miSession
	 * @param varName
	 * @return
	 */
	public Variable getVariable(MISession miSession, String varName) {
		Session session = (Session)getSession();
		Target target = session.getTarget(miSession);
		List varList = getVariableList(target);
		Variable[] vars = (Variable[])varList.toArray(new Variable[0]);
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getMIVar().getVarName().equals(varName)) {
				return vars[i];
			}
		}
		return null;
	}

	public Variable createVariable(StackFrame frame, String code) throws CDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
		try {
			MISession mi = target.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIVarCreate var = factory.createMIVarCreate(code);
			mi.postCommand(var);
			MIVarCreateInfo info = var.getMIVarCreateInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			Variable variable = new LocalVariable(target, null, frame, code, null, 0, 0, info.getMIVar());
			List varList = getVariableList(target);
			varList.add(variable);
			return variable;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}

	public void deleteAllVariables(Target target) throws CDIException {
		List varList = getVariableList(target);
		Variable[] variables = (Variable[]) varList.toArray(new Variable[varList.size()]);
		for (int i = 0; i < variables.length; ++i) {
			deleteVariable(variables[i]);
		}
	}
	/**
	 * Get rid of the underlying variable.
	 */
	public void deleteVariable(Variable variable) throws CDIException {
		Target target = (Target)variable.getTarget();
		MISession miSession = target.getMISession();
		MIVar miVar = variable.getMIVar();
		//remove the underlying var-object now.
		CommandFactory factory = miSession.getCommandFactory();
		MIVarDelete var = factory.createMIVarDelete(miVar.getVarName());
		try {
			miSession.postCommand(var);
			var.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

}
