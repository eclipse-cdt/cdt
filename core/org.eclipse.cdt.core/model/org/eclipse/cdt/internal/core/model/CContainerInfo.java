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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
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
		ICElement celement = getElement();
		ICProject cproject = celement.getCProject();
		// move back to the sourceroot.
		while (! (celement instanceof ISourceRoot) && celement != null) {
			celement = celement.getParent();
		}
		ISourceRoot root = null;
		if (celement instanceof ISourceRoot) {
			root = (ISourceRoot)celement;
		} else {
			return new Object[0]; // should not be. assert
		}

		try {
			IResource[] resources = null;
			if (res instanceof IContainer) {
				IContainer container = (IContainer)res;
				resources = container.members(false);
			}

			IPathEntry[] entries = cproject.getResolvedPathEntries();
			if (resources != null) {
				for (int i = 0; i < resources.length; i++) {
					IResource member = resources[i];
					switch(member.getType()) {
						case IResource.FOLDER: {
							// Check if the folder is not itself a sourceEntry.
							IPath resourcePath = member.getFullPath();
							if (cproject.isOnSourceRoot(member) || isSourceEntry(resourcePath, entries)) {
								continue;
							}
							break;
						}
						case IResource.FILE: {
							String filename = member.getName();
							if (CoreModel.isValidTranslationUnitName(cproject.getProject(), filename) &&
									root.isOnSourceEntry(member)) {
								continue;
							}
							if (root.isOnSourceEntry(member)) {
								if (CModelManager.getDefault().createBinaryFile((IFile)member) != null) {
									continue;
								}
							}
							break;
						}
					}
					notChildren.add(member);
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

	private static boolean isSourceEntry(IPath resourcePath, IPathEntry[] entries) {
		for (int k = 0; k < entries.length; k++) {
			IPathEntry entry = entries[k];
			if (entry.getEntryKind() == IPathEntry.CDT_SOURCE) {
				IPath sourcePath = entry.getPath();
				if (resourcePath.equals(sourcePath)) {
					return true;
				}
			}
		}
		return false;
	}
}
