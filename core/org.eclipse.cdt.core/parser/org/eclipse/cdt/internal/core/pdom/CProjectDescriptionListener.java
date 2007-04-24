/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.core.resources.IProject;

public class CProjectDescriptionListener implements	ICProjectDescriptionListener {

	private IIndexManager fIndexManager;

	public CProjectDescriptionListener(IIndexManager manager) {
		fIndexManager= manager;
	}

	public void handleEvent(CProjectDescriptionEvent event) {
		ICProjectDescription old= event.getOldCProjectDescription();
		ICProjectDescription act= event.getNewCProjectDescription();
		if (old != null && act != null) {
			ICConfigurationDescription oldConfig= old.getDefaultSettingConfiguration();
			ICConfigurationDescription newConfig= act.getDefaultSettingConfiguration();
			if (oldConfig != null && newConfig != null) {
				String oldID= oldConfig.getId();
				String newID= newConfig.getId();
				if (oldID != null && newID != null) {
					if (!oldID.equals(newID)) {
						IProject project= event.getProject();
						if (project != null && project.isOpen()) {
							ICProject cproject= CoreModel.getDefault().getCModel().getCProject(project.getName());
							if (cproject != null) {
								fIndexManager.reindex(cproject);
							}
						}
					}
				}
			}
		}
	}
}
