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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public abstract class ACPathEntry extends ACSettingEntry implements ICPathEntry {
//	IPath fFullPath;
//	IPath fLocation;
//	private IPath fPath;

	ACPathEntry(IResource rc, int flags) {
		super(rc.getFullPath().toString(), flags | RESOLVED | VALUE_WORKSPACE_PATH);
//		fFullPath = rc.getFullPath();
//		fPath = rc.getFullPath();
//		fLocation = rc.getLocation();
	}

/*	public ACLanguageSettingPathEntry(IPath fullPath, IPath location, int flags) {
		super(flags);
		fLocation = location;
		fFullPath = fullPath;
	}
*/
	ACPathEntry(String value, int flags) {
		super(value, flags);
	}

	ACPathEntry(IPath path, int flags) {
		super(path.toString(), flags /*| RESOLVED*/);
//		fPath = path;
//		if(isValueWorkspacePath())
//			fFullPath = path;
//		else
//			fLocation = path;
	}

	@Override
	public IPath getFullPath() {
		if(isValueWorkspacePath())
			return new Path(getValue());
		if(isResolved()) {
			IPath path = new Path(getValue());
			return fullPathForLocation(path);
		}
		return null;
	}

	protected IPath fullPathForLocation(IPath location){
		IResource rcs[] = isFile() ?
				(IResource[])ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(location)
				: (IResource[])ResourcesPlugin.getWorkspace().getRoot().findContainersForLocation(location);

		if(rcs.length > 0)
			return rcs[0].getFullPath();
		return null;
	}

	/**
	 * @since 5.4
	 */
	public abstract boolean isFile();

	@Override
	public IPath getLocation() {
		if(!isValueWorkspacePath())
			return new Path(getValue());
		if(isResolved()){
			IPath path = new Path(getValue());
			IResource rc = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			if(rc != null)
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
		return fName;
	}
}
