/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTTypeIdExpression extends IASTTypeIdExpression, ICPPASTExpression {
	public static final int op_typeid = IASTTypeIdExpression.op_typeid;

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTTypeIdExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTTypeIdExpression copy(CopyStyle style);
}
