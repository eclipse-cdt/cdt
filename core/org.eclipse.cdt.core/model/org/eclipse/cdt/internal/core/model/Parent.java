/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;

public abstract class Parent extends CElement {

	public Parent (ICElement parent, String name, int type) {
		super (parent, name, type);
	}

	// members
	
	/**
	 * Adds a child to the current element.
	 * Implementations override this method to support children
	 */
	public void addChild(ICElement member) throws CModelException {
		getElementInfo().addChild(member);
	}

	/**
	 * Removes a child to the current element.
	 * Implementations override this method to support children
	 */
	public void removeChild(ICElement member) throws CModelException {
		getElementInfo().removeChild(member);
	}

	public void removeChildren () throws CModelException {
		getElementInfo().removeChildren();
	}

	/**
	 * Gets the children of this element.
	 * Returns null if the element does not support children
	 * Implementations override this method to support children
	 */		
	public ICElement[] getChildren() throws CModelException {
		CElementInfo info = getElementInfo();
		if (info != null)
			return info.getChildren();
		else 
			return new ICElement[]{};
	}

	/**
	 * Gets the children of a certain type
	 * @param type
	 * @return ArrayList
	 */
	public List getChildrenOfType(int type) throws CModelException {
		ICElement[] children = getChildren();
		int size = children.length;
		ArrayList list = new ArrayList(size);
		for (int i = 0; i < size; ++i) {
			CElement elt = (CElement)children[i];
			if (elt.getElementType() == type) {
				list.add(elt);
			}
		}
		return list;
	}

	public boolean hasChildren () {
		try {
			return getElementInfo().hasChildren();
		} catch (CModelException e) {
			return false;
		}
	}

	protected void setChanged () {
		try {
			getElementInfo().setChanged();
		} catch (CModelException e) {
			// ignore
		}
	}

	protected boolean hasChanged () {
		try {
			return getElementInfo().hasChanged();
		} catch (CModelException e) {
			return false;
		}
	}

}
