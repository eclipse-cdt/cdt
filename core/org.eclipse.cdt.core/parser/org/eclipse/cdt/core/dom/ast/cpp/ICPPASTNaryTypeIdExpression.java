/*******************************************************************************
 * Copyright (c) 2016 Nathan Ridge.
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

/**
 * A type-id expression with any number of arguments.
 * Example: __is_trivially_constructible(MyClass, int, float)
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 6.0
 */
public interface ICPPASTNaryTypeIdExpression extends ICPPASTExpression {
	public static final ASTNodeProperty OPERAND = new ASTNodeProperty("ICPPASTNaryTypeIdExpression.OPERAND [IASTTypeId]"); //$NON-NLS-1$

	public static enum Operator {
		__is_trivially_constructible
	}

	/**
	 * Returns the operator of the expression.
	 */
	public Operator getOperator();
	
	/**
	 * Returns the operands of the expression.
	 */
	public ICPPASTTypeId[] getOperands();

	@Override
	public ICPPASTNaryTypeIdExpression copy();

	@Override
	public ICPPASTNaryTypeIdExpression copy(CopyStyle style);
}
