/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Provides the ability to view a variable in the memory view.
 * @since 7.4
 */
public interface IViewInMemory extends IAdaptable {

	/**
	 * Returns whether this element can currently be viewed in the memory view.
	 *
	 * @return whether this element can currently be viewed in the memory view.
	 */
	boolean canViewInMemory();

	/**
	 * Displays the element in the memory view.
	 */
	void viewInMemory();
}
