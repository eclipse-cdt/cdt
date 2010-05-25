/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * @return
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
