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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class CPElementGroup {

	private CPElement parent;
	private final int kind;
	private IResource resource;
	private Map childrenListMap;
	private List childrenList;

	public CPElementGroup(IResource resource) {
		this.kind = -1;
		this.resource = resource;
		this.childrenListMap = new LinkedHashMap(2);
	}

	public CPElementGroup(CPElement parent, int kind) {
		this.parent = parent;
		this.kind = kind;
		this.childrenList = new ArrayList();
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

	public int indexof(CPElement element) {
		List children = getChildrenList(element.getEntryKind(), false);
		return children != null ? children.indexOf(element) : -1;
	}
	
	public void addChild(CPElement element, int insertIndex) {
		List children = getChildrenList(element.getEntryKind(), true);
		children.add(insertIndex, element);
		element.setParent(this);
	}
	
	public void addChild(CPElement element) {
		List children = getChildrenList(element.getEntryKind(), true);
		int indx = children.indexOf(element);
		if (indx == -1) {
			indx = children.size();
			if (element.getInherited() == null) {
				for (int i = 0; i < children.size(); i++) {
					CPElement next = (CPElement)children.get(i);
					if (next.getInherited() != null) {
						indx = i;
						break;
					}
				}
			}
			children.add(indx, element);
			element.setParent(this);
		} else { // add element with closes matching resource path.
			CPElement other = (CPElement)children.get(indx);
			if (other.getInherited() != null && element.getInherited() != null) {
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
		if (elements.length > 0) {
			if (childrenListMap != null) {
				childrenListMap.put(new Integer(elements[0].getEntryKind()), new ArrayList(Arrays.asList(elements)));
			} else {
				childrenList = new ArrayList(Arrays.asList(elements));
			}
		}
	}

	public void addChildren(CPElement[] elements) {
		for (int i = 0; i < elements.length; i++) {
			addChild(elements[i]);
		}
	}

	public boolean removeChild(CPElement element) {
		List children = getChildrenList(element.getEntryKind(), false);
		if (children == null) {
			return false;
		}
		boolean removed = children.remove(element);
		if (removed) {
			element.setParent(null);
		}
		return removed;
	}

	public CPElement[] getChildren(int kind) {
		List children = getChildrenList(kind, true);
		return (CPElement[])children.toArray(new CPElement[children.size()]);
	}

	public CPElement[] getChildren() {
		if (childrenList != null) {
			return (CPElement[])childrenList.toArray(new CPElement[childrenList.size()]);
		}
		Collection lists = childrenListMap.values();
		Iterator iter = lists.iterator();
		List children = new ArrayList();
		while (iter.hasNext()) {
			children.addAll((List)iter.next());
		}
		return (CPElement[])children.toArray(new CPElement[children.size()]);
	}

	/**
	 * @param newPath
	 * @return
	 */
	public boolean contains(CPElement element) {
		List children = getChildrenList(element.getEntryKind(), false);
		if (children == null) {
			return false;
		}
		return children.contains(element);
	}

	public void replaceChild(CPElement element, CPElement replaceWith) {
		List children = getChildrenList(element.getEntryKind(), false);
		if (children == null) {
			return;
		}
		int idx = children.indexOf(element);
		if (idx != -1) {
			children.remove(idx);
			children.add(idx, replaceWith);
		}
	}

	private List getChildrenList(int kind, boolean create) {
		List children = null;
		if (childrenList != null) {
			children = childrenList;
		} else {
			children = (List)childrenListMap.get(new Integer(kind));
			if (children == null && create) {
				children = new ArrayList();
				childrenListMap.put(new Integer(kind), children);
			}
		}
		return children;
	}
}