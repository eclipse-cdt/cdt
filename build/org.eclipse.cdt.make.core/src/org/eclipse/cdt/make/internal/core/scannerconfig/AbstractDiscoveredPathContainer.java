/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public abstract class AbstractDiscoveredPathContainer implements IPathEntryContainer {
    public static final IPath CONTAINER_ID = new Path("org.eclipse.cdt.make.core.DISCOVERED_SCANNER_INFO"); //$NON-NLS-1$

    protected final IProject fProject;

    public AbstractDiscoveredPathContainer(IProject project) {
        fProject = project;
    }
    
    public IPathEntry[] getPathEntries() {
        IPathEntry[] fPathEntries;
        try {
            fPathEntries = getPathEntries(getPathEntryMap(), fProject);
        } catch (CoreException e) {
            MakeCorePlugin.log(e);
            return new IPathEntry[0];
        }
        return fPathEntries;
    }

    abstract protected Map getPathEntryMap();

    public String getDescription() {
        return MakeMessages.getString("DiscoveredContainer.description"); //$NON-NLS-1$
    }

    public IPath getPath() {
        return CONTAINER_ID;
    }

    public static IPathEntry[] getPathEntries(Map pathEntryMap, IProject project) throws CoreException {
        IPathEntry[] entries = (IPathEntry[])pathEntryMap.get(project);
        if (entries == null) {
            entries = computeNewPathEntries(project);
            pathEntryMap.put(project, entries);
        }
        return entries;
    }

    private static IPathEntry[] computeNewPathEntries(IProject project) throws CoreException {
        IDiscoveredPathInfo info = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project);
        IPath[] includes = info.getIncludePaths();
        Map syms = info.getSymbols();
        List entries = new ArrayList(includes.length + syms.size());
        for (int i = 0; i < includes.length; i++) {
            entries.add(CoreModel.newIncludeEntry(Path.EMPTY, Path.EMPTY, includes[i], true)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        Iterator iter = syms.entrySet().iterator();
        while (iter.hasNext()) {
            Entry entry = (Entry)iter.next();
            entries.add(CoreModel.newMacroEntry(Path.EMPTY, (String)entry.getKey(), (String)entry.getValue())); //$NON-NLS-1$
        }
        return (IPathEntry[])entries.toArray(new IPathEntry[entries.size()]);
    }

}
