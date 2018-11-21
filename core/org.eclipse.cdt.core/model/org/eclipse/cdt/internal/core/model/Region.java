/*******************************************************************************
 * Copyright (c) 2002, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * QNX Software Systems
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IRegion;

/**
 * @see IRegion
 */

public class Region implements IRegion {

	/**
	 * A collection of the top level elements that have been added to the region
	 */
	protected ArrayList<ICElement> fRootElements;

	/**
	 * Creates an empty region.
	 *
	 * @see IRegion
	 */
	public Region() {
		fRootElements = new ArrayList<>(1);
	}

	/**
	 * @see IRegion#add(ICElement)
	 */
	@Override
	public void add(ICElement element) {
		if (!contains(element)) {
			// "new" element added to region
			removeAllChildren(element);
			fRootElements.add(element);
			fRootElements.trimToSize();
		}
	}

	/**
	 * @see IRegion
	 */
	@Override
	public boolean contains(ICElement element) {

		int size = fRootElements.size();
		ArrayList<ICElement> parents = getAncestors(element);

		for (int i = 0; i < size; i++) {
			ICElement aTop = fRootElements.get(i);
			if (aTop.equals(element)) {
				return true;
			}
			for (int j = 0, pSize = parents.size(); j < pSize; j++) {
				if (aTop.equals(parents.get(j))) {
					// an ancestor is already included
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns a collection of all the parents of this element in bottom-up
	 * order.
	 *
	 */
	private ArrayList<ICElement> getAncestors(ICElement element) {
		ArrayList<ICElement> parents = new ArrayList<>();
		ICElement parent = element.getParent();
		while (parent != null) {
			parents.add(parent);
			parent = parent.getParent();
		}
		parents.trimToSize();
		return parents;
	}

	/**
	 * @see IRegion
	 */
	@Override
	public ICElement[] getElements() {
		int size = fRootElements.size();
		ICElement[] roots = new ICElement[size];
		for (int i = 0; i < size; i++) {
			roots[i] = fRootElements.get(i);
		}

		return roots;
	}

	/**
	 * @see IRegion#remove(ICElement)
	 */
	@Override
	public boolean remove(ICElement element) {

		removeAllChildren(element);
		return fRootElements.remove(element);
	}

	/**
	 * Removes any children of this element that are contained within this
	 * region as this parent is about to be added to the region.
	 *
	 * <p>
	 * Children are all children, not just direct children.
	 */
	private void removeAllChildren(ICElement element) {
		if (element instanceof IParent) {
			ArrayList<ICElement> newRootElements = new ArrayList<>();
			for (int i = 0, size = fRootElements.size(); i < size; i++) {
				ICElement currentRoot = fRootElements.get(i);
				// walk the current root hierarchy
				ICElement parent = currentRoot.getParent();
				boolean isChild = false;
				while (parent != null) {
					if (parent.equals(element)) {
						isChild = true;
						break;
					}
					parent = parent.getParent();
				}
				if (!isChild) {
					newRootElements.add(currentRoot);
				}
			}
			fRootElements = newRootElements;
		}
	}

	/**
	 * Returns a printable representation of this region.
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		ICElement[] roots = getElements();
		buffer.append('[');
		for (int i = 0; i < roots.length; i++) {
			buffer.append(roots[i].getElementName());
			if (i < (roots.length - 1)) {
				buffer.append(", "); //$NON-NLS-1$
			}
		}
		buffer.append(']');
		return buffer.toString();
	}
}
