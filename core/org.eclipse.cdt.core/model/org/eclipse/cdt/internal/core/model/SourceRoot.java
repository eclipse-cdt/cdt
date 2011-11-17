/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;


import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * SourceRoot
 */
public class SourceRoot extends CContainer implements ISourceRoot {

	ICSourceEntry sourceEntry;

	/**
	 * @param parent
	 * @param res
	 */
	public SourceRoot(ICElement parent, IResource res, ICSourceEntry entry) {
		super(parent, res);
		sourceEntry = entry;
		IPath path = getPath();
		IPath cpath = getParent().getPath();
		if (path.segmentCount() > cpath.segmentCount()) {
			IPath p = path.removeFirstSegments(cpath.segmentCount());
			setElementName(p.toString());
		}
	}

	public ICSourceEntry getSourceEntry() {
		return sourceEntry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceRoot#isOnclasspath(org.eclipse.cdt.core.model.ICElement)
	 */
	@Override
	public boolean isOnSourceEntry(ICElement element) {
		IPath path = element.getPath();
		return this.isOnSourceEntry(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceRoot#isOnSourceEntry(org.eclipse.core.resources.IResource)
	 */
	@Override
	public boolean isOnSourceEntry(IResource res) {
		IPath path = res.getFullPath();
		return isOnSourceEntry(path);
	}

	@Override
	public boolean isOnSourceEntry(IPath path) {
		if (sourceEntry.getFullPath().isPrefixOf(path)
				&& !CoreModelUtil.isExcluded(path, sourceEntry.fullExclusionPatternChars())) {
			return true;
		}
		return false;
	}

	@Override
	public void getHandleMemento(StringBuilder buff) {
		IPath path;
		IResource underlyingResource = getResource();
		if (underlyingResource != null) {
			if (getResource().getProject().equals(getCProject().getProject())) {
				path = underlyingResource.getProjectRelativePath();
			} else {
				path = underlyingResource.getFullPath();
			}
		} else {
			path= Path.EMPTY;
		}
		((CElement)getParent()).getHandleMemento(buff);
		buff.append(getHandleMementoDelimiter());
		escapeMementoName(buff, path.toString());
	}

	/**
	 * @see CElement#getHandleMemento()
	 */
	@Override
	protected char getHandleMementoDelimiter() {
		return CElement.CEM_SOURCEROOT;
	}

}
