/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.NullType;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

// ---------------------------------------------------------------------------
// ExtensionElement
// ---------------------------------------------------------------------------

/** Facade/utility class for dealing with Eclipse extensions. */
public class ExtensionElement {

	// --- static methods ---

	/** Wraps list of raw extension declarations. */
	public static List<ExtensionElement> wrapExtensionElements(IConfigurationElement[] elements) {
		int count = (elements == null) ? 0 : elements.length;
		List<ExtensionElement> result = new ArrayList<>(count);
		for (int i = 0; i < count; ++i) {
			result.add(new ExtensionElement(elements[i]));
		}
		return result;
	}

	// --- members ---

	/** Actual extension description loaded from extension point. */
	protected IConfigurationElement m_element = null;

	// --- constructors/destructors ---

	/** Constructor. */
	public ExtensionElement(IConfigurationElement element) {
		m_element = element;
	}

	// --- methods ---

	/** Gets string value of specified attribute. */
	public String getAttribute(String attributeName) {
		return m_element.getAttribute(attributeName);
	}

	/** Creates and returns instance of implementing class, using class name found in "class" attribute. */
	public <T> T getClassAttribute() {
		return getClassAttribute("class");
	}

	/** Creates and returns instance of implementing class, using class name found in specified attribute. */
	@SuppressWarnings("unchecked")
	public <T> T getClassAttribute(String attributeName) {
		T result = null;
		try {
			// TODO: Hmm... is there a right way to do this, without an unchecked cast?
			result = (T) m_element.createExecutableExtension(attributeName);
		} catch (ClassCastException ex) {
			// For now, eat it, and don't return this extension.
			// TODO: decide how to log this
		} catch (CoreException ex) {
			// For now, eat it, and don't return this extension.
			// TODO: decide how to log this
		}
		return result;
	}

	/** Creates and returns instance of implementing class, using class name found in specified attribute. */
	@SuppressWarnings("unchecked")
	public <T> T getClassAttribute(String attributeName, Object... arguments) {
		// Make sure we default to zero-argument form if we can.
		if (arguments == null || arguments.length == 0)
			return getClassAttribute(attributeName);

		// If we have arguments, have to do a little extra work.
		T result = null;
		try {
			// Get name of class we're trying to load from attribute.
			String className = getAttribute(attributeName);

			// Attempt to load class using the source plugin's class loader.
			// TODO: is there a better way?
			IContributor contributor = m_element.getContributor();
			String pluginID = contributor.getName();
			Bundle plugin = Platform.getBundle(pluginID);
			Class<?> instance = plugin.loadClass(className);

			// Select appropriate constructor for provided argument(s).
			int argumentsLength = (arguments == null) ? 0 : arguments.length;
			Class<?>[] argumentTypes = new Class<?>[argumentsLength];
			for (int i = 0; i < argumentsLength; ++i) {
				argumentTypes[i] = (arguments[i] == null) ? NullType.class : arguments[i].getClass();
			}
			Constructor<?> constructor = instance.getConstructor(argumentTypes);

			// Invoke the constructor.
			result = (T) constructor.newInstance(arguments);
		} catch (ClassNotFoundException ex) {
			// TODO: decide how to log this
		} catch (NoSuchMethodException ex) {
			// TODO: decide how to log this
		} catch (InvocationTargetException ex) {
			// TODO: decide how to log this
		} catch (IllegalAccessException ex) {
			// TODO: decide how to log this
		} catch (InstantiationException ex) {
			// TODO: decide how to log this
		} catch (ClassCastException ex) {
			// TODO: decide how to log this
		} catch (Exception ex) {
			// TODO: decide how to log this
		}
		return result;
	}

	/** Returns child elements of this element. */
	public List<ExtensionElement> getChildren() {
		return wrapExtensionElements(m_element.getChildren());
	}
}
