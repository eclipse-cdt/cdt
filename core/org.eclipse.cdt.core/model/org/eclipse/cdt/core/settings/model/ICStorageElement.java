/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;


/**
 *
 * this interface represents the abstract storage that can be used for storing
 * data in the tree-like format of name-value holder elements
 * 
 * This abstract storage mechanism is used, e.g. with the {@link ICProjectDescription} and {@link ICConfigurationDescription}
 * for storing custom data in the settings file (.cproject)
 * 
 * @see ICSettingsStorage
 * @see ICProjectDescription
 * @see ICConfigurationDescription
 *
 */
public interface ICStorageElement {
	ICStorageElement[] getChildren();
	
	String getAttribute(String name);
	
	ICStorageElement getParent();
	
	void setAttribute(String name, String value);
	
	void removeAttribute(String name);
	
	ICStorageElement createChild(String name);

	void clear();
	
	String getName();
	
//	void remove();
	
	void removeChild(ICStorageElement el);
	
	String getValue();
	
	void setValue(String value); 
	
	ICStorageElement importChild(ICStorageElement el) throws UnsupportedOperationException;
}
