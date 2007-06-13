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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.core.resources.IProject;

public class CProjectDescriptionListener implements	ICProjectDescriptionListener {

	private PDOMManager fIndexManager;

	public CProjectDescriptionListener(PDOMManager manager) {
		fIndexManager= manager;
	}

	public void handleEvent(CProjectDescriptionEvent event) {
		ICProjectDescription old= event.getOldCProjectDescription();
		ICProjectDescription act= event.getNewCProjectDescription();
		if (act != null) {
			if (completedProjectCreation(old, act)) {
				ICProject project= getProject(event);
				if (project != null) {
					fIndexManager.addProject(project);
				}
			}
			else if (old != null && changedDefaultSettingConfiguration(old, act)) {
				ICProject project= getProject(event);
				if (project != null) {
					fIndexManager.reindex(project);
				}
			}
		}
	}

	private boolean changedDefaultSettingConfiguration(ICProjectDescription old, ICProjectDescription act) {
		ICConfigurationDescription oldConfig= old.getDefaultSettingConfiguration();
		ICConfigurationDescription newConfig= act.getDefaultSettingConfiguration();
		if (oldConfig != null && newConfig != null) {
			String oldID= oldConfig.getId();
			String newID= newConfig.getId();
			if (oldID != null && newID != null) {
				if (!oldID.equals(newID)) {
					return true;
				}
			}
		}
		return false;
	}

	private ICProject getProject(CProjectDescriptionEvent event) {
		IProject project= event.getProject();
		if (project != null && project.isOpen()) {
			return CoreModel.getDefault().create(project);
		}
		return null;
	}

	private boolean completedProjectCreation(ICProjectDescription old, ICProjectDescription act) {
		return (old == null || old.isCdtProjectCreating()) && !act.isCdtProjectCreating();
	}
}
