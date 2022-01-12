/*******************************************************************************
 * Copyright (c) 2009, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * Pack expansion as it can occur as an element in an expression-lists or as a
 * non-type template argument.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.2
 */
public interface ICPPASTPackExpansionExpression extends ICPPASTExpression {
	/**
	 * Represents the relationship between a pack-expansion and its pattern.
	 */
	public static final ASTNodeProperty PATTERN = new ASTNodeProperty(
			"ICPPASTPackExpansionExpression.Pattern [IASTExpression]"); //$NON-NLS-1$

	/**
	 * Returns the pattern of the pack expansion.
	 */
	IASTExpression getPattern();

	/**
	 * Sets the pattern of the pack expansion expression. Cannot be called on frozen ast.
	 */
	void setPattern(IASTExpression left);
}
