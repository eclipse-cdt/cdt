/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.properties;

/**
 * Provides context-sensitive properties.  Can be registered as an adapter for
 * an element or implemented directly
 *
 * @since 1.0
 */
public interface IElementPropertiesProvider {

	/**
	 * Common property representing an element's name.  This property can be
	 * used in future extensions for filtering and sorting.
	 *
	 * @since 2.0
	 */
	public static final String PROP_NAME = "name"; //$NON-NLS-1$

	/**
	 * Updates the specified property sets.
	 *
	 * @param updates each update specifies the element and context for which
	 * a set of properties is requested and stores them
	 */
	public void update(IPropertiesUpdate[] updates);
}
