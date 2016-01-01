/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;

/**
 * @since 5.1
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTFunctionCallExpression
		extends IASTFunctionCallExpression, ICPPASTExpression, IASTImplicitNameOwner {
	@Override
	ICPPASTFunctionCallExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	ICPPASTFunctionCallExpression copy(CopyStyle style);
	
	/**
	 * Returns the function binding for the overloaded operator() invoked by
	 * the function call, or {@code null} if the operator() is not overloaded.
	 * @since 5.8
	 */
	public ICPPFunction getOverload();
}
