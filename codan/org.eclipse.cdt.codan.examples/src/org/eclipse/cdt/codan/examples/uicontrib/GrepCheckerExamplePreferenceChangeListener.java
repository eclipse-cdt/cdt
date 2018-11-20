/*******************************************************************************
 * Copyright (c) 2009,2016 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.examples.uicontrib;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.examples.Activator;
import org.eclipse.cdt.codan.examples.checkers.GrepChecker;
import org.eclipse.cdt.codan.internal.core.CodanPreferencesLoader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

/**
 * Example of property change listener for changing error profiles using eclipse preferences.
 * Too see debug prints uncomment printout line in {@link #trace(String) function }
 * In this examples plugin this listener is activated on startup by calling ProfileChangeListener.getInstance(), see
 * {@link Activator#start(org.osgi.framework.BundleContext)}
 * Note: this example will not properly listen on properties for new or deleted projects
 */
public class GrepCheckerExamplePreferenceChangeListener implements INodeChangeListener, IPreferenceChangeListener {
	static GrepCheckerExamplePreferenceChangeListener instance;

	public static GrepCheckerExamplePreferenceChangeListener getInstance() {
		if (instance == null)
			instance = new GrepCheckerExamplePreferenceChangeListener();
		return instance;
	}

	private IProject project;

	private GrepCheckerExamplePreferenceChangeListener(IProject project) {
		this.project = project;
	}

	private GrepCheckerExamplePreferenceChangeListener() {
		CodanCorePlugin.getDefault().getStorePreferences().addNodeChangeListener(this);
		CodanCorePlugin.getDefault().getStorePreferences().addPreferenceChangeListener(this);
		IWorkspace root = ResourcesPlugin.getWorkspace();
		IProject[] projects = root.getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			IEclipsePreferences prefs = CodanPreferencesLoader.getProjectNode(project);
			if (prefs != null)
				prefs.addPreferenceChangeListener(new GrepCheckerExamplePreferenceChangeListener(project));
		}
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getSource() instanceof IEclipsePreferences) {
			//IEclipsePreferences ep = (IEclipsePreferences) event.getSource();
			if (GrepChecker.ID.equals(event.getKey())) {
				// severity or enablement has changed
				String val = (String) event.getNewValue();
				String fors = (" for " + ((project == null) ? "workspace" : project.getName()));
				if (val != null && !val.startsWith("-")) {
					trace("grep checker enabled :)" + fors);
				} else {
					trace("grep checker disabled :(" + fors);
				}

			}
		}
	}

	private void trace(String message) {
		// NOTE: uncomment this to see what this guy is listening on
		//	System.err.print("example codan pref listener: " + message);
	}

	@Override
	public void added(NodeChangeEvent event) {
		trace("node added " + event);
	}

	@Override
	public void removed(NodeChangeEvent event) {
		trace("node removed " + event);
	}

	/**
	 *
	 */
	public void dispose() {
		CodanCorePlugin.getDefault().getStorePreferences().removeNodeChangeListener(this);
		CodanCorePlugin.getDefault().getStorePreferences().removePreferenceChangeListener(this);
	}
}
