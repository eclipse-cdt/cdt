/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.ctags;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Doug Schaefer
 */
public class CtagsIndexer implements IPDOMIndexer {

	private PDOM pdom;
	
	private boolean useCtagsOnPath = true;
	private String ctagsCommand = ""; //$NON-NLS-1$
	private boolean useInternalCtagsFile = true;
	private String ctagsFileName = ""; //$NON-NLS-1$
	
	public void handleDelta(ICElementDelta delta) {
		// TODO Auto-generated method stub
	}

	public void reindex() throws CoreException {
		new CtagsReindex(this).schedule();
	}

	public void setPDOM(IPDOM pdom) {
		this.pdom = (PDOM)pdom;
		loadPreferences();
	}

	public IPDOM getPDOM() {
		return pdom;
	}
	
	// Preference Management
	private static final String useCtagsOnPathId = "useCtagsOnPath"; //$NON-NLS-1$
	private static final String ctagsCommandId = "ctagsCommand"; //$NON-NLS-1$
	private static final String useInternalCtagsFileId = "useInternalCtagsFile"; //$NON-NLS-$
	private static final String ctagsFileNameId = "ctagsFileName"; //$NON-NLS-1$

	// project preferences
	private void loadPreferences() {
		IProject project = pdom.getProject().getProject();
    	IEclipsePreferences prefs = new ProjectScope(project.getProject()).getNode(CCorePlugin.PLUGIN_ID);
		if (prefs == null)
			return;
		
		useCtagsOnPath = prefs.getBoolean(useCtagsOnPathId, getDefaultUseCtagsOnPath());
		ctagsCommand = prefs.get(ctagsCommandId, getDefaultCtagsCommand());
		useInternalCtagsFile = prefs.getBoolean(useInternalCtagsFileId, getDefaultUseInternalCtagsFile());
		ctagsFileName = prefs.get(ctagsFileNameId, getDefaultCtagsFileName());
	}

	public void setPreferences(
			boolean useCtagsOnPath,
			String ctagsCommand,
			boolean useInternalCtagsFile,
			String ctagsFileName) {
		
		IProject project = pdom.getProject().getProject();
    	IEclipsePreferences prefs = new ProjectScope(project.getProject()).getNode(CCorePlugin.PLUGIN_ID);
		if (prefs == null)
			return;
		
		boolean changed = false;
		if (this.useCtagsOnPath != useCtagsOnPath) {
			this.useCtagsOnPath = useCtagsOnPath;
			prefs.putBoolean(useCtagsOnPathId, useCtagsOnPath);
			changed = true;
		}

		if (! this.ctagsCommand.equals(ctagsCommand)) {
			this.ctagsCommand = ctagsCommand;
			prefs.put(ctagsCommandId, ctagsCommand);
			changed = true;
		}
		
		if (this.useInternalCtagsFile != useInternalCtagsFile) {
			this.useInternalCtagsFile = useInternalCtagsFile;
			prefs.putBoolean(useInternalCtagsFileId, useInternalCtagsFile);
			changed = true;
		}

		if (! this.ctagsFileName.equals(ctagsFileName)) {
			this.ctagsFileName = ctagsFileName;
			prefs.put(ctagsFileNameId, ctagsFileName);
			changed = true;
		}
		
		if (changed) {
			try {
				prefs.flush();
			} catch (BackingStoreException e) {
	    		CCorePlugin.log(e);
			}
		}
		
	}
	
	public boolean useCtagsOnPath() {
		return useCtagsOnPath;
	}
	
	public String getCtagsCommand() {
		return ctagsCommand;
	}
	
	public String getResolvedCtagsCommand() {
		return useCtagsOnPath ? "ctags" : ctagsCommand; //$NON-NLS-1
	}
	
	public boolean useInternalCtagsFile() {
		return useInternalCtagsFile;
	}
	
	public String getCtagsFileName() {
		return ctagsFileName;
	}

	public String getResolvedCtagsFileName() {
		if (useInternalCtagsFile)
			return CCorePlugin.getDefault().getStateLocation().append(pdom.getProject().getElementName() + ".ctags").toOSString(); //$NON-NLS-1$
		else
			return ctagsFileName;
	}
	
	// Defaults stored in metadata
	public static boolean getDefaultUseCtagsOnPath() {
    	IPreferencesService prefService = Platform.getPreferencesService();
    	return prefService.getBoolean(CCorePlugin.PLUGIN_ID, useCtagsOnPathId,
    			true, null);
	}
	
	public static String getDefaultCtagsCommand() {
    	IPreferencesService prefService = Platform.getPreferencesService();
    	return prefService.getString(CCorePlugin.PLUGIN_ID, ctagsCommandId,
    			"", null); //$NON-NLS-1$
	}
	
	public static boolean getDefaultUseInternalCtagsFile() {
    	IPreferencesService prefService = Platform.getPreferencesService();
    	return prefService.getBoolean(CCorePlugin.PLUGIN_ID, useInternalCtagsFileId,
    			true, null);
	}
	
	public static String getDefaultCtagsFileName() {
    	IPreferencesService prefService = Platform.getPreferencesService();
    	return prefService.getString(CCorePlugin.PLUGIN_ID, ctagsFileNameId,
    			"", null); //$NON-NLS-1$
	}
	
	public static void setDefaultPreferences(
			boolean useCtagsOnPath,
			String ctagsCommand,
			boolean useInternalCtagsFile,
			String ctagsFileName) {
		
    	IEclipsePreferences prefs = new InstanceScope().getNode(CCorePlugin.PLUGIN_ID);
    	if (prefs == null)
    		return;
    	
    	prefs.putBoolean(useCtagsOnPathId, useCtagsOnPath);
    	prefs.put(ctagsCommandId, ctagsCommand);
    	prefs.putBoolean(useInternalCtagsFileId, useInternalCtagsFile);
    	prefs.put(ctagsFileNameId, ctagsFileName);
    	
    	try {
    		prefs.flush();
    	} catch (BackingStoreException e) {
    		CCorePlugin.log(e);
    	}
	}
	
}
