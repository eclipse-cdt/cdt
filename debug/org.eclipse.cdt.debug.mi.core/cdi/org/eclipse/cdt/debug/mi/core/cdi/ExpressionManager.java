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
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Expression;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;
import org.eclipse.cdt.debug.mi.core.cdi.model.VariableObject;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.command.MIVarDelete;
import org.eclipse.cdt.debug.mi.core.command.MIVarUpdate;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarDeletedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarChange;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarUpdateInfo;

/**
 */
public class ExpressionManager extends Manager implements ICDIExpressionManager{

	private List expList;
	MIVarChange[] noChanges = new MIVarChange[0];

	public ExpressionManager(Session session) {
		super(session, true);
		expList = Collections.synchronizedList(new ArrayList());
	}

	/**
	 * Tell gdb to remove the underlying var-object also.
	 */
	void removeMIVar(MIVar miVar) throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarDelete var = factory.createMIVarDelete(miVar.getVarName());
		try {
			mi.postCommand(var);
			var.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * When element are remove from the cache, they are put on the OutOfScope list, oos,
	 * because they are still needed for the destroy events.  The destroy event will
	 * call removeOutOfScope.
	 */
	public void removeExpression(String varName) throws CDIException {
		Expression[] exps = (Expression[])expList.toArray(new Expression[0]);
		for (int i = 0; i < exps.length; i++) {
			if (exps[i].getMIVar().getVarName().equals(varName)) {
				expList.remove(exps[i]);
				removeMIVar(exps[i].getMIVar());
			}
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#createExpression(String)
	 */
	public ICDIExpression createExpression(String name) throws CDIException {
		Expression expression = null;
		try {
			Session session = (Session)getSession();
			ICDITarget currentTarget = session.getCurrentTarget();
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIVarCreate var = factory.createMIVarCreate(name);
			mi.postCommand(var);
			MIVarCreateInfo info = var.getMIVarCreateInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			VariableObject varObj = new VariableObject(currentTarget, name, null, 0, 0);
			expression = new Expression(varObj, info.getMIVar());
			expList.add(expression);
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return expression;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#createExpression(ICDIStackFrame, String)
	 */
	public ICDIExpression createExpression(ICDIStackFrame frame, String name) throws CDIException {
		Expression expression = null;
		Session session = (Session)getSession();
		ICDITarget currentTarget = session.getCurrentTarget();
		ICDIThread currentThread = currentTarget.getCurrentThread();
		ICDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
		frame.getThread().setCurrentStackFrame(frame, false);
		try {
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIVarCreate var = factory.createMIVarCreate(name);
			mi.postCommand(var);
			MIVarCreateInfo info = var.getMIVarCreateInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			ICDITarget tgt = frame.getThread().getTarget();
			VariableObject varObj = new VariableObject(tgt, name, frame, 0, 0);
			expression = new Expression(varObj, info.getMIVar());
			expList.add(expression);
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
		return expression;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#getExpressions()
	 */
	public ICDIExpression[] getExpressions() throws CDIException {
		return (ICDIExpression[])expList.toArray(new ICDIExpression[0]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#removeExpression(ICDIExpression)
	 */
	public void destroyExpression(ICDIExpression expression) throws CDIException {
		if (expression instanceof Expression) {
			// Fire  a destroyEvent ?
			Expression exp= (Expression)expression;
			MIVarDeletedEvent del = new MIVarDeletedEvent(exp.getMIVar().getVarName());
			Session session = (Session)getSession();
			MISession mi = session.getMISession();
			mi.fireEvent(del);
		}
	}

	/**
	 * Return the element that have the uniq varName.
	 * null is return if the element is not in the cache.
	 */
	public Variable getExpression(String varName) {
		Expression[] exps = (Expression[])expList.toArray(new Expression[0]);
		for (int i = 0; i < exps.length; i++) {
			if (exps[i].getMIVar().getVarName().equals(varName)) {
				return exps[i];
			}
			Variable v = exps[i].getChild(varName);
			if (v != null) {
				return v;
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#update()
	 */
	public void update() throws CDIException {
		List eventList = new ArrayList();
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		Expression[] exps = (Expression[])expList.toArray(new Expression[0]);
		for (int i = 0; i < exps.length; i++) {
			String varName = exps[i].getMIVar().getVarName();
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
				eventList.add(new MIVarDeletedEvent(varName));
			}
			for (int j = 0 ; j < changes.length; j++) {
				String n = changes[j].getVarName();
				if (changes[j].isInScope()) {
					eventList.add(new MIVarChangedEvent(n));
				}
				// We do not implicitely delete Expressions.
				//else {
				//	eventList.add(new MIVarDeletedEvent(n));
				//}
			}
		}
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);
	}

}
