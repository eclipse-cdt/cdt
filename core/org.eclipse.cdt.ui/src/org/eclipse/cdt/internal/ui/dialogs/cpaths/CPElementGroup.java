/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class CPElementGroup {

	private CPElement parent;
	private final int kind;
	private IResource resource;
	private List children = new ArrayList(1);

	public CPElementGroup(IResource resource) {
		this.kind = -1;
		this.resource = resource;
		this.children = new ArrayList();
	}

	public CPElementGroup(CPElement parent, int kind) {
		this.parent = parent;
		this.kind = kind;
	}

	public IResource getResource() {
		return resource;
	}

	public IPath getPath() {
		return resource != null ? resource.getFullPath() : parent.getPath();
	}

	public CPElement getParent() {
		return parent;
	}

	public int getEntryKind() {
		return kind;
	}

	public boolean equals(Object arg0) {
		if (arg0 == this) {
			return true;
		}
		if (arg0 instanceof CPElementGroup) {
			CPElementGroup other = (CPElementGroup)arg0;
			return (kind == other.kind && ( (parent == null && other.parent == null) || parent.equals(other.parent)) && ( (resource == null && other.resource == null) || resource.equals(other.resource)));
		}
		return false;
	}

	public int hashCode() {
		int hashCode = parent != null ? parent.hashCode() : 0;
		hashCode += resource != null ? resource.hashCode() : 0;
		return hashCode + kind;
	}

	public void addChild(CPElement element) {
		int indx = children.indexOf(element);
		if (indx == -1) {
			children.add(element);
			element.setParent(this);
		} else { 	// add element with closes matching resource path.
			CPElement other = (CPElement)children.get(indx);
			if ( other.getInherited() != null && element.getInherited() != null) {
				IPath otherPath = other.getInherited().getPath(); 
				IPath elemPath = element.getInherited().getPath(); 
				if (!otherPath.equals(elemPath) && otherPath.isPrefixOf(elemPath)) {
					children.remove(indx);
					other.setParent(null);
					children.add(element);
					element.setParent(this);
				}
			}
		}
	}

	public void setChildren(CPElement[] elements) {
		children = new ArrayList(Arrays.asList(elements));
	}
	
	public void addChildren(CPElement[] elements) {
		for (int i = 0; i < elements.length; i++) {
			addChild(elements[i]);
		}
	}

	public boolean removeChild(CPElement element) {
		boolean removed = children.remove(element);
		if (removed) {
			element.setParent(null);
		}
		return removed;
	}

	public CPElement[] getChildren() {
		return (CPElement[])children.toArray(new CPElement[children.size()]);
	}

	/**
	 * @param newPath
	 * @return
	 */
	public boolean contains(CPElement newPath) {
		return children.contains(newPath);
	}

	public void replaceChild(CPElement element, CPElement replaceWith) {
		int idx = children.indexOf(element);
		if (idx != -1) {
			children.remove(idx);
			children.add(idx, replaceWith);
		}
	}

}