/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * SourceRoot
 */
public class SourceRoot extends CContainer implements ISourceRoot {

	ISourceEntry sourceEntry;

	/**
	 * @param parent
	 * @param res
	 */
	public SourceRoot(ICElement parent, IResource res, ISourceEntry entry) {
		super(parent, res);
		sourceEntry = entry;
		IPath path = getPath();
		IPath cpath = getParent().getPath();
		if (path.segmentCount() > cpath.segmentCount()) {
			IPath p = path.removeFirstSegments(cpath.segmentCount());
			setElementName(p.toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CContainer#computeChildren(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.resources.IResource)
	 */
	protected boolean computeChildren(OpenableInfo info, IResource res) throws CModelException {
			ArrayList vChildren = new ArrayList();
			try {
				IResource[] resources = null;
				if (res instanceof IContainer) {
					//System.out.println (" Resource: " +
					// res.getFullPath().toOSString());
					IContainer container = (IContainer) res;
					resources = container.members(false);
				}
				if (resources != null) {
					ICProject cproject = getCProject();
					for (int i = 0; i < resources.length; i++) {
						// Check for Valid C Element only.
						ICElement celement = null;						
						if (isOnSourceEntry(resources[i].getFullPath())) {
							celement = computeChild(resources[i], cproject);
						}
						if (celement != null) {
							vChildren.add(celement);
						}
					}
				}
			} catch (CoreException e) {
				//System.out.println (e);
				//CPlugin.log (e);
				//e.printStackTrace();
				throw new CModelException(e);
			}
			ICElement[] children = new ICElement[vChildren.size()];
			vChildren.toArray(children);
			info.setChildren(children);
			if (info instanceof CContainerInfo) {
				((CContainerInfo) info).setNonCResources(null);
			}
			return true;
	}

	public ISourceEntry getSourceEntry() {
		return sourceEntry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceRoot#isOnclasspath(org.eclipse.cdt.core.model.ICElement)
	 */
	public boolean isOnSourceEntry(ICElement element) {
		IPath path = element.getPath();
		if (element.getElementType() == ICElement.C_CCONTAINER) {
			// ensure that folders are only excluded if all of their children are excluded
			path = path.append("*"); //$NON-NLS-1$
		}
		return this.isOnSourceEntry(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceRoot#isOnSourceEntry(org.eclipse.core.resources.IResource)
	 */
	public boolean isOnSourceEntry(IResource resource) {
		IPath path = resource.getFullPath();
		
		// ensure that folders are only excluded if all of their children are excluded
		if (resource.getType() == IResource.FOLDER) {
			path = path.append("*"); //$NON-NLS-1$
		}
		
		return isOnSourceEntry(path);
	}

	private boolean isOnSourceEntry(IPath path) {
		if (sourceEntry.getPath().isPrefixOf(path) 
				&& !CoreModelUtil.isExcluded(path, sourceEntry.fullExclusionPatternChars())) {
			return true;
		}
		return false;
	}

}
