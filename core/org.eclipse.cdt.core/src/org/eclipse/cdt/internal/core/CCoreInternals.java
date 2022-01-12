/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class CCoreInternals {

	public static PDOMManager getPDOMManager() {
		return (PDOMManager) CCorePlugin.getIndexManager();
	}

	/**
	 * Saves the local project preferences, shared project preferences and the
	 * scope preferences for the core plugin.
	 * @param project the project for which to save preferences, may be <code>null</code>
	 * @since 4.0
	 */
	public static void savePreferences(final IProject project, final boolean saveSharedPrefs) {
		Job job = new Job(CCorePlugin.getResourceString("CCoreInternals.savePreferencesJob")) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					if (project != null) {
						new LocalProjectScope(project).getNode(CCorePlugin.PLUGIN_ID).flush();
						if (saveSharedPrefs && project.isOpen()) {
							new ProjectScope(project).getNode(CCorePlugin.PLUGIN_ID).flush();
						}
					}
					InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).flush();
				} catch (BackingStoreException e) {
					CCorePlugin.log(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		if (project != null) {
			// using workspace rule, see bug 240888
			job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		}
		job.schedule();
	}
}
