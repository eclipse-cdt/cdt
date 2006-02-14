/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.actions;

import java.util.*;

import org.eclipse.cdt.managedbuilder.core.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;

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
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo((IProject)iter.next());
			IConfiguration[] configs = info.getManagedProject().getConfigurations();
			int i = 0;
			for (; i < configs.length; i++) {
				if (configs[i].getName().equals(fConfigName)) {
					break;
				}
			}
			if (i != configs.length) {
				info.setDefaultConfiguration(configs[i]);
				info.setSelectedConfiguration(configs[i]);
			}
		}
	}
}
