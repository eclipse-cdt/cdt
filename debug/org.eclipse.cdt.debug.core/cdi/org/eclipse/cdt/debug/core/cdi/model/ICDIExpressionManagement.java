/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * ICDIExpressionManagement
 */
public interface ICDIExpressionManagement {

	/**
	 * Create an expression for code snippet
	 * @param code
	 * @return ICDIExpression
	 * @throws CDIException
	 */
	ICDIExpression createExpression(String code) throws CDIException;

	/**
	 * Return all expressions for this target
	 * @return
	 * @throws CDIException
	 */
	ICDIExpression[] getExpressions() throws CDIException;

	/**
	 * Remove expressions for this target
	 * 
	 * @param expressions
	 */
	void destroyExpressions(ICDIExpression[] expressions) throws CDIException;

	/**
	 * Remove all expressions on this target
	 *
	 */
	void destroyAllExpressions() throws CDIException;
}
