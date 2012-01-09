/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
