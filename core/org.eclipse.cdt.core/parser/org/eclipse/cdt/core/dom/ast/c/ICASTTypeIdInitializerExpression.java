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
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTTypeIdInitializerExpression;

/**
 * C Expression of the format type-id { initializer }
 *
 * GCC allows compound literals for c++, therefore the interface was moved to the common
 * ast interfaces ({@link IASTTypeIdInitializerExpression}). For compatibility this interface
 * is kept.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTTypeIdInitializerExpression extends IASTTypeIdInitializerExpression {
	/**
	 * @since 5.1
	 */
	@Override
	public ICASTTypeIdInitializerExpression copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICASTTypeIdInitializerExpression copy(CopyStyle style);
}
