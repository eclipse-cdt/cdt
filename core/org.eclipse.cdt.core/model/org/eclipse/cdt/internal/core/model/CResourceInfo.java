package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.model.ICElement;

/**
 */
public class CResourceInfo extends CElementInfo {

	/**
	 * Constructs a new C Model Info 
	 */
	protected CResourceInfo(CElement element) {
		super(element);
	}

	// Always return true, save the unnecessary probing from the Treeviewer.
	protected boolean hasChildren () {
		return true;
	}

	protected ICElement [] getChildren () {
		try {
			IResource[] resources = null;
			IResource res = getElement().getUnderlyingResource();
			if (res != null) {
				//System.out.println ("  Resource: " + res.getFullPath().toOSString());
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
					// Check for Valid C projects only.
					if(resources[i].getType() == IResource.PROJECT) {
						IProject proj = (IProject)resources[i];
						if (!factory.hasCNature(proj)) {	
							continue;
						}
					}
					addChild(factory.create(getElement(), resources[i]));
				}
			}
		} catch (CoreException e) {
			//System.out.println (e);
			//CPlugin.log (e);
			e.printStackTrace();
		}
		return super.getChildren();
	}
}
