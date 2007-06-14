/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;

public class ManagedConfigStorageElement implements ICStorageElement {
	private IManagedConfigElement fElement;
	private List fChildList;
	private ManagedConfigStorageElement fParent;
	public ManagedConfigStorageElement(IManagedConfigElement el){
		this(el, null);
	}

	public ManagedConfigStorageElement(IManagedConfigElement el, ManagedConfigStorageElement parent){
		fElement = el;
		fParent = parent;
	}

	public void clear() {
		throw new WriteAccessException();
	}

	public ICStorageElement createChild(String name) {
		throw new WriteAccessException();
	}

	public String getAttribute(String name) {
		return fElement.getAttribute(name);
	}

	public ICStorageElement[] getChildren() {
		List list = getChildList(true);
		return (ManagedConfigStorageElement[])list.toArray(new ManagedConfigStorageElement[list.size()]);
	}
	
	private List getChildList(boolean create){
		if(fChildList == null && create){
			IManagedConfigElement children[] = fElement.getChildren();
			
			fChildList = new ArrayList(children.length);
			fChildList.addAll(Arrays.asList(children));
		}
		return fChildList;
	}

	public String getName() {
		return fElement.getName();
	}

	public ICStorageElement getParent() {
		return fParent;
	}

	public String getValue() {
		return null;
	}

	public ICStorageElement importChild(ICStorageElement el)
			throws UnsupportedOperationException {
		throw new WriteAccessException();
	}

	public void removeAttribute(String name) {
		throw new WriteAccessException();
	}

	public void removeChild(ICStorageElement el) {
		throw new WriteAccessException();
	}

	public void setAttribute(String name, String value) {
		throw new WriteAccessException();
	}

	public void setValue(String value) {
		throw new WriteAccessException();
	}

	public String[] getAttributeNames() {
		throw new UnsupportedOperationException();
	}
}
