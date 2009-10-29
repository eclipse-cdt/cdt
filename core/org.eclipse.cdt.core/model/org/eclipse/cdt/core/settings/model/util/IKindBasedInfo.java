/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import org.eclipse.cdt.core.settings.model.ICSettingEntry;

/**
 * Type-Parameterised kind based item
 * @param <T>
 */
public interface IKindBasedInfo<T> {
	/**
	 * @return {@link ICSettingEntry} type 
	 */
	int getKind();

	/**
	 * Return type info 
	 * @see KindBasedStore
	 * @return the data stored
	 */
	T getInfo();

	/**
	 * Set info
	 * @param newInfo
	 * @return previous data stored
	 */
	T setInfo(T newInfo);
}
