/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

public final class CIncludeFileEntry extends ACPathEntry implements
		ICIncludeFileEntry {

	public CIncludeFileEntry(String value, int flags) {
		super(value, flags);
	}

	public CIncludeFileEntry(IPath location, int flags) {
		super(location, flags);
	}

	public CIncludeFileEntry(IFile rc, int flags) {
		super(rc, flags);
	}

	@Override
	public final int getKind() {
		return INCLUDE_FILE;
	}

	@Override
	public final boolean isFile() {
		return true;
	}

}
