/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ExpressionManager extends SessionObject implements ICDIExpressionManager {

	public ExpressionManager(CSession session) {
		super(session);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#getExpression(String)
	 */
	public ICDIExpression getExpression(String expressionId)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#getExpressions()
	 */
	public ICDIExpression[] getExpressions() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#removeExpression(ICDIExpression)
	 */
	public void removeExpression(ICDIExpression expression) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#removeExpressions(ICDIExpression[])
	 */
	public void removeExpressions(ICDIExpression[] expressions)
		throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#createExpression(String)
	 */
	public ICDIExpression createExpression(String expressionId)
		throws CDIException {
		return null;
	}

}
