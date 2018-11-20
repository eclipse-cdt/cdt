/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPointerType extends IType {
	/**
	 * Returns the type that this is a pointer to.
	 */
	public IType getType();

	/**
	 * Returns whether the pointer is const qualified.
	 */
	public boolean isConst();

	/**
	 * Returns whether the pointer is volatile qualified.
	 */
	public boolean isVolatile();

	/**
	 * Returns whether the pointer is qualified to be restrict.
	 * For c++ this is a GNU-extension.
	 * @since 5.3
	 */
	boolean isRestrict();
}
