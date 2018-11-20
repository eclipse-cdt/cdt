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
 *     John Camelon (IBM Rational Software) - Initial API and implementation
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
	 * @since 5.1
	 */
	@Override
	public IGPPASTBinaryExpression copy();
}
