/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredInfoListener;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class DiscoveredPathContainer implements IPathEntryContainer {

	public static IPath CONTAINER_ID = new Path("org.eclipse.cdt.make.core.DISCOVERED_SCANNER_INFO"); //$NON-NLS-1$

	private final IProject fProject;
	static Map fgPathEntries;

	public DiscoveredPathContainer(IProject project) {
		fProject = project;
	}

	public static IPathEntry[] getPathEntries(IProject project) throws CoreException {
		if (fgPathEntries == null) {
			fgPathEntries = new HashMap(10);
			IDiscoveredInfoListener listener = new IDiscoveredInfoListener() {

				public void infoRemoved(IProject project) {
					fgPathEntries.remove(project);
				}

				public void infoChanged(IDiscoveredPathInfo info) {
					fgPathEntries.remove(info.getProject());
				}
			};
			MakeCorePlugin.getDefault().getDiscoveryManager().addDiscoveredInfoListener(listener);
		}
		IPathEntry[] entries = (IPathEntry[])fgPathEntries.get(project);
		if (entries == null) {
			entries = computeNewPathEntries(project);
			fgPathEntries.put(project, entries);
		}
		return entries;
	}

	private static IPathEntry[] computeNewPathEntries(IProject project) throws CoreException {
		IDiscoveredPathInfo info = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project);
		IPath[] includes = info.getIncludePaths();
		Map syms = info.getSymbols();
		List entries = new ArrayList(includes.length + syms.size());
		for (int i = 0; i < includes.length; i++) {
			entries.add(CoreModel.newIncludeEntry(Path.EMPTY, Path.EMPTY, includes[i])); //$NON-NLS-1$ //$NON-NLS-2$
		}
		Iterator iter = syms.entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry)iter.next();
			entries.add(CoreModel.newMacroEntry(Path.EMPTY, (String)entry.getKey(), (String)entry.getValue())); //$NON-NLS-1$
		}
		return (IPathEntry[])entries.toArray(new IPathEntry[entries.size()]);
	}

	public IPathEntry[] getPathEntries() {
		IPathEntry[] fPathEntries;
		try {
			fPathEntries = getPathEntries(fProject);
		} catch (CoreException e) {
			MakeCorePlugin.log(e);
			return new IPathEntry[0];
		}
		return fPathEntries;
	}

	public String getDescription() {
		return MakeMessages.getString("DiscoveredContainer.description"); //$NON-NLS-1$
	}

	public IPath getPath() {
		return CONTAINER_ID;
	}

}
