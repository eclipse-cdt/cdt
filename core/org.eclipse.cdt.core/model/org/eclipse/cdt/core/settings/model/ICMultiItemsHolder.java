/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

/**
 * Implementors of this interface are intended
 * to hold 1 or more items and perform
 * some simultaneous operations on them.
 *
 * There are no any restrictions for items
 * types to be held.
 *
 * As common rule, items are set in constructor
 * and their list cannot be changed in life time.
 *
 */
public interface ICMultiItemsHolder {
	public static final String EMPTY_STR = ""; //$NON-NLS-1$

	/**
	 * @return array of items which it holds
	 */
	Object[] getItems();
}
