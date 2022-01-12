/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;

/**
 * @since 5.1
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTArraySubscriptExpression
		extends IASTArraySubscriptExpression, ICPPASTExpression, IASTImplicitNameOwner {
	@Override
	public ICPPASTArraySubscriptExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTArraySubscriptExpression copy(CopyStyle style);

	/**
	 * @since 5.5
	 */
	@Override
	public ICPPASTExpression getArrayExpression();

	/**
	 * @since 5.5
	 */
	@Override
	public ICPPASTInitializerClause getArgument();
}
