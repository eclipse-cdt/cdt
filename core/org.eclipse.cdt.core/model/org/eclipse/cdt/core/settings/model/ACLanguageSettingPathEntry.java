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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public abstract class ACLanguageSettingPathEntry extends ACLanguageSettingEntry
		implements ICLanguageSettingPathEntry {
	IPath fFullPath;
	IPath fLocation;
//	private IPath fPath;
	
	public ACLanguageSettingPathEntry(IResource rc, int flags) {
		super(rc.getFullPath().toString(), flags | RESOLVED | VALUE_WORKSPACE_PATH);
		fFullPath = rc.getFullPath();
//		fPath = rc.getFullPath();
		fLocation = rc.getLocation();
	}

/*	public ACLanguageSettingPathEntry(IPath fullPath, IPath location, int flags) {
		super(flags);
		fLocation = location;
		fFullPath = fullPath;
	}
*/
	public ACLanguageSettingPathEntry(String value, int flags) {
		super(value, flags);
	}
	
	public ACLanguageSettingPathEntry(IPath path, int flags) {
		super(path.toString(), flags | RESOLVED);
//		fPath = path;
//		if(isValueWorkspacePath())
//			fFullPath = path;
//		else
//			fLocation = path;
	}

	public IPath getFullPath() {
		if(fFullPath == null && isResolved()){
			if(isValueWorkspacePath()){
				fFullPath = new Path(getValue());
			} else {
				fLocation = new Path(getValue());
				fFullPath = fullPathForLocation(fLocation);
			}
		}
		return fFullPath;
	}
	
	protected IPath fullPathForLocation(IPath location){
		IResource rcs[] = isFile() ?
				(IResource[])ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(location)
				: (IResource[])ResourcesPlugin.getWorkspace().getRoot().findContainersForLocation(location);
		
		if(rcs.length > 0)
			return rcs[0].getFullPath();
		return null;
	}
	
	protected abstract boolean isFile();

	public IPath getLocation() {
		if(fLocation == null && isResolved()){
			if(isValueWorkspacePath()){
				fFullPath = new Path(getValue());
				IResource rc = ResourcesPlugin.getWorkspace().getRoot().findMember(fFullPath);
				if(rc != null)
					fLocation = rc.getLocation();
			} else {
				fLocation = new Path(getValue());
			}
		}
		return fLocation;
	}
	
	public boolean isValueWorkspacePath() {
		return checkFlags(VALUE_WORKSPACE_PATH);
	}
}
