/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
 * This represents the good ol' * pointer operator.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTPointer extends IASTPointerOperator {
	/**
	 * Returns whether the pointer is const qualified.
	 */
	public boolean isConst();

	/**
	 * Returns whether the pointer is volatile qualified.
	 */
	public boolean isVolatile();

	/**
	 * Returns whether the pointer is restrict qualified.
	 * @since 5.3
	 */
	public boolean isRestrict();

	/**
	 * Not allowed on frozen ast.
	 */
	public void setConst(boolean value);

	/**
	 * Not allowed on frozen ast.
	 */
	public void setVolatile(boolean value);

	/**
	 * Not allowed on frozen ast.
	 * @since 5.3
	 */
	public void setRestrict(boolean value);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTPointer copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTPointer copy(CopyStyle style);
}
