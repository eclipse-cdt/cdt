/*******************************************************************************
 * Copyright (c) 2007, 2014 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public abstract class ACPathEntry extends ACSettingEntry implements ICPathEntry {
	/**
	 * Creates an ACPathEntry.
	 *
	 * @param rc - a resource in the workspace.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 *     If {@link #VALUE_WORKSPACE_PATH} is missing it will be supplied.
	 */
	ACPathEntry(IResource rc, int flags) {
		super(rc.getFullPath().toString(), flags | RESOLVED | VALUE_WORKSPACE_PATH);
	}

	/**
	 * Creates an ACPathEntry.
	 *
	 * @param name - resource path. The path can be an absolute location on the local file-system
	 *     or with flag {@link #VALUE_WORKSPACE_PATH} it is treated as workspace full path.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 */
	ACPathEntry(String name, int flags) {
		super(name, flags);
	}

	/**
	 * Constructor.
	 *
	 * @param path - resource path. The path can be an absolute location on the local
	 *    file-system or with flag {@link #VALUE_WORKSPACE_PATH} it is treated as workspace full path.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 */
	ACPathEntry(IPath path, int flags) {
		super(path.toString(), flags /*| RESOLVED*/);
	}

	@Override
	public IPath getFullPath() {
		if (isValueWorkspacePath())
			return new Path(getValue());
		if (isResolved()) {
			IPath path = new Path(getValue());
			return fullPathForLocation(path);
		}
		return null;
	}

	protected IPath fullPathForLocation(IPath location) {
		IResource rcs[] = isFile()
				? (IResource[]) ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(location)
				: (IResource[]) ResourcesPlugin.getWorkspace().getRoot().findContainersForLocation(location);

		if (rcs.length > 0)
			return rcs[0].getFullPath();
		return null;
	}

	/**
	 * @since 5.4
	 */
	public abstract boolean isFile();

	@Override
	public IPath getLocation() {
		if (!isValueWorkspacePath())
			return new Path(getValue());
		if (isResolved()) {
			IPath path = new Path(getValue());
			IResource rc = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			if (rc != null)
				return rc.getLocation();
		}
		return null;
	}

	@Override
	public boolean isValueWorkspacePath() {
		return checkFlags(VALUE_WORKSPACE_PATH);
	}

	@Override
	protected String contentsToString() {
		return getName();
	}
}
