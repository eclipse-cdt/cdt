/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;

/**
 * @deprecated Everything can be expressed as {@link ICASTSimpleDeclSpecifier}.
 * @noreference This interface is not intended to be referenced by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
@Deprecated
public interface IGCCASTSimpleDeclSpecifier extends ICASTSimpleDeclSpecifier {
	/**
	 * @deprecated Replaced by {@link IASTSimpleDeclSpecifier#DECLTYPE_EXPRESSION}.
	 */
	@Deprecated
	public static final ASTNodeProperty TYPEOF_EXPRESSION = new ASTNodeProperty(
			"IGCCASTSimpleDeclSpecifier.TYPEOF_EXPRESSION - typeof() Expression"); //$NON-NLS-1$

	/**
	 * @deprecated Replaced by {@link IASTSimpleDeclSpecifier#setDeclTypeExpression(IASTExpression)}.
	 */
	@Deprecated
	public void setTypeofExpression(IASTExpression typeofExpression);

	/**
	 * @deprecated Replaced by {@link IASTSimpleDeclSpecifier#getDeclTypeExpression()}.
	 */
	@Deprecated
	public IASTExpression getTypeofExpression();

	/**
	 * @since 5.1
	 */
	@Override
	public IGCCASTSimpleDeclSpecifier copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IGCCASTSimpleDeclSpecifier copy(CopyStyle style);
}
