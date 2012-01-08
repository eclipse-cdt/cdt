/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;

/**
 * G++ introduces additional operators.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IGPPASTBinaryExpression extends ICPPASTBinaryExpression {

	/**
	 * <code>op_max</code> represents >?
	 */
	public static final int op_max = IASTBinaryExpression.op_max;

	/**
	 * <code>op_min</code> represents <?
	 */
	public static final int op_min = IASTBinaryExpression.op_min;

	/**
	 * @deprecated all constants must be defined in {@link IASTBinaryExpression} to avoid
	 * using a constant twice.
	 */
	@Deprecated
	public static final int op_last = IASTBinaryExpression.op_last;
	
	/**
	 * @since 5.1
	 */
	@Override
	public IGPPASTBinaryExpression copy();

}
