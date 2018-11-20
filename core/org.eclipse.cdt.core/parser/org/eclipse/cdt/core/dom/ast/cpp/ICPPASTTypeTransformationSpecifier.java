/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUnaryTypeTransformation.Operator;

/**
 * A decl-specifier that represents the application of an intrinsic type
 * transformation operator like __underlying_type(T). Intrinsic operators
 * of this form take a type as input, and evaluate to a type.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.6
 */
public interface ICPPASTTypeTransformationSpecifier extends ICPPASTDeclSpecifier {
	/**
	 * <code>OPERAND</code> represents the relationship between an <code>ICPPASTTypeTransformationSpecifier</code> and
	 * its nested <code>IASTTypeId</code>.
	 */
	public static final ASTNodeProperty OPERAND = new ASTNodeProperty(
			"ICPPASTTypeTransformationSpecifier.OPERAND - type operand for ICPPASTTypeTransformationSpecifier"); //$NON-NLS-1$

	/**
	 * Returns the type transformation operator being applied.
	 */
	Operator getOperator();

	/**
	 * Returns the type-id to which the type transformation operator is being applied.
	 */
	ICPPASTTypeId getOperand();
}
