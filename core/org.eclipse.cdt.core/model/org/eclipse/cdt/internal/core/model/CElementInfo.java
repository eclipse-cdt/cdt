package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;

/**
 * Holds cached structure and properties for a C element.
 * Subclassed to carry properties for specific kinds of elements.
 */
class CElementInfo {

	/**
	 * Shared empty collection used for efficiency.
	 */
	static Object[] NO_NON_C_RESOURCES = new Object[] {};

	protected CElement element;

	/**
	 * Collection of handles of immediate children of this
	 * object. This is an empty array if this element has
	 * no children.
	 */
	protected ICElement[] fChildren;

	/**
	 * Shared empty collection used for efficiency.
	 */
	protected static ICElement[] fgEmptyChildren = new ICElement[]{};

	/**
	 * Is the structure of this element known
	 * @see ICElement.isStructureKnown()
	 */
	protected boolean fIsStructureKnown = false;

	protected long modificationStamp = 0;

	protected CElementInfo(CElement element) {
		this.element = element;
		fChildren = fgEmptyChildren;
	}

	protected CElement getElement() {
		return element;
	}

	protected void addChild(ICElement child) {
		if (fChildren == fgEmptyChildren) {
			setChildren(new ICElement[] {child});
		} else {
			if (!includesChild(child)) {
				setChildren(growAndAddToArray(fChildren, child));
			}
		}
	}

	protected ICElement[] getChildren() {
		return fChildren;
	}

	/**
	 * Adds the new element to a new array that contains all of the elements of the old array.
	 * Returns the new array.
	 */
	protected ICElement[] growAndAddToArray(ICElement[] array, ICElement addition) {
		ICElement[] old = array;
		array = new ICElement[old.length + 1];
		System.arraycopy(old, 0, array, 0, old.length);
		array[old.length] = addition;
		return array;
	}

	/**
	 * Returns <code>true</code> if this child is in my children collection
	 */
	protected boolean includesChild(ICElement child) {
	
		for (int i= 0; i < fChildren.length; i++) {
			if (fChildren[i].equals(child)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see ICElement.isStructureKnown()
	 */
	protected boolean isStructureKnown() {
		return fIsStructureKnown;
	}

	/**
	 * Returns an array with all the same elements as the specified array except for
	 * the element to remove. Assumes that the deletion is contained in the array.
	 */
	protected ICElement[] removeAndShrinkArray(ICElement[] array, ICElement deletion) {
		ICElement[] old = array;
		array = new ICElement[old.length - 1];
		int j = 0;
		for (int i = 0; i < old.length; i++) {
			if (!old[i].equals(deletion)) {
				array[j] = old[i];
			} else {
				System.arraycopy(old, i + 1, array, j, old.length - (i + 1));
				return array;
			}
			j++;
		}
		return array;
	}

	protected void removeChild(ICElement child) {
		if (includesChild(child)) {
			setChildren(removeAndShrinkArray(fChildren, child));
		}
	}

	protected void removeChildren () {
		fChildren = fgEmptyChildren;
	}

	protected void setChildren(ICElement[] children) {
		fChildren = children;
	}

	protected boolean hasChildren() {
		return fChildren.length > 0;
	}

	protected void setChanged() {
		modificationStamp = 0;
	}

	protected boolean hasChanged () {
		IResource r = null;
		boolean b = false;
		r = getElement().getUnderlyingResource();
		if (r != null && r.exists()) {
			long modif = 0;
			switch(r.getType()) {
				// Adding/Removing does not count as changing, in Eclipse
				// Ask the underlying file system
				case IResource.FOLDER:
				case IResource.PROJECT:
				case IResource.ROOT:
					File file = r.getLocation().toFile();
					modif = file.lastModified();
				break;

				case IResource.FILE:
					modif = r.getModificationStamp();
				break;
			}
			b = (modif != modificationStamp);
			modificationStamp = modif;
		}
		return b;
	}

	/**
	 * Sets whether the structure of this element known
	 * @see ICElement.isStructureKnown()
	 */
	protected void setIsStructureKnown(boolean newIsStructureKnown) {
		fIsStructureKnown = newIsStructureKnown;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error();
		}
	}
}
