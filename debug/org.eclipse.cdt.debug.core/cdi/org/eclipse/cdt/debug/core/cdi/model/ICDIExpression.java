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

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

/**
 * An expression is a snippet of code that can be evaluated to 
 * produce a value.
 *  
 * @since Jul 9, 2002
 */
public interface ICDIExpression extends ICDIObject {


	/**
	 * Returns the expression snippet of code.
	 * 
	 * @return the expression
	 */
	String getExpressionText();

	/**
	 * Returns true if the variable Object are the same,
	 * For example event if the name is the same because of
	 * casting this may return false;
	 * @return true if the same
	 */
	boolean equals(ICDIExpression expr);

	/**
	 * Returns the value of this expression.
	 * 
	 * @param ICDIStackFrame frame context
	 * @return the value of this expression
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIValue getValue(ICDIStackFrame context) throws CDIException;

	/**
	 * Return the type of this expression
	 * 
	 * @param context frame context
	 * @return
	 * @throws CDIException
	 */
	ICDIType getType(ICDIStackFrame context) throws CDIException;
}
