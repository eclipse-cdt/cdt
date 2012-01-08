/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;

/**
 * G++ adds its own modifiers and types to the Simple Decl Specifier.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @deprecated Replaced by {@link ICPPASTSimpleDeclSpecifier}.
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
	 * @deprecated All constants must be defined in {@link IASTSimpleDeclSpecifier}.
	 */
	@Deprecated
	public static final int t_last = t_typeof;
	
	/**
	 * @deprecated Replaced by {@link ICPPASTSimpleDeclSpecifier#DECLTYPE_EXPRESSION}.
	 */
	@Deprecated
	public static final ASTNodeProperty TYPEOF_EXPRESSION = new ASTNodeProperty(
			"IGPPASTSimpleDeclSpecifier.TYPEOF_EXPRESSION - typeof() Expression"); //$NON-NLS-1$

}
