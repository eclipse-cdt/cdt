/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems) 
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu;

import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;

/**
 * There are GNU language extensions that apply to both GCC and G++. Unary
 * expressions for _alignOf() and typeof() along the lines of sizeof().
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @deprecated Use {@link IASTTypeIdExpression}, instead.
 */
@Deprecated
public interface IGNUASTTypeIdExpression extends IASTTypeIdExpression {

	/**
	 * <code>op_typeof</code> is used for typeof( typeId ) type expressions.
	 */
	public static final int op_typeof = IASTTypeIdExpression.op_typeof;

	/**
	 * <code>op_alignOf</code> is used for __alignOf( typeId ) type
	 * expressions.
	 */
	public static final int op_alignof = IASTTypeIdExpression.op_alignof;

	/**
	 * @deprecated all constants must be declared in {@link IASTTypeIdExpression} 
	 */
	@Deprecated
	public static final int op_last = IASTTypeIdExpression.op_last;
	
	/**
	 * @since 5.1
	 */
	@Override
	public IGNUASTTypeIdExpression copy();
}
