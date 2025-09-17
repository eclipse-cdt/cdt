/*******************************************************************************
 * Copyright (c) 2025 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * An AST node that may have C++ constraints.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 9.3
 */
public interface ICPPASTConstraintOwner extends IASTNode {
	public static final ASTNodeProperty CONSTRAINT_SPECIFIER = new ASTNodeProperty(
			"ICPPASTAttributeOwner.CONSTRAINT_SPECIFIER"); //$NON-NLS-1$

	/**
	 * Returns an array of all the node's constraint specifiers.
	 */
	public IASTExpression[] getConstraintExpressions();

	/**
	 * Adds a constraint specifier to the node.
	 */
	public void addConstraintExpression(IASTExpression constraintExpression);
}
