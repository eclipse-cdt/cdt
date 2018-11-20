/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anders Dahlberg (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

/**
 * Represents a GNU goto expression.
 *
 * <code>
 * foo:
 *   void *labelPtr = &&foo;
 *   goto *labelPtr;
 * </code>
 *
 * @since 5.8
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IGNUASTGotoStatement extends IASTStatement, IASTNameOwner {
	public static final ASTNodeProperty LABEL_NAME = new ASTNodeProperty(
			"IASTGotoExpression.LABEL_NAME [IASTExpression]"); //$NON-NLS-1$

	/**
	 * Returns the label-name expression. The expression resolves to a ILabel binding.
	 *
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getLabelNameExpression();

	/**
	 * Set the label-name expression.
	 *
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setLabelNameExpression(IASTExpression expression);

	/**
	 * @since 5.1
	 */
	@Override
	public IGNUASTGotoStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IGNUASTGotoStatement copy(CopyStyle style);
}
