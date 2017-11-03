/*******************************************************************************
 * Copyright (c) 2006, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.cdt.ui.newui.CDTPropertyManager;

/**
 * Action which changes active build configuration of the current project to 
 * the given one.
 */
public class ChangeConfigAction extends Action {

	private String fConfigName = null;
	protected HashSet<IProject> fProjects = null;
	
	/**
	 * Constructs the action.
	 * @param projects List of selected managed-built projects 
	 * @param configName Build configuration name
	 * @param accel Number to be used as accelerator
	 */
	public ChangeConfigAction(HashSet<IProject> projects, String configName, String displayName, int accel) {
		super("&" + accel + " " + displayName); //$NON-NLS-1$ //$NON-NLS-2$
		fProjects = projects;
		fConfigName = configName;
	}
	
	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		Iterator<IProject> iter = fProjects.iterator();
		while (iter.hasNext()) {
			IProject prj = iter.next();
			boolean changed = false;
			if (CoreModel.getDefault().isNewStyleProject(prj)) {
				ICProjectDescription prjd = CDTPropertyManager.getProjectDescription(prj);
				ICConfigurationDescription[] configs = prjd.getConfigurations();
				if (configs != null && configs.length > 0) {
					for (ICConfigurationDescription config : configs) {
						if (config.getName().equals(fConfigName)) {
							config.setActive();
							CDTPropertyManager.performOk(null);
							AbstractPage.updateViews(prj);
							changed = true;
							break;
						}
					}
				}
			} else {
				try {
					List<IBuildConfiguration> configsToKeep = new ArrayList<>();
					IBuildConfiguration newConfig = null;

					/*
					 * Find the build configuration to change to. Remove the default config if it is
					 * still here.
					 */
					for (IBuildConfiguration config : prj.getBuildConfigs()) {
						if (config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
							continue;
						}

						String[] elems = config.getName().split("/"); //$NON-NLS-1$
						if (elems.length != 2)
							continue;

						configsToKeep.add(config);
						
						String configName = elems[1];
						if (configName.equals(fConfigName)) {
							newConfig = config;
						}
					}

					if (newConfig != null) {
						IProjectDescription descr = prj.getDescription();
						descr.setActiveBuildConfig(newConfig.getName());
						descr.setBuildConfigs(
								configsToKeep.stream().map(c -> c.getName()).toArray(n -> new String[n]));
						prj.setDescription(descr, null);
						changed = true;
					}

				} catch (CoreException e) {
					/*
					 * This should only happen if the project is closed or does not exist. In either
					 * case there is really not much we can do.
					 */
				}
			}
			
			if(!changed)
				CDTPropertyManager.performCancel(null);
		}
	}
}
