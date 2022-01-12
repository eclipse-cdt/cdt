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
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;

/**
 * @deprecated Replaced by {@link ICPPASTSimpleDeclSpecifier}.
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface IGPPASTSimpleDeclSpecifier extends IGPPASTDeclSpecifier, ICPPASTSimpleDeclSpecifier {
	/**
	 * @deprecated Replaced by {@link ICPPASTSimpleDeclSpecifier#setDeclTypeExpression(IASTExpression)}.
	 */
	@Deprecated
	public void setTypeofExpression(IASTExpression typeofExpression);

	/**
	 * @deprecated Replaced by {@link ICPPASTSimpleDeclSpecifier#getDeclTypeExpression()}.
	 */
	@Deprecated
	public IASTExpression getTypeofExpression();

	/**
	 * @since 5.1
	 */
	@Override
	public IGPPASTSimpleDeclSpecifier copy();

	/**
	 * @deprecated Replaced by {@link ICPPASTSimpleDeclSpecifier#DECLTYPE_EXPRESSION}.
	 */
	@Deprecated
	public static final ASTNodeProperty TYPEOF_EXPRESSION = new ASTNodeProperty(
			"IGPPASTSimpleDeclSpecifier.TYPEOF_EXPRESSION - typeof() Expression"); //$NON-NLS-1$
}
