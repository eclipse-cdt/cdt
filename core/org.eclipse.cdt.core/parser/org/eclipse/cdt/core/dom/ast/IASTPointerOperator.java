/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTPointerOperator extends IASTNode {
	/**
	 * Constant/sentinel.
	 */
	public static final IASTPointerOperator[] EMPTY_ARRAY = {};

	/**
	 * @since 5.1
	 */
	@Override
	public IASTPointerOperator copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTPointerOperator copy(CopyStyle style);
}
