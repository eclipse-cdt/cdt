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
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarUpdate;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarDeletedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIVarChange;
import org.eclipse.cdt.debug.mi.core.output.MIVarUpdateInfo;

/**
 */
public class ExpressionManager extends Manager {

	final static ICDIExpression[] EMPTY_EXPRESSIONS = {};
	Map expMap;
	MIVarChange[] noChanges = new MIVarChange[0];

	public ExpressionManager(Session session) {
		super(session, true);
		expMap = new Hashtable();
	}

	synchronized List getExpressionList(Target target) {
		List expList = (List)expMap.get(target);
		if (expList == null) {
			expList = Collections.synchronizedList(new ArrayList());
			expMap.put(target, expList);
		}
		return expList;
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
			if (expressions[i] instanceof Expression) {
				expList.remove(expressions[i]);
				((Expression)expressions[i]).deleteVariable();
			}
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
		List eventList = new ArrayList();
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		List expList = getExpressionList(target);
		Expression[] exps = (Expression[])expList.toArray(new Expression[0]);
		for (int i = 0; i < exps.length; i++) {
			Variable variable = exps[i].getVariable();
			if (variable != null) {
				String varName = variable.getMIVar().getVarName();
				MIVarChange[] changes = noChanges;
				MIVarUpdate update = factory.createMIVarUpdate(varName);
				try {
					mi.postCommand(update);
					MIVarUpdateInfo info = update.getMIVarUpdateInfo();
					if (info == null) {
						throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
					}
					changes = info.getMIVarChanges();
				} catch (MIException e) {
					//throw new MI2CDIException(e);
					eventList.add(new MIVarDeletedEvent(mi, varName));
				}
				for (int j = 0 ; j < changes.length; j++) {
					String n = changes[j].getVarName();
					if (changes[j].isInScope()) {
						eventList.add(new MIVarChangedEvent(mi, n));
					}
				}
			}
		}
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);
	}

	/**
	 * @param miSession
	 * @param varName
	 * @return
	 */
	public Expression getExpression(MISession miSession, String varName) {
		Session session = (Session)getSession();
		Target target = session.getTarget(miSession);
		List expList = getExpressionList(target);
		Expression[] exps = (Expression[])expList.toArray(new Expression[0]);
		for (int i = 0; i < exps.length; i++) {
			Variable variable = exps[i].getVariable();
			if (variable != null) {
				if (variable.getMIVar().getVarName().equals(varName)) {
					return exps[i];
				}
			}
		}
		return null;
	}

}
