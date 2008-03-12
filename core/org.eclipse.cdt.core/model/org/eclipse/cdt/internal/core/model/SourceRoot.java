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
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
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
	public boolean isOnSourceEntry(ICElement element) {
		IPath path = element.getPath();
		return this.isOnSourceEntry(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceRoot#isOnSourceEntry(org.eclipse.core.resources.IResource)
	 */
	public boolean isOnSourceEntry(IResource res) {
		IPath path = res.getFullPath();
		return isOnSourceEntry(path);
	}

	public boolean isOnSourceEntry(IPath path) {
		if (sourceEntry.getFullPath().isPrefixOf(path) 
				&& !CoreModelUtil.isExcluded(path, sourceEntry.fullExclusionPatternChars())) {
			return true;
		}
		return false;
	}

	/*
	 * @see CElement
	 */
	public ICElement getHandleFromMemento(String token, MementoTokenizer memento) {
		switch (token.charAt(0)) {
			case CEM_SOURCEFOLDER:
				String name;
				if (memento.hasMoreTokens()) {
					name = memento.nextToken();
					char firstChar = name.charAt(0);
					if (firstChar == CEM_TRANSLATIONUNIT) {
						token = name;
						name = ""; //$NON-NLS-1$
					} else {
						token = null;
					}
				} else {
					name = ""; //$NON-NLS-1$
					token = null;
				}
				CElement folder = (CElement)getCContainer(name);
				if (token == null) {
					return folder.getHandleFromMemento(memento);
				} else {
					return folder.getHandleFromMemento(token, memento);
				}
		case CEM_TRANSLATIONUNIT:
			return super.getHandleFromMemento(token, memento);
		}
		return null;
	}
	/**
	 * @see CElement#getHandleMemento(StringBuilder)
	 */
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
	protected char getHandleMementoDelimiter() {
		return CElement.CEM_SOURCEROOT;
	}

}
