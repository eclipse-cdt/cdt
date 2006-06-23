/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.io.File;
import java.util.List;
import java.util.Vector;

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
	private List fChildren;

	/**
	 * Is the structure of this element known
	 * @see ICElement.isStructureKnown()
	 */
	protected boolean fIsStructureKnown = false;

	protected long modificationStamp = 0;

	protected CElementInfo(CElement element) {
		this.element = element;
		// Array list starts with size = 0
		fChildren = new Vector(0);
	}

	protected CElement getElement() {
		return element;
	}

	protected void addChild(ICElement child) {
		// Do not add a check if the child is contained here
		// because it causes a performance bottle neck for large files.
		fChildren.add(child);
	}

	protected ICElement[] getChildren() {
		synchronized (fChildren) {
			ICElement[] array= new ICElement[fChildren.size()];		
			return (ICElement[]) fChildren.toArray( array );
		}
	}


	/**
	 * Returns <code>true</code> if this child is in my children collection
	 */
	protected boolean includesChild(ICElement child) {	
		if(fChildren.contains(child))
			return true;
		return false;
	}

	/**
	 * @see ICElement.isStructureKnown()
	 */
	protected boolean isStructureKnown() {
		return fIsStructureKnown;
	}

	protected void removeChild(ICElement child) {
		fChildren.remove(child);
	}

	protected void removeChildren () {
		fChildren.clear();
	}

	protected void setChildren(List children) {
		fChildren.addAll(children);
	}

	protected boolean hasChildren() {
		return fChildren.size() > 0;
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
