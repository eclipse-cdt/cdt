/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
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
import org.eclipse.cdt.debug.mi.core.cdi.model.VariableObject;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.command.MIVarUpdate;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIVarChange;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarUpdateInfo;

/**
 */
public class ExpressionManager extends SessionObject implements ICDIExpressionManager{

	private List expList;
	private boolean autoupdate;
	MIVarChange[] noChanges = new MIVarChange[0];

	public ExpressionManager(Session session) {
		super(session);
		expList = Collections.synchronizedList(new ArrayList());
		autoupdate = true;
	}

	synchronized private void addExpression(Expression exp) {
		expList.add(exp);
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
				throw new CDIException("No answer");
			}
			VariableObject varObj = new VariableObject(currentTarget, name, null, 0, 0);
			expression = new Expression(varObj, info.getMIVar());
			addExpression(expression);
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
				throw new CDIException("No answer");
			}
			ICDITarget tgt = frame.getThread().getTarget();
			VariableObject varObj = new VariableObject(tgt, name, frame, 0, 0);
			expression = new Expression(varObj, info.getMIVar());
			addExpression(expression);
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
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#destroyExpression(ICDIExpression)
	 */
	synchronized public void removeExpression(ICDIExpression expression) throws CDIException {
		expList.remove(expression);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#removeExpression(ICDIExpression)
	 */
	public void destroyExpression(ICDIExpression expression) throws CDIException {
		removeExpression(expression);
	}

	/**
	 * Return the element that have the uniq varName.
	 * null is return if the element is not in the cache.
	 */
	public Expression getExpression(String varName) {
		Expression[] exps = (Expression[])expList.toArray(new Expression[0]);
		for (int i = 0; i < exps.length; i++) {
			if (exps[i].getMIVar().getVarName().equals(varName)) {
				return exps[i];
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
					throw new CDIException("No answer");
				}
				changes = info.getMIVarChanges();
			} catch (MIException e) {
				//throw new MI2CDIException(e);
				eventList.add(new MIVarChangedEvent(0, varName, false));
			}
			for (int j = 0 ; j < changes.length; j++) {
				String n = changes[j].getVarName();
				eventList.add(new MIVarChangedEvent(0, n, changes[j].isInScope()));
			}
		}
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#isAutoUpdate()
	 */
	public boolean isAutoUpdate() {
		return autoupdate;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#setAutoUpdate(boolean)
	 */
	public void setAutoUpdate(boolean update) {
		autoupdate = update;
	}

}
