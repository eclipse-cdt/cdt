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
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * @deprecated Unified with {@link ICPPASTSimpleTypeConstructorExpression}.
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface ICPPASTTypenameExpression extends ICPPASTSimpleTypeConstructorExpression, IASTNameOwner {
	/**
	 * Was template token consumed?
	 *
	 * @param templateTokenConsumed
	 *            boolean
	 */
	public void setIsTemplate(boolean templateTokenConsumed);

	/**
	 * Was template token consumed?
	 *
	 * @return boolean
	 */
	public boolean isTemplate();

	/**
	 * <code>TYPENAME</code> is the name of the type.
	 */
	public static final ASTNodeProperty TYPENAME = new ASTNodeProperty(
			"ICPPASTTypenameExpression.TYPENAME - The name of the type"); //$NON-NLS-1$

	/**
	 * Set the name.
	 *
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setName(IASTName name);

	/**
	 * Get the name.
	 *
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * <code>INITIAL_VALUE</code> is an expression.
	 */
	public static final ASTNodeProperty INITIAL_VALUE = INITIALIZER;

	/**
	 * Set initial value.
	 *
	 * @param expressionList
	 *            <code>IASTExpression</code>
	 */
	@Override
	public void setInitialValue(IASTExpression expressionList);

	/**
	 * Get initial value.
	 *
	 * @return <code>IASTExpression</code>
	 */
	@Override
	public IASTExpression getInitialValue();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTTypenameExpression copy();

}
