/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOM {

    private static final String PDOM_NODE = CCorePlugin.PLUGIN_ID + ".pdom"; //$NON-NLS-1$ 
    private static final String ENABLED_KEY = "enabled"; //$NON-NLS-1$
    	
    private static IEclipsePreferences getPreferences(IProject project) {
    	IScopeContext projectScope = new ProjectScope(project);
    	if (projectScope == null)
    		return null;
    	else
    		return projectScope.getNode(PDOM_NODE);
    }
    
    public static boolean isEnabled(IProject project) {
    	IEclipsePreferences prefs = getPreferences(project);
    	if (prefs == null)
    		return false;
    	else
    		return prefs.getBoolean(ENABLED_KEY, false);
    }
    
    public static void setEnabled(IProject project, boolean enabled) {
    	IEclipsePreferences prefs = getPreferences(project);
    	if (prefs == null)
    		return;
    	
    	prefs.putBoolean(ENABLED_KEY, enabled);
    	try {
    		prefs.flush();
    	} catch (BackingStoreException e) {
    	}
    }

    public static boolean isInitialized(IProject project) {
    	return getPreferences(project) != null;
    }
    
    /**
     * Get the PDOM for the given project.
     * 
     * @param project
     * @return the PDOM for the project
     */
	public static IPDOM getPDOM(IProject project) {
		if (isEnabled(project))
			return PDOMManager.getInstance().getPDOM(project);
		else
			return null;
	}

	public static void deletePDOM(IProject project) throws CoreException {
		if (isEnabled(project))
			PDOMManager.getInstance().getPDOM(project).delete();
	}
	
	/**
	 * Startup the PDOM. This mainly sets us up to handle model
	 * change events.
	 */
	public static void startup() {
		IElementChangedListener listener = PDOMManager.getInstance();
		if (listener != null) {
			CoreModel.getDefault().addElementChangedListener(listener);
		}
	}

}
