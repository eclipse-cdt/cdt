/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;

/**
 * 
 * Manages the collection of registered expressions in the 
 * debug session.
 * 
 * @since Jul 9, 2002
 */
public interface ICDIExpressionManager extends ICDISessionObject
{
	
	/**
	 * Removes the given array of expressions from the expression 
	 * manager.
	 * 
	 * @param expressions - the array of expressions to remove
	 * @throws CDIException on failure. Reasons include:
	 */
	void removeExpressions( ICDIExpression[] expressions ) throws CDIException;
	
	/**
	 * Removes the given expression from the expression manager.
	 * 
	 * @param expressions - the expression to remove
	 * @throws CDIException on failure. Reasons include:
	 */
	void removeExpression( ICDIExpression expression ) throws CDIException;

	/**
	 * Returns an expression specified by the given identifier.
	 * 
	 * @param expressionId - the expression identifier
	 * @return ICDIExpression an expression specified by the given identifier
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIExpression createExpression( String expressionId ) throws CDIException;
	
	/**
	 * Returns a collection of all registered expressions, possibly empty.
	 * 
	 * @return an array of expressions
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIExpression[] getExpressions() throws CDIException;
}
