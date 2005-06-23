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

import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;

/**
 * C++ adds additional literal types to primary expression.
 * 
 * @author jcamelon
 */
public interface ICPPASTLiteralExpression extends IASTLiteralExpression {

	/**
	 * <code>lk_this</code> represents the 'this' keyword.
	 */
	public static final int lk_this = IASTLiteralExpression.lk_last + 1;

	/**
	 * <code>lk_true</code> represents the 'true' keyword.
	 */
	public static final int lk_true = IASTLiteralExpression.lk_last + 2;

	/**
	 * <code>lk_false</code> represents the 'false' keyword.
	 */
	public static final int lk_false = IASTLiteralExpression.lk_last + 3;

	/**
	 * <code>lk_last</code> is maintained for future subinterfaces.
	 */
	public static final int lk_last = lk_false;
}
