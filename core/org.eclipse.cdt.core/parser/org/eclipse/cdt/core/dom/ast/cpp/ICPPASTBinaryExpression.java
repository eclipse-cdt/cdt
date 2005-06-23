/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;

/**
 * C++ adds a few more binary expressions over C.
 * 
 * @author jcamelon
 */
public interface ICPPASTBinaryExpression extends IASTBinaryExpression {

	/**
	 * <code>op_pmdot</code> pointer-to-member field dereference.
	 */
	public static final int op_pmdot = IASTBinaryExpression.op_last + 1;

	/**
	 * <code>op_pmarrow</code> pointer-to-member pointer dereference.
	 */
	public static final int op_pmarrow = IASTBinaryExpression.op_last + 2;

	/**
	 * <code>op_last</code> is defined for subinterfaces to further extend.
	 */
	public static final int op_last = op_pmarrow;
}
