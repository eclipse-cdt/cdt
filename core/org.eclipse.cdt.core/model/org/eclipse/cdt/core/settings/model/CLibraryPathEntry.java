/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.internal.core.settings.model.ACLanguageSettingPathEntry;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;

public final class CLibraryPathEntry extends ACLanguageSettingPathEntry implements
		ICLibraryPathEntry {

	public CLibraryPathEntry(String value, int flags) {
		super(value, flags);
	}

	public CLibraryPathEntry(IPath location, int flags) {
		super(location, flags);
	}

	public CLibraryPathEntry(IFolder rc, int flags) {
		super(rc, flags);
	}

	public final int getKind() {
		return LIBRARY_PATH;
	}

	public final boolean isFile() {
		return false;
	}

}
