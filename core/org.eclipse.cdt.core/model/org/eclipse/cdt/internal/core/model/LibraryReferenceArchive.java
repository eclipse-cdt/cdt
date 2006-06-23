/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.io.File;

import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @author alain
 */
public class LibraryReferenceArchive extends Archive implements ILibraryReference {

	ILibraryEntry entry;

	public LibraryReferenceArchive(ICElement parent, ILibraryEntry e, IBinaryArchive ar) {
		super(parent, e.getFullLibraryPath(), ar);
		entry = e;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getResource()
	 */
	public IResource getResource() {
		return null;
	}

	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getPath()
	 */
	public IPath getPath() {
		return entry.getFullLibraryPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#exists()
	 */
	public boolean exists() {
		File f = getPath().toFile();
		if (f != null) {
			return f.exists();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILibraryReference#getLibraryEntry()
	 */
	public ILibraryEntry getLibraryEntry() {
		return entry;
	}
}
