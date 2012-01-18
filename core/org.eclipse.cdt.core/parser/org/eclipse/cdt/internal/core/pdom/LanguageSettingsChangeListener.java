/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsChangeEvent;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsChangeListener;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 *	This class handles changes in language settings for the PDOM.
 */
public class LanguageSettingsChangeListener implements ILanguageSettingsChangeListener {
	private PDOMManager fManager;

	/**
	 * Constructor.
	 *
	 * @param manager - PDOM manager.
	 */
	public LanguageSettingsChangeListener(PDOMManager manager) {
		fManager = manager;
	}

	@Override
	public void handleEvent(ILanguageSettingsChangeEvent event) {
		IWorkspaceRoot wspRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = wspRoot.getProject(event.getProjectName());

		if (project != null) {
			ICProjectDescription prjDescription = CCorePlugin.getDefault().getProjectDescription(project);
			if (prjDescription != null) {
				ICConfigurationDescription indexedCfgDescription = prjDescription.getDefaultSettingConfiguration();
				String indexedCfgId = indexedCfgDescription.getId();

				for (String cfgId : event.getConfigurationDescriptionIds()) {
					if (cfgId.equals(indexedCfgId)) {
						fManager.handlePostBuildEvent();
						return;
					}
				}
			}
		}

	}

}
