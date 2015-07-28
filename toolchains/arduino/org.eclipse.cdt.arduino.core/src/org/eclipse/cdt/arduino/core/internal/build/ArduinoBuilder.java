/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.build;

import java.util.Map;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoProjectNature;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * This class is responsible for generating the Makefile for the current build
 * config.
 */
public class ArduinoBuilder extends IncrementalProjectBuilder {

	public static final String ID = Activator.getId() + ".arduinoBuilder"; //$NON-NLS-1$

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		IConfiguration config = info.getDefaultConfiguration();

		// TODO if there are references we want to watch, return them here
		return null;
	}

	public static void handleProjectDescEvent(CProjectDescriptionEvent event) {
		try {
			IProject project = event.getProject();
			// Is this an arduino project?
			if (!ArduinoProjectNature.hasNature(project)) {
				return;
			}

			// See if CDT config changed and sync the Resource config
			ICConfigurationDescription newConfigDesc = event.getNewCProjectDescription().getActiveConfiguration();
			ICConfigurationDescription oldConfigDesc = event.getOldCProjectDescription().getActiveConfiguration();
			if (!newConfigDesc.equals(oldConfigDesc)) {
				System.out.println("Active config changed: " + newConfigDesc.getName()); //$NON-NLS-1$
				String configName = newConfigDesc.getName();
				if (project.hasBuildConfig(configName)) {
					IProjectDescription projDesc = project.getDescription();
					projDesc.setActiveBuildConfig(configName);
					project.setDescription(projDesc, new NullProgressMonitor());
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

}
