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
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.parser.IProblem;

/**
 * Interface for problems in the AST tree.
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTProblem extends IProblem, IASTNode {
	/**
	 * @since 5.1
	 */
	@Override
	public IASTProblem copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTProblem copy(CopyStyle style);

	/**
	 * If this problem was triggered by another problem, returns that problem,
	 * otherwise returns null.
	 * @since 6.4
	 */
	public IASTProblem getOriginalProblem();

	/**
	 * Record another problem as being the original cause of this one.
	 * @since 6.4
	 */
	public void setOriginalProblem(IASTProblem original);
}
