/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * An expression is a snippet of code that can be evaluated to 
 * produce a value.
 *  
 * @since Jul 9, 2002
 */
public interface ICDIExpression extends ICDIObject
{
	/**
	 * Returns this expression's snippet of code.
	 * 
	 * @return the expression
	 */
	String getExpressionText();
	
	/**
	 * Returns the current value of this expression or <code>null</code> 
	 * if this expression does not currently have a value.
	 * 
	 * @return the current value of this expression
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIValue getValue() throws CDIException;
}
