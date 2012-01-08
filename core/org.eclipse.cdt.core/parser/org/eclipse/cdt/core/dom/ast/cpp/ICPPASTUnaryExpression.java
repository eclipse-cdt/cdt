/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Mike Kucera
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTUnaryExpression extends IASTUnaryExpression, IASTImplicitNameOwner {

	/**
	 * <code>op_throw</code> throw exp
	 */
	public static final int op_throw = IASTUnaryExpression.op_throw;

	/**
	 * <code>op_typeid</code> = typeid( exp )
	 */
	public static final int op_typeid = IASTUnaryExpression.op_typeid;

	/**
	 * @deprecated all constants to be defined in {@link IASTUnaryExpression}
	 */
	@Deprecated
	public static final int op_last = IASTUnaryExpression.op_last;
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTUnaryExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTUnaryExpression copy(CopyStyle style);
}
