/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 */
public class CContainerInfo extends OpenableInfo {

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
		ICProject cproject = parent.getCProject();
		try {
			IResource[] resources = null;
			if (res instanceof IContainer) {
				IContainer container = (IContainer)res;
				resources = container.members(false);
			}

			IPathEntry[] entries = cproject.getResolvedPathEntries();
			if (resources != null) {
				ICElement[] children = getChildren();
				for (int i = 0; i < resources.length; i++) {
					boolean found = false;
					// Check if the folder is not itself a sourceEntry.
					if (resources[i].getType() == IResource.FOLDER) {
						IPath fullPath = resources[i].getFullPath();
						for (int k = 0; k < entries.length; k++) {
							IPathEntry entry = entries[k];
							if (entry.getEntryKind() == IPathEntry.CDT_SOURCE) {
								IPath sourcePath = entry.getPath();
								if (fullPath.equals(sourcePath)) {
									found = true;
									break;
								}
							}
						}
					}
					// Check the children for a match
					if (!found) {
						for (int j = 0; j < children.length; j++) {
							IResource r = children[j].getResource();
							if (r != null && r.equals(resources[i])){
								found = true;
								break;
							}
						}
					}
					if (!found) {
						notChildren.add(resources[i]);
					}
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
