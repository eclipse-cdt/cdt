/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.core.runtime.IExtension;

public class TestConfigElement implements IManagedConfigElement {
	
	private String name;
	private Map attributeMap;
	private IManagedConfigElement[] children;
	
	public TestConfigElement(String name, String[][] attributes, 
			IManagedConfigElement[] children) {
		this.name = name;
		this.children = children;
		this.attributeMap = new TreeMap();
		for (int i = 0; i < attributes.length; i++) {
			attributeMap.put(attributes[i][0], attributes[i][1]);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getAttribute(java.lang.String)
	 */
	public String getAttribute(String name) {
		return (String)attributeMap.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getChildren()
	 */
	public IManagedConfigElement[] getChildren() {
		return children;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getChildren(java.lang.String)
	 */
	public IManagedConfigElement[] getChildren(String elementName) {
		List ret = new ArrayList(children.length);
		for (int i = 0; i < children.length; i++) {
			if (children[i].getName().equals(elementName)) {
				ret.add(children[i]);
			}
		}
		return (IManagedConfigElement[])ret.toArray(new IManagedConfigElement[ret.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getExtension(java.lang.String)
	 */
	public IExtension getExtension() {
		return null;
	}		
}
