/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTBinaryExpression extends IASTBinaryExpression {

	/**
	 * <code>op_pmdot</code> pointer-to-member field dereference.
	 */
	public static final int op_pmdot = IASTBinaryExpression.op_pmdot;

	/**
	 * <code>op_pmarrow</code> pointer-to-member pointer dereference.
	 */
	public static final int op_pmarrow = IASTBinaryExpression.op_pmarrow;

	/**
	 * @deprecated all constants must be defined in {@link IASTBinaryExpression}, to avoid 
	 * duplicate usage of the same constant.
	 */
	@Deprecated
	public static final int op_last = IASTBinaryExpression.op_last;
	
	/**
	 * @since 5.1
	 */
	public ICPPASTBinaryExpression copy();
}
