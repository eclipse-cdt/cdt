/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

/**
 * Interface for container type preferences, such as map or list
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemPreferenceCompositeValue {
	/**
	 * Returns value of the child element of a given key
	 *
	 * @param key
	 * @return value of the child preference
	 */
	Object getChildValue(String key);

	/**
	 * Sets the value of the child element of a given key
	 *
	 * @param key
	 * @param value
	 */
	void setChildValue(String key, Object value);

	/**
	 * Removes child element matching the given key
	 *
	 * @param key
	 */
	void removeChildValue(String key);
}
