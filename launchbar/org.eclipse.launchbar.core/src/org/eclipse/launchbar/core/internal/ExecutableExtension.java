/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * A wrapper class that delays instantiation of classes until they're needed
 * to prevent early plug-in loading.
 *
 * @param <T> the type of the object created
 */
public class ExecutableExtension<T> {

	private IConfigurationElement element;
	private String propertyName;
	private T object;

	public ExecutableExtension(IConfigurationElement element, String propertyName) {
		this.element = element;
		this.propertyName = propertyName;
	}

	// For testing, pre-populate the object
	public ExecutableExtension(T object) {
		this.object = object;
	}

	/**
	 * Get the object instantiating it if necessary.
	 * @return object
	 * @throws CoreException
	 */
	@SuppressWarnings("unchecked")
	public T get() throws CoreException {
		if (element != null) {
			object = (T) element.createExecutableExtension(propertyName);
			element = null;
			propertyName = null;
		}
		return object;
	}

	/**
	 * Creates a new object. Can't be done if you've done a get already.
	 * @return a new object from the extension or null if get was called earlier
	 * @throws CoreException
	 */
	@SuppressWarnings("unchecked")
	public T create() throws CoreException {
		if (element != null) {
			return (T) element.createExecutableExtension(propertyName);
		}
		return null;
	}
}
