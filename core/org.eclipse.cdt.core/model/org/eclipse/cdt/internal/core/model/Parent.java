package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

public abstract class Parent extends CElement implements IParent {
	
	protected IResource resource;

	public Parent (ICElement parent, IPath path, int type) {
		// Check if the file is under the workspace.
		this (parent, ResourcesPlugin.getWorkspace().getRoot().getFileForLocation (path),
			path.lastSegment(), type);
	}

	public Parent (ICElement parent, String name, int type) {
		this (parent, null, name, type);
	}
	
	public Parent (ICElement parent, IResource resource, String name, int type) {
		super (parent, name, type);
		this.resource = resource;
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
		return getElementInfo().getChildren();
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

	public void setUnderlyingResource(IResource res) {
		resource = res;
	}

	public IResource getUnderlyingResource() throws CModelException {
		if (resource == null) {
			ICElement p = getParent();
			if (p != null) {
				return p.getUnderlyingResource();
			}
		}
		return resource;
	}

	public IResource getResource() throws CModelException {
		return null;
	}

	protected void setChanged () {
		getElementInfo().setChanged();
	}

	protected boolean hasChanged () {
		return getElementInfo().hasChanged();
	}

	protected abstract CElementInfo createElementInfo ();
}
