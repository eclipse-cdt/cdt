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

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;

/**
 * 
 * Manages the collection of registered expressions in the 
 * debug session.
 * Auto update is on by default.
 * @since Jul 9, 2002
 */
public interface ICDIExpressionManager extends ICDIManager {

	/**
	 * Returns an expression specified by the given identifier.
	 * 
	 * @param expressionId - the expression identifier
	 * @return ICDIExpression an expression specified by the given identifier
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIExpression createExpression(String name) throws CDIException;

	/**
	 * Returns a collection of all registered expressions, possibly empty.
	 * 
	 * @return an array of expressions
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIExpression[] getExpressions() throws CDIException;

	/**
	 * Removes the given expression from the expression manager.
	 * 
	 * @param expressions - the expression to remove
	 * @throws CDIException on failure. Reasons include:
	 */
	void destroyExpression(ICDIExpression expression) throws CDIException;

}
