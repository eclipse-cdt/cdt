/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
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
	private Map<Integer, List<CPElement>> childrenListMap;
	private List<CPElement> childrenList;

	public CPElementGroup(IResource resource) {
		this.kind = -1;
		this.resource = resource;
		this.childrenListMap = new LinkedHashMap<>(2);
	}

	public CPElementGroup(CPElement parent, int kind) {
		this.parent = parent;
		this.kind = kind;
		this.childrenList = new ArrayList<>();
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

	@Override
	public boolean equals(Object arg0) {
		if (arg0 == this) {
			return true;
		}
		if (arg0 instanceof CPElementGroup) {
			CPElementGroup other = (CPElementGroup) arg0;
			return (kind == other.kind && ((parent == null && other.parent == null) || parent.equals(other.parent))
					&& ((resource == null && other.resource == null) || resource.equals(other.resource)));
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = parent != null ? parent.hashCode() : 0;
		hashCode += resource != null ? resource.hashCode() : 0;
		return hashCode + kind;
	}

	public int indexof(CPElement element) {
		List<CPElement> children = getChildrenList(element.getEntryKind(), false);
		return children != null ? children.indexOf(element) : -1;
	}

	public void addChild(CPElement element, int insertIndex) {
		List<CPElement> children = getChildrenList(element.getEntryKind(), true);
		children.add(insertIndex, element);
		element.setParent(this);
	}

	public void addChild(CPElement element) {
		List<CPElement> children = getChildrenList(element.getEntryKind(), true);
		int indx = children.indexOf(element);
		if (indx == -1) {
			indx = children.size();
			if (element.getInherited() == null) {
				for (int i = 0; i < children.size(); i++) {
					CPElement next = children.get(i);
					if (next.getInherited() != null) {
						indx = i;
						break;
					}
				}
			}
			children.add(indx, element);
			element.setParent(this);
		} else { // add element with closes matching resource path.
			CPElement other = children.get(indx);
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
				childrenListMap.put(Integer.valueOf(elements[0].getEntryKind()),
						new ArrayList<>(Arrays.asList(elements)));
			} else {
				childrenList = new ArrayList<>(Arrays.asList(elements));
			}
		}
	}

	public void addChildren(CPElement[] elements) {
		for (CPElement element : elements) {
			addChild(element);
		}
	}

	public boolean removeChild(CPElement element) {
		List<CPElement> children = getChildrenList(element.getEntryKind(), false);
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
		List<CPElement> children = getChildrenList(kind, true);
		return children.toArray(new CPElement[children.size()]);
	}

	public CPElement[] getChildren() {
		if (childrenList != null) {
			return childrenList.toArray(new CPElement[childrenList.size()]);
		}
		Collection<List<CPElement>> lists = childrenListMap.values();
		Iterator<List<CPElement>> iter = lists.iterator();
		List<CPElement> children = new ArrayList<>();
		while (iter.hasNext()) {
			children.addAll(iter.next());
		}
		return children.toArray(new CPElement[children.size()]);
	}

	public boolean contains(CPElement element) {
		List<CPElement> children = getChildrenList(element.getEntryKind(), false);
		if (children == null) {
			return false;
		}
		return children.contains(element);
	}

	public void replaceChild(CPElement element, CPElement replaceWith) {
		List<CPElement> children = getChildrenList(element.getEntryKind(), false);
		if (children == null) {
			return;
		}
		int idx = children.indexOf(element);
		if (idx != -1) {
			children.remove(idx);
			children.add(idx, replaceWith);
		}
	}

	private List<CPElement> getChildrenList(int kind, boolean create) {
		List<CPElement> children = null;
		if (childrenList != null) {
			children = childrenList;
		} else {
			children = childrenListMap.get(Integer.valueOf(kind));
			if (children == null && create) {
				children = new ArrayList<>();
				childrenListMap.put(Integer.valueOf(kind), children);
			}
		}
		return children;
	}
}
