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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IPathEntryContainerExtension;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigScope;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredInfoListener;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class PerFileDiscoveredPathContainer extends AbstractDiscoveredPathContainer 
                                            implements IPathEntryContainerExtension {
    private static final IIncludeEntry[] NO_INCLUDENTRIES = new IIncludeEntry[0];
    private static final IMacroEntry[] NO_SYMBOLENTRIES = new IMacroEntry[0];

    static Map fgPathEntries;

    public PerFileDiscoveredPathContainer(IProject project) {
        super(project);
        initialize();
    }

    private static void initialize() {
        if (fgPathEntries == null) {
            fgPathEntries = new HashMap(10);

            IDiscoveredInfoListener listener = new IDiscoveredInfoListener() {

                public void infoRemoved(IDiscoveredPathInfo info) {
                    if (info != null && 
                            ScannerConfigScope.FILE_SCOPE.equals(info.getScope())) {
                        fgPathEntries.remove(info.getProject());
                    }
                }

                public void infoChanged(IDiscoveredPathInfo info) {
                    if (info != null && 
                            ScannerConfigScope.FILE_SCOPE.equals(info.getScope())) {
                        fgPathEntries.remove(info.getProject());
                    }
                }

            };
            MakeCorePlugin.getDefault().getDiscoveryManager().addDiscoveredInfoListener(listener);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.model.IPathEntryContainerExtension#getIncludeEntries(org.eclipse.core.runtime.IPath)
     */
    public IIncludeEntry[] getIncludeEntries(IPath path) {
        IDiscoveredPathInfo info;
        try {
            info = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(fProject);
            IPath[] includes = info.getIncludePaths(path);
            List entries = new ArrayList(includes.length);
            for (int i = 0; i < includes.length; i++) {
                entries.add(CoreModel.newIncludeEntry(path, Path.EMPTY, includes[i])); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return (IIncludeEntry[])entries.toArray(new IIncludeEntry[entries.size()]);
        }
        catch (CoreException e) {
            return NO_INCLUDENTRIES;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.model.IPathEntryContainerExtension#getMacroEntries(org.eclipse.core.runtime.IPath)
     */
    public IMacroEntry[] getMacroEntries(IPath path) {
        IDiscoveredPathInfo info;
        try {
            info = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(fProject);
            Map syms = info.getSymbols(path);
            List entries = new ArrayList(syms.size());
            for (Iterator iter = syms.entrySet().iterator(); iter.hasNext(); ) {
                Entry entry = (Entry)iter.next();
                entries.add(CoreModel.newMacroEntry(path, (String)entry.getKey(), (String)entry.getValue())); //$NON-NLS-1$
            }
            return (IMacroEntry[])entries.toArray(new IMacroEntry[entries.size()]);
        }
        catch (CoreException e) {
            return NO_SYMBOLENTRIES;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.AbstractDiscoveredPathContainer#getPathEntryMap()
     */
    protected Map getPathEntryMap() {
        return fgPathEntries;
    }

}
