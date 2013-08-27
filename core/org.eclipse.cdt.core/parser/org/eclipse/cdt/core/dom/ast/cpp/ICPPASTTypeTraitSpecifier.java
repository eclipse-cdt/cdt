/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTypeTraitType.TypeTraitOperator;

/**
 * A decl-specifier that represents the application of an intrinsic
 * type trait operator like __underlying_type(T). Intrinsic operators
 * of this form take a type as input, and evaluate to a type.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.6
 */
public interface ICPPASTTypeTraitSpecifier extends ICPPASTDeclSpecifier {
	/**
	 * <code>OPERAND</code> represents the relationship between an <code>ICPPASTTypeTraitSpecifier</code> and
	 * its nested <code>IASTTypeId</code>.
	 */
	public static final ASTNodeProperty OPERAND = new ASTNodeProperty("ICPPASTTypeTraitSpecifier.OPERAND - type operand for ICPPASTTypeTraitSpecifier"); //$NON-NLS-1$

	/**
	 * Returns the type trait operator being applied.
	 */
	TypeTraitOperator getOperator();
	
	/**
	 * Returns the type-id to which the type trait operator is being applied.
	 */
	ICPPASTTypeId getOperand();
}
