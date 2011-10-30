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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.language.settings.providers.ILanguageSettingsChangeEvent;
import org.eclipse.cdt.internal.core.language.settings.providers.ILanguageSettingsChangeListener;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 *	This class handles changes in language settings for the PDOM by reindexing the appropriate resources.
 */
public class LanguageSettingsChangeListener implements ILanguageSettingsChangeListener {

	private IIndexManager fManager;
	
	public LanguageSettingsChangeListener(IIndexManager manager) {
		fManager = manager;
	}
	
	public void handleEvent(ILanguageSettingsChangeEvent event) {
		String projectName = event.getProjectName();
		IWorkspaceRoot wspRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = wspRoot.getProject(projectName);
		
		ICProjectDescription prjDescription = CCorePlugin.getDefault().getProjectDescription(project);
		ICConfigurationDescription indexedCfgDescription = prjDescription.getDefaultSettingConfiguration();
		
		CModelManager manager = CModelManager.getDefault();

		IResource[] resources = event.getResources(indexedCfgDescription.getId());
		
		if (resources.length > 0) {
			ICProject cProject = manager.getCModel().getCProject(project);
			List<ICElement> elements = new ArrayList<ICElement>();
			
			for (IResource rc : resources) {
				// AG TODO - remove the log
				CCorePlugin.log(new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID,
						"LanguageSettingsChangeListener"+"["+System.identityHashCode(this)+"]"+".handleEvent() for " + rc, new Exception()));
				
				elements.add(manager.create(rc, cProject));
			}
			
			try {
				fManager.update(elements.toArray(new ICElement[elements.size()]), IIndexManager.UPDATE_ALL);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		
	}

}
