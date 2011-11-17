/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainerExtension;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class PerFileDiscoveredPathContainer extends DiscoveredPathContainer
                                            implements IPathEntryContainerExtension {

    public PerFileDiscoveredPathContainer(IProject project) {
        super(project);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.model.IPathEntryContainerExtension#getPathEntries(org.eclipse.core.runtime.IPath, int)
     */
    @Override
	public IPathEntry[] getPathEntries(IPath path, int mask) {
		ArrayList<IPathEntry> entries = new ArrayList<IPathEntry>();
        try {
            IDiscoveredPathInfo info = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(fProject);
            if (info instanceof IPerFileDiscoveredPathInfo) {
                IPerFileDiscoveredPathInfo filePathInfo = (IPerFileDiscoveredPathInfo) info;

        		if ((mask & IPathEntry.CDT_INCLUDE) != 0) {
    				IPath[] includes = filePathInfo.getIncludePaths(path);
    				for (int i = 0; i < includes.length; i++) {
                        // add as a system include path
    					entries.add(CoreModel.newIncludeEntry(path, Path.EMPTY, includes[i], true));
    				}
                    includes = filePathInfo.getQuoteIncludePaths(path);
                    for (int i = 0; i < includes.length; i++) {
                        // add as a local include path
                        entries.add(CoreModel.newIncludeEntry(path, Path.EMPTY, includes[i], false));
                    }
        		}
        		if ((mask & IPathEntry.CDT_MACRO) != 0) {
    				Map<String, String> syms = filePathInfo.getSymbols(path);
    				Set<Entry<String, String>> entrySet = syms.entrySet();
    				for (Entry<String, String> entry : entrySet) {
    					entries.add(CoreModel.newMacroEntry(path, entry.getKey(), entry.getValue()));
    				}
        		}
        		// compare the resource with include and macros files
            	IPath fullResPath = fProject.getWorkspace().getRoot().getFile(path).getLocation();
            	if (fullResPath == null) {
            		fullResPath = path;
            	}
        		if ((mask & IPathEntry.CDT_INCLUDE_FILE) != 0) {
                    IPath[] includeFiles = filePathInfo.getIncludeFiles(path);
                    for (int i = 0; i < includeFiles.length; i++) {
                    	if (!includeFiles[i].equals(fullResPath)) {
                            entries.add(CoreModel.newIncludeFileEntry(path, includeFiles[i]));
                    	}
                    }
                }
                if ((mask & IPathEntry.CDT_MACRO_FILE) != 0) {
                    IPath[] imacrosFiles = filePathInfo.getMacroFiles(path);
                    for (int i = 0; i < imacrosFiles.length; i++) {
                    	if (!imacrosFiles[i].equals(fullResPath)) {
                    		entries.add(CoreModel.newMacroFileEntry(path, imacrosFiles[i]));
                    	}
                    }
                }
            }
        }
        catch (CoreException e) {
            //
        }
		return entries.toArray(new IPathEntry[entries.size()]);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IPathEntryContainerExtension#isEmpty(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public boolean isEmpty(IPath path) {
        IDiscoveredPathInfo info;
		try {
			info = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(fProject);
	        if (info instanceof IPerFileDiscoveredPathInfo) {
	            IPerFileDiscoveredPathInfo filePathInfo = (IPerFileDiscoveredPathInfo) info;
	            return filePathInfo.isEmpty(path);
	        }
		} catch (CoreException e) {
		}
		return false;
	}

}
