/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents a base interface to represent a problem owner or
 * holder.
 * 
 * @author jcamelon
 */
public interface IASTProblemHolder {
	/**
	 * <code>PROBLEM</code> represents the relationship between a
	 * <code>IASTProblemHolder</code> and its <code>IASTProblem</code>.
	 */
	public static final ASTNodeProperty PROBLEM = new ASTNodeProperty("IASTProblemHolder.PROBLEM - IASTProblem for IASTProblemHolder"); //$NON-NLS-1$

	/**
	 * Get the problem.
	 * 
	 * @return <code>IASTProblem</code>
	 */
	public IASTProblem getProblem();

	/**
	 * Set the problem.
	 * 
	 * @param p
	 *            <code>IASTProblem</code>
	 */
	public void setProblem(IASTProblem p);
}
