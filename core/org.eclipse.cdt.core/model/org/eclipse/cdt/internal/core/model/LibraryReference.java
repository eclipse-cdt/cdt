/*******************************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
/*
 * Created on Apr 2, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.cdt.internal.core.model;

import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author alain
 */
public class LibraryReference extends Parent implements ILibraryReference {

	ILibraryEntry entry;

	public LibraryReference(ICElement parent, ILibraryEntry e) {
		super(parent, e.getLibraryPath().lastSegment(), ICElement.C_VCONTAINER);
		entry = e;
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
		return new CElementInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getPath()
	 */
	public IPath getPath() {
		return entry.getFullLibraryPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILibraryReference#getLibraryEntry()
	 */
	public ILibraryEntry getLibraryEntry() {
		return entry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#generateInfos(java.lang.Object, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void generateInfos(Object info, Map newElements, IProgressMonitor monitor) throws CModelException {
	}

}
