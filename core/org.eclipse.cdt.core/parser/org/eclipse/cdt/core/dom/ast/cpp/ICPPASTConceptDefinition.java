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
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * Represents a C++ concept definition.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 9.3
 */
public interface ICPPASTConceptDefinition extends IASTDeclaration, IASTNameOwner, IASTAttributeOwner {
	/**
	 * <code>CONCEPT_NAME</code> is the name that is brought into the local scope.
	 */
	public static final ASTNodeProperty CONCEPT_NAME = new ASTNodeProperty(
			"ICPPASTConstraintDeclaration.CONCEPT_NAME - Introduced constraint name"); //$NON-NLS-1$

	/**
	 * <code>CONSTRAINT_EXPRESSON</code> is constraint expression.
	 */
	public static final ASTNodeProperty CONSTRAINT_EXPRESSION = new ASTNodeProperty(
			"ICPPASTConstraintDeclaration.CONSTRAINT_EXPRESSION - Constraint expression"); //$NON-NLS-1$

	/**
	 * Returns the constraint name.
	 *
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * Sets the constraint name.
	 *
	 * @param name <code>IASTName</code>
	 */
	public void setName(IASTName name);

	/**
	 * Returns the constraint expression.
	 *
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getExpression();

	/**
	 * Sets the constraint expression.
	 *
	 * @param name <code>IASTExpression</code>
	 */
	public void setExpression(IASTExpression expr);
}
