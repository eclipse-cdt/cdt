/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
