/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class CCoreInternals {

	private static final String PREFS_FILE_EXTENSION = ".prefs"; //$NON-NLS-1$
	private static final String SETTINGS_DIRECTORY_NAME = ".settings"; //$NON-NLS-1$

	public static PDOMManager getPDOMManager() {
		return (PDOMManager) CCorePlugin.getIndexManager();
	}
	
	/**
	 * Saves the local project preferences, shared project preferences and the
	 * scope preferences for the core plugin. 
	 * @param project the project for which to save preferences, may be <code>null</code>
	 * @since 4.0
	 */
	public static void savePreferences(final IProject project) {
    	Job job= new Job(CCorePlugin.getResourceString("CCoreInternals.savePreferencesJob")) { //$NON-NLS-1$
        	protected IStatus run(IProgressMonitor monitor) {
        		try {
        			if (project != null) {
    					new LocalProjectScope(project).getNode(CCorePlugin.PLUGIN_ID).flush();
    					new ProjectScope(project).getNode(CCorePlugin.PLUGIN_ID).flush();
        			}
        			new InstanceScope().getNode(CCorePlugin.PLUGIN_ID).flush();
				} catch (BackingStoreException e) {
					CCorePlugin.log(e);
				}
       	    	return Status.OK_STATUS;
        	}
    	};
    	job.setSystem(true);
    	if (project != null) {
    		IResourceRuleFactory rf= ResourcesPlugin.getWorkspace().getRuleFactory();
    		IFile wsFile= project.getFile(new Path(SETTINGS_DIRECTORY_NAME).append(CCorePlugin.PLUGIN_ID + PREFS_FILE_EXTENSION));
    		ISchedulingRule[] rules= {
    				rf.modifyRule(wsFile), 
    				rf.createRule(wsFile.getParent()),
    				rf.createRule(wsFile), 
    				rf.deleteRule(wsFile) 
    		};
    		job.setRule(MultiRule.combine(rules));
    	}
    	job.schedule(2000);
	}
}
