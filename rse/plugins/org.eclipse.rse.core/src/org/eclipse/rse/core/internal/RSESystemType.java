/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.core.internal;

import java.util.HashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.IRSESystemType;
import org.osgi.framework.Bundle;

/**
 * Class representing a system type.
 */
public class RSESystemType implements IRSESystemType {

	private static final String ATTR_ID = "id";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_DESCRIPTION = "description";
	private static final String ATTR_VALUE = "value";

	String id = null;
	String name = null;
	String description = null;
	HashMap properties;
	Bundle definingBundle = null;

	/**
	 * Constructor for an object representing a system type.
	 * @param element the configuration element describing the system type
	 */
	public RSESystemType(IConfigurationElement element) {

		id = element.getAttribute(ATTR_ID);
		name = element.getAttribute(ATTR_NAME);
		description = element.getAttribute(ATTR_DESCRIPTION);

		loadProperties(element);

		definingBundle = Platform.getBundle(element.getContributor().getName());
	}

	/**
	 * Loads properties defined for the system type.
	 * @param element the configuration element
	 */
	private void loadProperties(IConfigurationElement element) {
		IConfigurationElement[] children = element.getChildren();
		properties = new HashMap(children.length);

		for (int i = 0; i < children.length; i++) {
			IConfigurationElement child = children[i];
			String key = child.getAttribute(ATTR_NAME);
			String value = child.getAttribute(ATTR_VALUE);

			if (key != null && value != null)
				properties.put(key, value);
		}
	}

	/**
	 * Returns the id of the system type.
	 * @see org.eclipse.rse.core.IRSESystemType#getId()
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the name of the system type.
	 * @see org.eclipse.rse.core.IRSESystemType#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the description of the system type.
	 * @see org.eclipse.rse.core.IRSESystemType#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns a property of the system type given a key.
	 * @see org.eclipse.rse.core.IRSESystemType#getProperty(java.lang.String)
	 */
	public String getProperty(String key) {
		return (String)(properties.get(key));
	}

	/**
	 * Returns the bundle which is responsible for the definition of this system type.
	 * @see org.eclipse.rse.core.IRSESystemType#getDefiningBundle()
	 */
	public Bundle getDefiningBundle() {
		return definingBundle;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}
}