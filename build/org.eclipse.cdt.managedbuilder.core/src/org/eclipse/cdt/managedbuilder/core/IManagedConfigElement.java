/**********************************************************************
 * Copyright (c) 2004, 2005 TimeSys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core;

/**
 * This class represents a configuration element for loading the managed build
 * model objects.  They can either be loaded from the ManagedBuildInfo extension
 * point, or from an instance of IManagedConfigProvider.
 */
public interface IManagedConfigElement {
	
	/**
	 * @return the name of this config element (i.e. tag name of the 
	 * corresponding xml element)
	 */
	String getName();
	
	/**
	 * @return the value of the attribute with the given name, or null
	 * if the attribute is unset.
	 */
	String getAttribute(String name);
	
	/**
	 * @return all child elements of the current config element.
	 */
	IManagedConfigElement[] getChildren();
	
	/**
	 * @return all child elements of the current config element, such that
	 * <code>child.getName().equals(elementName)</code>.
	 */
	IManagedConfigElement[] getChildren(String elementName);
}
