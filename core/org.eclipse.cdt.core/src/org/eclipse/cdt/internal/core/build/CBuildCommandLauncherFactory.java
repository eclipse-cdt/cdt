/*******************************************************************************
 * Copyright (c) 2017 Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.core.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.ICommandLauncherFactory;
import org.eclipse.cdt.core.build.ICBuildCommandLauncherConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * Core Builder implementation of ICommandLauncherFactory.  Note that for IProjects passed
 * to this command launcher factory, the given project build configuration is checked for
 * the ICBuildConfiguration adapter, and if found is checked for the ICBuildCommandLauncherConfiguration
 * adapter.  If found, then the three methods:  registerLanguageSettings, verifyLanguageSettings,
 * and getCommandLauncher are delegated to the ICBuildCommandLauncherConfiguration.
 *
 */
public class CBuildCommandLauncherFactory
		implements ICommandLauncherFactory {

	public CBuildCommandLauncherFactory() {
		super();
	}

	private <A> Collection<A> getConfigurationAdapters(IProject project,
			Class<A> adapter) {
		List<A> results = new ArrayList<A>();
		try {
			for (IBuildConfiguration config : project.getBuildConfigs()) {
				ICBuildConfiguration cbc = config
						.getAdapter(ICBuildConfiguration.class);
				if (cbc != null) {
					A cconfig = cbc.getAdapter(adapter);
					if (cconfig != null)
						results.add(cconfig);
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(IStatus.ERROR, "Could not get ICBuildConfiguration adapter=" //$NON-NLS-1$
					+ adapter + " for project=" + project); //$NON-NLS-1$
		}
		return results;
	}

	private <A> A getDefaultConfiguration(IProject project,
			Class<A> adapter) {
		Collection<A> configs = getConfigurationAdapters(project, adapter);
		return (configs.size() > 0) ? configs.iterator().next() : null;
	}

	@Override
	public ICommandLauncher getCommandLauncher(IProject project) {
		ICBuildCommandLauncherConfiguration comConfig = getDefaultConfiguration(
				project, ICBuildCommandLauncherConfiguration.class);
		if (comConfig != null)
			return comConfig.getCommandLauncher();
		return null;
	}

	@Override
	public ICommandLauncher getCommandLauncher(
			ICConfigurationDescription cfgd) {
		return getCommandLauncher(cfgd.getProjectDescription().getProject());
	}

	@Override
	public void registerLanguageSettingEntries(IProject project,
			List<? extends ICLanguageSettingEntry> entries) {
		ICBuildCommandLauncherConfiguration comConfig = getDefaultConfiguration(
				project, ICBuildCommandLauncherConfiguration.class);
		if (comConfig != null)
			comConfig.registerLanguageSettingEntries(entries);
	}

	@Override
	public List<ICLanguageSettingEntry> verifyLanguageSettingEntries(
			IProject project, List<ICLanguageSettingEntry> entries) {
		ICBuildCommandLauncherConfiguration comConfig = getDefaultConfiguration(
				project, ICBuildCommandLauncherConfiguration.class);
		List<ICLanguageSettingEntry> results = null;
		if (comConfig != null)
			results = comConfig.verifyLanguageSettingEntries(entries);
		return results;
	}

}
