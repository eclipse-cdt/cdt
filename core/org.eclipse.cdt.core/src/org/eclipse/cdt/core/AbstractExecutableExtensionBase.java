/*******************************************************************************
 * Copyright (c) 2009, 2012 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core;

/**
 * Helper abstract class serving as a base for creating a frame of executable class
 * defined as an extension in plugin.xml.
 *
 * @since 5.4
 *
 */
public abstract class AbstractExecutableExtensionBase {
	private String fId;
	private String fName;

	/**
	 * Default constructor will initialize with the name of the class
	 * using reflection mechanism.
	 */
	public AbstractExecutableExtensionBase() {
		fName = this.getClass().getSimpleName();
		fId = this.getClass().getCanonicalName();
	}

	/**
	 * Constructor to initialize with ID and name of the extension.
	 *
	 * @param id - ID of the extension.
	 * @param name - name of the extension.
	 */
	public AbstractExecutableExtensionBase(String id, String name) {
		fName = name;
		fId = id;
	}

	/**
	 * Set extension ID.
	 *
	 * @param id of extension
	 */
	public void setId(String id) {
		fId = id;
	}

	/**
	 * Set extension name.
	 *
	 * @param name of extension
	 */
	public void setName(String name) {
		fName = name;
	}

	/**
	 * @return id of extension
	 */
	public String getId() {
		return fId;
	}

	/**
	 * @return name of extension
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Method toString() for debugging purposes.
	 */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "id=" + fId + ", name=" + fName;
	}
}
