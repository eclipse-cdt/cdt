/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.typehierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.IAdaptable;

public class THNode implements IAdaptable {
	private THNode fParent;
	private ICElement fElement;
	private List<THNode> fChildren = Collections.emptyList();

	private int fHashCode;
	private boolean fIsFiltered;
	private boolean fIsImplementor;

	/**
	 * Creates a new node for the type hierarchy browser.
	 */
	public THNode(THNode parent, ICElement decl) {
		fParent = parent;
		fElement = decl;
		fHashCode = Objects.hash(fParent, fElement);
	}

	@Override
	public int hashCode() {
		return fHashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof THNode)) {
			return false;
		}

		THNode rhs = (THNode) o;
		if (fHashCode != rhs.fHashCode) {
			return false;
		}

		return Objects.equals(fElement, rhs.fElement);
	}

	/**
	 * Returns the parent node or {@code null} for the root node.
	 */
	public THNode getParent() {
		return fParent;
	}

	public ICElement getElement() {
		return fElement;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ICElement.class)) {
			return getElement();
		}
		return null;
	}

	public boolean isFiltered() {
		return fIsFiltered;
	}

	public void setIsFiltered(boolean val) {
		fIsFiltered = val;
	}

	public void addChild(THNode childNode) {
		if (fChildren.isEmpty()) {
			fChildren = new ArrayList<>();
		}
		fChildren.add(childNode);
	}

	public boolean hasChildren() {
		return !fChildren.isEmpty();
	}

	public THNode[] getChildren() {
		return fChildren.toArray(new THNode[fChildren.size()]);
	}

	public void setIsImplementor(boolean val) {
		fIsImplementor = val;
	}

	public boolean isImplementor() {
		return fIsImplementor;
	}

	public void removeFilteredLeaves() {
		for (Iterator<THNode> iterator = fChildren.iterator(); iterator.hasNext();) {
			THNode child = iterator.next();
			child.removeFilteredLeaves();
			if (child.isFiltered() && !child.hasChildren()) {
				iterator.remove();
			}
		}
	}

	public void removeNonImplementorLeaves() {
		for (Iterator<THNode> iterator = fChildren.iterator(); iterator.hasNext();) {
			THNode child = iterator.next();
			child.removeNonImplementorLeaves();
			if (!child.isImplementor() && !child.hasChildren()) {
				iterator.remove();
			}
		}
	}
}
