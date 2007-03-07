/*******************************************************************************
 * Copyright (c) 2006, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.newui.AbstractPage;

/**
 * Action which changes active build configuration of the current project to 
 * the given one.
 */
public class BuildConfigAction extends Action {

	private String fConfigName = null;
	private HashSet fProjects = null;
	
	/**
	 * Constructs the action.
	 * @param projects List of selected managed-built projects 
	 * @param configName Build configuration name
	 * @param accel Number to be used as accelerator
	 */
	public BuildConfigAction(HashSet projects, String configName, String displayName, int accel) {
		super("&" + accel + " " + displayName); //$NON-NLS-1$ //$NON-NLS-2$
		fProjects = projects;
		fConfigName = configName;
	}
	
	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		Iterator iter = fProjects.iterator();
		while (iter.hasNext()) {
			IProject prj = (IProject)iter.next();
			ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(prj, true);
			ICConfigurationDescription[] configs = prjd.getConfigurations(); 
			if (configs != null && configs.length > 0) {
				for (int i = 0; i < configs.length; i++) {
					if (configs[i].getName().equals(fConfigName)) {
						configs[i].setActive();
						try {
							CoreModel.getDefault().setProjectDescription(prj, prjd);
							AbstractPage.updateViews(prj);
						} catch (CoreException e) { }	
						break;
					}
				}
			}
		}
	}
}
