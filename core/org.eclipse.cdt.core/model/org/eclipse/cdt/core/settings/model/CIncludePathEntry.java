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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;

public final class CIncludePathEntry extends ACPathEntry implements ICIncludePathEntry{

	public CIncludePathEntry(String value, int flags) {
		super(value, flags);
	}

	public CIncludePathEntry(IPath location, int flags) {
		super(location, flags);
	}

	public CIncludePathEntry(IFolder rc, int flags) {
		super(rc, flags);
	}

	public boolean isLocal() {
		return checkFlags(LOCAL);
	}

	public final int getKind() {
		return INCLUDE_PATH;
	}

	@Override
	public final boolean isFile() {
		return false;
	}
}
