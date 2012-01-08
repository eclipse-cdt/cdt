/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;


/**
 * This is the root class of expressions.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTExpression extends IASTInitializerClause {
	/**
	 * @since 5.3
	 */
	public enum ValueCategory {
		/**
		 * Traditional lvalue
		 */
		LVALUE,
		/**
		 * Expiring value as introduced by c++ 0x.
		 */
		XVALUE,
		/**
		 * Pure rvalue.
		 */
		PRVALUE;
		
		/**
		 * Both prvalues and xvalues are rvalues.
		 */
		public boolean isRValue() {
			return this != LVALUE;
		}
		/**
		 * A generalized lvalue is either an lvalue or an xvalue.
		 */
		public boolean isGLValue() {
			return this != PRVALUE;
		}
	}

	/**
	 * Empty expression array.
	 */
	public static final IASTExpression[] EMPTY_EXPRESSION_ARRAY = new IASTExpression[0];
	
	public IType getExpressionType();
	
	/**
	 * Returns whether this expression is an lvalue. LValues are for instance required on the
	 * left hand side of an assignment expression.
	 * @since 5.2
	 */
	public boolean isLValue();
	
	/**
	 * Returns the value category of this expression.
	 * @since 5.3
	 */
	ValueCategory getValueCategory();

	/**
	 * @since 5.1
	 */
	@Override
	public IASTExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTExpression copy(CopyStyle style);
}
