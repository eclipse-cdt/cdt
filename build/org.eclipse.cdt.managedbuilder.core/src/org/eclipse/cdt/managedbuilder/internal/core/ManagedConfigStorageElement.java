/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 * 	   James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.core.runtime.CoreException;

public class ManagedConfigStorageElement implements ICStorageElement {
	private IManagedConfigElement fElement;
	private List<IManagedConfigElement> fChildList;
	private ManagedConfigStorageElement fParent;
	public ManagedConfigStorageElement(IManagedConfigElement el){
		this(el, null);
	}

	public ManagedConfigStorageElement(IManagedConfigElement el, ManagedConfigStorageElement parent){
		fElement = el;
		fParent = parent;
	}

	@Override
	public void clear() {
		throw new WriteAccessException();
	}

	@Override
	public ICStorageElement createChild(String name) {
		throw new WriteAccessException();
	}

	@Override
	public String getAttribute(String name) {
		return fElement.getAttribute(name);
	}

	@Override
	public boolean hasAttribute(String name) {
		return fElement.getAttribute(name) != null;
	}

	@Override
	public ICStorageElement[] getChildren() {
		List<IManagedConfigElement> list = getChildList(true);
		return list.toArray(new ManagedConfigStorageElement[list.size()]);
	}

	private List<IManagedConfigElement> getChildList(boolean create){
		if(fChildList == null && create){
			IManagedConfigElement children[] = fElement.getChildren();

			fChildList = new ArrayList<IManagedConfigElement>(children.length);
			fChildList.addAll(Arrays.asList(children));
		}
		return fChildList;
	}

	@Override
	public ICStorageElement[] getChildrenByName(String name) {
		List<ICStorageElement> children = new ArrayList<ICStorageElement>();
		for (ICStorageElement child : getChildren())
			if (name.equals(child.getName()))
				children.add(child);
		return children.toArray(new ICStorageElement[children.size()]);
	}

	@Override
	public boolean hasChildren() {
		return getChildList(true).isEmpty();
	}

	@Override
	public String getName() {
		return fElement.getName();
	}

	@Override
	public ICStorageElement getParent() {
		return fParent;
	}

	@Override
	public String getValue() {
		return null;
	}

	@Override
	public ICStorageElement importChild(ICStorageElement el)
			throws UnsupportedOperationException {
		throw new WriteAccessException();
	}

	@Override
	public void removeAttribute(String name) {
		throw new WriteAccessException();
	}

	@Override
	public void removeChild(ICStorageElement el) {
		throw new WriteAccessException();
	}

	@Override
	public void setAttribute(String name, String value) {
		throw new WriteAccessException();
	}

	@Override
	public void setValue(String value) {
		throw new WriteAccessException();
	}

	@Override
	public String[] getAttributeNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ICStorageElement createCopy() throws UnsupportedOperationException, CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(ICStorageElement other) {
		throw new UnsupportedOperationException();
	}
}
