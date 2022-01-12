/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents a base interface to represent a problem owner or holder.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTProblemHolder {
	/**
	 * <code>PROBLEM</code> represents the relationship between a
	 * <code>IASTProblemHolder</code> and its <code>IASTProblem</code>.
	 */
	public static final ASTNodeProperty PROBLEM = new ASTNodeProperty(
			"IASTProblemHolder.PROBLEM - IASTProblem for IASTProblemHolder"); //$NON-NLS-1$

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
