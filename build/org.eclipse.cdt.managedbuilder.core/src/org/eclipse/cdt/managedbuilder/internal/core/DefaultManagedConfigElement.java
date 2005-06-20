/*******************************************************************************
 * Copyright (c) 2004, 2005 TimeSys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * TimeSys Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;

/**
 * Implements the ManagedConfigElement by delegate all calls to an
 * IConfigurationElement instance.  This is used to load configuration
 * information from the extension point.
 */
public class DefaultManagedConfigElement implements IManagedConfigElement {

	private IConfigurationElement element;
	private IExtension extension;
	
	/**
	 * @param element
	 */
	public DefaultManagedConfigElement(IConfigurationElement element, IExtension extension) {
		this.element = element;
		this.extension = extension;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getName()
	 */
	public String getName() {
		return element.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getAttribute(java.lang.String)
	 */
	public String getAttribute(String name) {
		return element.getAttribute(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getChildren()
	 */
	public IManagedConfigElement[] getChildren() {
		return convertArray(element.getChildren(), extension);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getChildren(java.lang.String)
	 */
	public IManagedConfigElement[] getChildren(String elementName) {
		return convertArray(element.getChildren(elementName), extension);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getExtension(java.lang.String)
	 */
	public IExtension getExtension() {
		return extension;
	}
	
	/**
	 * @return
	 */
	public IConfigurationElement getConfigurationElement() {
		return element;
	}
	
	/**
	 * Convenience method for converting an array of IConfigurationElements
	 * into an array of IManagedConfigElements.
	 */
	public static IManagedConfigElement[] convertArray(
		IConfigurationElement[] elements,
		IExtension extension) {

		IManagedConfigElement[] ret = new IManagedConfigElement[elements.length];
		for (int i = 0; i < elements.length; i++) {
			ret[i] = new DefaultManagedConfigElement(elements[i], extension);
		}
		return ret;
	}

}
