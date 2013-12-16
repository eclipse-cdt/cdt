/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.codan.examples.uicontrib;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.core.model.IProblemProfileChangeListener;
import org.eclipse.cdt.codan.core.model.ProblemProfileChangeEvent;
import org.eclipse.cdt.codan.examples.checkers.GrepChecker;
import org.eclipse.cdt.codan.internal.core.CodanPreferencesLoader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

/**
 * Example of property change listener for changing error profiles
 */
public class ProfileChangeListener implements INodeChangeListener, IPreferenceChangeListener, IProblemProfileChangeListener {
	
	static ProfileChangeListener instance = new ProfileChangeListener();
	
	public static ProfileChangeListener getInstance(){
		return instance;
	}
	private IProject project;
	private ProfileChangeListener(IProject project) {
		this.project = project;
	}
	private ProfileChangeListener() {
		CodanCorePlugin.getDefault().getStorePreferences().addNodeChangeListener(this);
		CodanCorePlugin.getDefault().getStorePreferences().addPreferenceChangeListener(this);
		 IWorkspace root = ResourcesPlugin.getWorkspace();
		 IProject[] projects = root.getRoot().getProjects();
		 for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			IEclipsePreferences prefs = CodanPreferencesLoader.getProjectNode(project);
			if (prefs != null)
				prefs.addPreferenceChangeListener(new ProfileChangeListener(project));
		}
       // cannot do on plugin startup
	   // CheckersRegistry.getInstance().getWorkspaceProfile().addProfileChangeListener(this);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
	 */
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getSource() instanceof IEclipsePreferences) {
			IEclipsePreferences ep =  (IEclipsePreferences) event.getSource(); 
			if (project!=null) {

				if (GrepChecker.ID.equals(event.getKey())) {
					// severity or enablement has changed
					String val = (String) event.getNewValue();
					if (!val.startsWith("-")) {
						System.err.println("grep checker enabled!");
					}
				}
			
			}
		}

		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener#added(org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent)
	 */
	public void added(NodeChangeEvent event) {
		System.err.println("node added "+event);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener#removed(org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent)
	 */
	public void removed(NodeChangeEvent event) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 
	 */
	public void dispose() {
		CodanCorePlugin.getDefault().getStorePreferences().removeNodeChangeListener(this);
		CodanCorePlugin.getDefault().getStorePreferences().removePreferenceChangeListener(this);
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.codan.internal.core.CheckersRegistry.IProblemProfileChangeListener#profileChange(org.eclipse.cdt.codan.internal.core.CheckersRegistry.ProblemProfileChangeEvent)
	 */
	public void profileChange(ProblemProfileChangeEvent event) {
		if (event.getKey().equals(ProblemProfileChangeEvent.PROBLEM_KEY)) {
			IResource resource = (IResource) event.getSource();
			IProblemProfile profile = (IProblemProfile) event.getNewValue();
			IProblem pp = profile.findProblem(GrepChecker.ID);
			System.err.println(pp.getName() + " enabled "+ pp.isEnabled());
		}
		
	}
}
