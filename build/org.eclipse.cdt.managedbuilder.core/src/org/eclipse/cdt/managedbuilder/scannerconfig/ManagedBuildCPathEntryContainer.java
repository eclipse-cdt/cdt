/**********************************************************************
 * Copyright (c) Apr 21, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.scannerconfig;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * 
 *
 */
public class ManagedBuildCPathEntryContainer implements IPathEntryContainer {

	private ManagedBuildInfo info;
	
	/**
	 * 
	 */
	public ManagedBuildCPathEntryContainer(ManagedBuildInfo info) {
		super();
		this.info = info;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IPathEntryContainer#getPathEntries()
	 */
	public IPathEntry[] getPathEntries() {
		// Resolve the symbols and paths for the project
		IPath resourcePath = info.getOwner().getProjectRelativePath();
		
		ArrayList entries = new ArrayList();
		entries.add(CoreModel.newIncludeEntry(resourcePath, null, new Path("/usr/include/c++"), true));
		entries.add(CoreModel.newIncludeEntry(resourcePath, null, new Path("/usr/include/c++/i686-pc-cygwin"), true));
		entries.add(CoreModel.newIncludeEntry(resourcePath, null, new Path("/usr/include/c++/backward"), true));
		entries.add(CoreModel.newIncludeEntry(resourcePath, null, new Path("/usr/lib/gcc-lib/i686-pc-cygwin/3.3.1/include"), true));
		entries.add(CoreModel.newIncludeEntry(resourcePath, null, new Path("/usr/include"), true));
		entries.add(CoreModel.newIncludeEntry(resourcePath, null, new Path("/usr/include/api"), true));
		
		entries.add(CoreModel.newMacroEntry(resourcePath, "__GNUC__", "3"));
		entries.add(CoreModel.newMacroEntry(resourcePath, "__GNUC_MINOR__", "3"));
		entries.add(CoreModel.newMacroEntry(resourcePath, "__GNUC_PATCHLEVEL__", "1"));
		entries.add(CoreModel.newMacroEntry(resourcePath, "__CYGWIN32__", ""));
		entries.add(CoreModel.newMacroEntry(resourcePath, "__CYGWIN__", ""));
		entries.add(CoreModel.newMacroEntry(resourcePath, "unix", ""));
		entries.add(CoreModel.newMacroEntry(resourcePath, "__unix__", ""));
		entries.add(CoreModel.newMacroEntry(resourcePath, "unix__", ""));
		return (IPathEntry[])entries.toArray(new IPathEntry[entries.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IPathEntryContainer#getDescription()
	 */
	public String getDescription() {
		return "CDT Managed Build Project";	//$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IPathEntryContainer#getPath()
	 */
	public IPath getPath() {
		return new Path("org.eclipse.cdt.managedbuilder.MANAGED_CONTAINER");	//$NON-NLS-1$
	}
}
