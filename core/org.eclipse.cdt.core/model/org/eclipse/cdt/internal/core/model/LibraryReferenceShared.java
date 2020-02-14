/*******************************************************************************
 * Copyright (c) 2002, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Created on Apr 2, 2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code Template
 * /
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.io.File;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @author alain
 */
public class LibraryReferenceShared extends Binary implements ILibraryReference {

	ILibraryEntry entry;

	public LibraryReferenceShared(ICElement parent, ILibraryEntry e, IBinaryObject bin) {
		super(parent, e.getFullLibraryPath(), bin);
		entry = e;
	}

	@Override
	protected long getModificationStamp() {
		File f = getPath().toFile();
		if (f != null) {
			return f.lastModified();
		}
		return 0;
	}

	@Override
	public IResource getResource() {
		return null;
	}

	@Override
	public boolean exists() {
		File f = getPath().toFile();
		if (f != null) {
			return f.exists();
		}
		return false;
	}

	@Override
	public IPath getPath() {
		return entry.getFullLibraryPath();
	}

	@Override
	public ILibraryEntry getLibraryEntry() {
		return entry;
	}

}
