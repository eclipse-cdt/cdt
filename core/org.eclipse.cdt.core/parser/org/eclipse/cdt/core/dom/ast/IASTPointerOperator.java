/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTPointerOperator extends IASTAttributeOwner {
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
