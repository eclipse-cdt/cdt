/*******************************************************************************
 * Copyright (c) 2011, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsChangeEvent;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsChangeListener;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

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
			ICProjectDescription prjDescription = CCorePlugin.getDefault().getProjectDescription(project, false);
			if (prjDescription != null) {
				// cfgDescription being indexed
				ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
				String indexedId = cfgDescription.getId();

				for (String id : event.getConfigurationDescriptionIds()) {
					if (id.equals(indexedId)) {
						reindex(id, event);
						return;
					}
				}
			}
		}
	}

	private void reindex(String cfgId, ILanguageSettingsChangeEvent event) {
		CModelManager manager = CModelManager.getDefault();
		ICProject cProject = manager.getCModel().getCProject(event.getProjectName());
		Set<ICElement> tuSelection = new HashSet<>();

		Set<IResource> resources = event.getAffectedResources(cfgId);
		if (resources != null && !resources.isEmpty()) {
			for (IResource rc : resources) {
				tuSelection.add(manager.create(rc, cProject));
			}

			try {
				fManager.update(tuSelection.toArray(new ICElement[tuSelection.size()]), IIndexManager.UPDATE_ALL);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
	}
}
