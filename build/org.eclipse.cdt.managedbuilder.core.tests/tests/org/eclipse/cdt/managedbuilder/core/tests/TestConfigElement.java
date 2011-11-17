/*******************************************************************************
 * Copyright (c) 2004, 2011 TimeSys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * TimeSys Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.core.runtime.IExtension;

public class TestConfigElement implements IManagedConfigElement {

	private String name;
	private Map<String, String> attributeMap;
	private IManagedConfigElement[] children;

	public TestConfigElement(String name, String[][] attributes,
			IManagedConfigElement[] children) {
		this.name = name;
		this.children = children;
		this.attributeMap = new TreeMap<String, String>();
		for (int i = 0; i < attributes.length; i++) {
			attributeMap.put(attributes[i][0], attributes[i][1]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getAttribute(java.lang.String)
	 */
	@Override
	public String getAttribute(String name) {
		return attributeMap.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getChildren()
	 */
	@Override
	public IManagedConfigElement[] getChildren() {
		return children;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getChildren(java.lang.String)
	 */
	@Override
	public IManagedConfigElement[] getChildren(String elementName) {
		List<IManagedConfigElement> ret = new ArrayList<IManagedConfigElement>(children.length);
		for (int i = 0; i < children.length; i++) {
			if (children[i].getName().equals(elementName)) {
				ret.add(children[i]);
			}
		}
		return ret.toArray(new IManagedConfigElement[ret.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedConfigElement#getExtension(java.lang.String)
	 */
	public IExtension getExtension() {
		return null;
	}
}
