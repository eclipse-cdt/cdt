/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;

/**
 * Representation in the project model of library path settings entries.
 * As an example, those are supplied by a gcc compiler with option "-L".
 */
public final class CLibraryPathEntry extends ACPathEntry implements ICLibraryPathEntry {
	/**
	 * This constructor is discouraged to be referenced by clients.
	 *
	 * Instead, use pooled entries with CDataUtil.createCLibraryPathEntry(name, flags).
	 *
	 * @param name - library path. The path can be an absolute location on the local file-system
	 *    or with flag {@link #VALUE_WORKSPACE_PATH} it is treated as workspace full path.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 */
	public CLibraryPathEntry(String name, int flags) {
		super(name, flags);
	}

	/**
	 * This constructor is discouraged to be used directly.
	 *
	 * Instead, use pooled entries with CDataUtil.createCLibraryPathEntry(location.toString(), flags)
	 * or wrap it with CDataUtil.getPooledEntry(new CLibraryPathEntry(location, flags)).
	 *
	 * @param location - library path. The path can be an absolute location on the local
	 *    file-system or with flag {@link #VALUE_WORKSPACE_PATH} it is treated as workspace full path.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 */
	public CLibraryPathEntry(IPath location, int flags) {
		super(location, flags);
	}

	/**
	 * This constructor is discouraged to be used directly.
	 *
	 * Instead, use pooled entries wrapping with CDataUtil.getPooledEntry(new CLibraryPathEntry(rc, flags)).
	 *
	 * @param rc - include path as a resource in the workspace.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 *    If {@link #VALUE_WORKSPACE_PATH} is missing it will be supplied.
	 */
	public CLibraryPathEntry(IFolder rc, int flags) {
		super(rc, flags);
	}

	@Override
	public final int getKind() {
		return LIBRARY_PATH;
	}

	@Override
	public final boolean isFile() {
		return false;
	}

}
