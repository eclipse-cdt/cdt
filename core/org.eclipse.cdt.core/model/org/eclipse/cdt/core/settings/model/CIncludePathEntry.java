/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;

/**
 * Representation in the project model of include path settings entries.
 * As an example, those are supplied by a gcc compiler with option "-I".
 */
public final class CIncludePathEntry extends ACPathEntry implements ICIncludePathEntry {
	/**
	 * This constructor is discouraged to be referenced by clients.
	 *
	 * Instead, use pooled entries with CDataUtil.createCIncludePathEntry(name, flags).
	 *
	 * @param name - include path. The path can be an absolute location on the local file-system
	 *    or with flag {@link #VALUE_WORKSPACE_PATH} it is treated as workspace full path.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 */
	public CIncludePathEntry(String name, int flags) {
		super(name, flags);
	}

	/**
	 * This constructor is discouraged to be used directly.
	 *
	 * Instead, use pooled entries with CDataUtil.createCIncludePathEntry(location.toString(), flags)
	 * or wrap it with CDataUtil.getPooledEntry(new CIncludePathEntry(location, flags)).
	 *
	 * @param location - include path. The path can be an absolute location on the local
	 *    file-system or with flag {@link #VALUE_WORKSPACE_PATH} it is treated as workspace full path.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 */
	public CIncludePathEntry(IPath location, int flags) {
		super(location, flags);
	}

	/**
	 * This constructor is discouraged to be used directly.
	 *
	 * Instead, use pooled entries wrapping with CDataUtil.getPooledEntry(new CIncludePathEntry(rc, flags)).
	 *
	 * @param rc - include path as a resource in the workspace.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 *    If {@link #VALUE_WORKSPACE_PATH} is missing it will be supplied.
	 */
	public CIncludePathEntry(IFolder rc, int flags) {
		super(rc, flags);
	}

	@Override
	public boolean isLocal() {
		return checkFlags(LOCAL);
	}

	@Override
	public final int getKind() {
		return INCLUDE_PATH;
	}

	@Override
	public final boolean isFile() {
		return false;
	}
}
