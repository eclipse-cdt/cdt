package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;

public abstract class Parent extends CElement implements IParent {

	public Parent (ICElement parent, String name, int type) {
		super (parent, name, type);
	}

	// members
	
	/**
	 * Adds a child to the current element.
	 * Implementations override this method to support children
	 */
	protected void addChild(ICElement member) {
		getElementInfo().addChild(member);
	}

	/**
	 * Removes a child to the current element.
	 * Implementations override this method to support children
	 */
	protected void removeChild(ICElement member) {
		getElementInfo().removeChild(member);
	}

	protected void removeChildren () {
		getElementInfo().removeChildren();
	}

	/**
	 * Gets the children of this element.
	 * Returns null if the element does not support children
	 * Implementations override this method to support children
	 */		
	public ICElement[] getChildren() {
		CElementInfo info = getElementInfo();
		if (info != null)
			return getElementInfo().getChildren();
		else 
			return new ICElement[]{};
	}

	/**
	 * Gets the children of a certain type
	 * @param type
	 * @return ArrayList
	 */
	public ArrayList getChildrenOfType(int type){
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
		return getElementInfo().hasChildren();
	}

	protected void setChanged () {
		getElementInfo().setChanged();
	}

	protected boolean hasChanged () {
		return getElementInfo().hasChanged();
	}

}
