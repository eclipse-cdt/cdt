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

import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

/**
 * @author jcamelon
 */
public interface ICPPASTUnaryExpression extends IASTUnaryExpression {

	/**
	 * <code>op_throw</code> throw exp
	 */
	public static final int op_throw = IASTUnaryExpression.op_last + 1;

	/**
	 * <code>op_typeid</code> = typeid( exp )
	 */
	public static final int op_typeid = IASTUnaryExpression.op_last + 2;

	/**
	 * <code>op_last</code> is provided for subinterfaces.
	 */
	public static final int op_last = op_typeid;

}
