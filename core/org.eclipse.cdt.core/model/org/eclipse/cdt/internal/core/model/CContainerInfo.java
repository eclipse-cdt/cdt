package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 */
public class CContainerInfo extends OpenableInfo {

	/**
	 * Shared empty collection used for efficiency.
	 */
	static Object[] NO_NON_C_RESOURCES = new Object[] {};

	Object[] nonCResources = null;

	/**
	 * Constructs a new C Model Info 
	 */
	protected CContainerInfo(CElement element) {
		super(element);
	}

	/**
	 * @param container
	 * @return
	 */
	public Object[] getNonCResources(IResource res) {
		if (nonCResources != null)
			return nonCResources;

		ArrayList notChildren = new ArrayList();
		ICElement parent = getElement();
		try {
			IResource[] resources = null;
			if (res != null) {
				switch(res.getType()) {
					case IResource.ROOT:
					case IResource.PROJECT:
					case IResource.FOLDER:
						IContainer container = (IContainer)res;
						resources = container.members(false);
						break;

					case IResource.FILE:
						break;
				}
			}

			if (resources != null) {
				CModelManager factory = CModelManager.getDefault();
				for (int i = 0; i < resources.length; i++) {
					ICElement[] children = getChildren();
					boolean found = false;
					for (int j = 0; j < children.length; j++) {
						IResource r = children[j].getResource();
						if (r.equals(resources[i])){
							found = true;
							break;
						}
					}
					if (!found) {
						notChildren.add(resources[i]);
					}
					// Check for Valid C projects only.
					//ICElement celement = factory.create(parent, resources[i]);
					//if (celement == null) {
					//	notChildren.add(resources[i]);
					//}
				}
			}
		} catch (CoreException e) {
			//System.out.println (e);
			//CPlugin.log (e);
			//e.printStackTrace();
		}
		setNonCResources(notChildren.toArray());	
		return nonCResources;
	}

	/**
	 * @param container
	 * @return
	 */
	public void setNonCResources(Object[] resources) {
		nonCResources = resources;
	}
}
