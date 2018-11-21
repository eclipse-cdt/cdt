/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to cast a variable to the given type.
 */
public interface ICastToType extends IAdaptable {

	/**
	 * Returns whether this element can currently be casted.
	 *
	 * @return whether this element can currently be casted
	 */
	boolean canCast();

	/**
	 * Returns the string presentation of the current type.
	 *
	 * @return the string presentation of the current type
	 */
	String getCurrentType();

	/**
	 * Performs the casting to the given type.
	 *
	 * @param type a type to cast to.
	 * @throws DebugException
	 */
	void cast(String type) throws DebugException;

	/**
	 * Restores the original type.
	 *
	 * @throws DebugException
	 */
	void restoreOriginal() throws DebugException;

	/**
	 * Returns whether this element is casted.
	 *
	 * @return whether this element is casted
	 */
	boolean isCasted();
}
