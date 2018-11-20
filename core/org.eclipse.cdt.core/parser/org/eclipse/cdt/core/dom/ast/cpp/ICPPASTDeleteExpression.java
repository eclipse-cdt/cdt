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
 *     Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;

/**
 * This interface represents a delete expression. delete [] operand;
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTDeleteExpression extends ICPPASTExpression, IASTImplicitNameOwner {
	/**
	 * <code>OPERAND</code> is the expression representing the pointer being
	 * deleted.
	 */
	public static final ASTNodeProperty OPERAND = new ASTNodeProperty(
			"ICPPASTDeleteExpression.OPERAND - Expression of poniter being deleted"); //$NON-NLS-1$

	/**
	 * Get the operand.
	 *
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getOperand();

	/**
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setOperand(IASTExpression expression);

	/**
	 * Set this to be the global delete function called.
	 *
	 * @param global
	 *            boolean
	 */
	public void setIsGlobal(boolean global);

	/**
	 * Is this the global delete function called?
	 *
	 * @return boolean
	 */
	public boolean isGlobal();

	/**
	 * Set this to be a vector delete. ([])
	 *
	 * @param vectored
	 *            boolean
	 */
	public void setIsVectored(boolean vectored);

	/**
	 * Is this a delete[] ?
	 *
	 * @return boolean
	 */
	public boolean isVectored();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTDeleteExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTDeleteExpression copy(CopyStyle style);

}
