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

import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * IncludeReference
 */
public class IncludeReference extends Openable implements IIncludeReference {
	
	IIncludeEntry fIncludeEntry;

	/**
	 * @param parent
	 * @param name
	 * @param type
	 */
	public IncludeReference(ICProject cproject, IIncludeEntry entry) {
		super(cproject, null, entry.getIncludePath().toString(), ICElement.C_VCONTAINER);
		fIncludeEntry = entry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getResource()
	 */
	public IResource getResource() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#createElementInfo()
	 */
	protected CElementInfo createElementInfo() {
		return new OpenableInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IIncludeReference#getIncludeEntry()
	 */
	public IIncludeEntry getIncludeEntry() {
		return fIncludeEntry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#generateInfos(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	protected boolean generateInfos(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws CModelException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IIncludeReference#getAffectedPath()
	 */
	public IPath getAffectedPath() {
		return fIncludeEntry.getPath();
	}
}
