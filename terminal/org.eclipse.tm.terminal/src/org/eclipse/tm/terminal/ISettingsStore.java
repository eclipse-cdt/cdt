/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal;

/**
 * A simple interface to a store to persist the state of a connection. 
 * 
 * <p>TODO Not to be implemented.
 * @author Michael Scharf
 */
public interface ISettingsStore {
	/**
	 * @param key
	 * @return value
	 */
	String get(String key);

	/**
	 * @param key
	 * @param defaultValue
	 * @return the value or the fecaault
	 */
	String get(String key, String defaultValue);

	/**
	 * Save a string value
	 * @param key
	 * @param value
	 */
	void put(String key, String value);
}
