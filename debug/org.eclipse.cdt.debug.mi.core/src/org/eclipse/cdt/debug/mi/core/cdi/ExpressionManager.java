/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICExpressionManager;
import org.eclipse.cdt.debug.core.cdi.ICSession;
import org.eclipse.cdt.debug.core.cdi.model.ICExpression;
import org.eclipse.cdt.debug.mi.core.MISession;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ExpressionManager implements ICExpressionManager {

	MISession session;
	
	public ExpressionManager(MISession s) {
		session = s;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICExpressionManager#addExpression(ICExpression)
	 */
	public void addExpression(ICExpression expression) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICExpressionManager#getExpression(String)
	 */
	public ICExpression getExpression(String expressionId)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICExpressionManager#getExpressions()
	 */
	public ICExpression[] getExpressions() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICExpressionManager#removeExpression(ICExpression)
	 */
	public void removeExpression(ICExpression expression) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICExpressionManager#removeExpressions(ICExpression[])
	 */
	public void removeExpressions(ICExpression[] expressions)
		throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSessionObject#getSession()
	 */
	public ICSession getSession() {
		return null;
	}

}
