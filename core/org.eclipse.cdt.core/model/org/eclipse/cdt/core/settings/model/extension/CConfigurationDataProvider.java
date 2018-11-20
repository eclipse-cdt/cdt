/*******************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.extension;

import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.IModificationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The class is to be implemented by the Configuration data provider contributed via
 * a org.eclipse.cdt.core.CConfigurationDataProvider extension point
 *
 */
public abstract class CConfigurationDataProvider {
	/**
	 * Requests the Configuration Data to be loaded for the given ConfigurationDescription.
	 * The method can be called in following cases:
	 * <br>
	 * 1. Loading configuration from external data source such as .cproject.<br>
	 * 2. Loading preference configuration defined in Preferences -> C/C++ -> New project Wizard -> Makefile Project.
	 *
	 * @param cfgDescription - configuration description being loaded.
	 * @param monitor - progress monitor.
	 *
	 * @throws CoreException
	 */
	public abstract CConfigurationData loadConfiguration(ICConfigurationDescription cfgDescription,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Requests the Configuration Data to be created for the given ConfigurationDescription.
	 * The method can be called in several cases:
	 * <br>
	 * 1. When the new configuration is being created based upon the already existing one via
	 *    ICProjectDescription.createConfiguration method call.<br>
	 * 2. When the configuration copy (clone) is being created for the copy description.
	 *
	 * @param cfgDescription - configuration description for the configuration data being created.
	 * @param baseCfgDescription - configuration description for the configuration data the new data to be based upon.
	 * @param baseData - configuration data the new data to be based upon.
	 * @param clone - {@code true} indicates that the configuration copy (clone) is being created for the copy description.
	 * @param monitor - progress monitor.
	 *
	 * @return {@code false} indicates that the new configuration is being created based upon the already existing one via
	 *    ICProjectDescription.createConfiguration method call, {@code true} otherwise.
	 *
	 * @throws CoreException
	 */
	public abstract CConfigurationData createConfiguration(ICConfigurationDescription cfgDescription,
			ICConfigurationDescription baseCfgDescription, CConfigurationData baseData, boolean clone,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Called to notify the provider that the configuration is removed.
	 *
	 * @param cfgDescription - configuration description being removed.
	 * @param data - configuration data.
	 * @param monitor - progress monitor.
	 */
	public abstract void removeConfiguration(ICConfigurationDescription cfgDescription, CConfigurationData data,
			IProgressMonitor monitor);

	/**
	 * The method is called in case the implementer does NOT override method
	 * {@link #applyConfiguration(ICConfigurationDescription, ICConfigurationDescription, CConfigurationData, IModificationContext, IProgressMonitor)}.
	 *
	 * @param cfgDescription - configuration description where the configuration data are being applied to.
	 * @param baseCfgDescription - configuration description of the configuration data being applied.
	 * @param baseData - configuration data being applied.
	 *
	 * @throws CoreException
	 */
	public CConfigurationData applyConfiguration(ICConfigurationDescription cfgDescription,
			ICConfigurationDescription baseCfgDescription, CConfigurationData baseData, IProgressMonitor monitor)
			throws CoreException {

		return baseData;
	}

	/**
	 * Called during the setProjectDescription operation to notify the provider that the configuration data
	 * is being applied.
	 * Provider would typically store all the necessary data configuration during this call.
	 *
	 * @param cfgDescription - configuration description where the configuration data are being applied to.
	 * @param baseCfgDescription - configuration description of the configuration data being applied.
	 * @param baseData - configuration data being applied.
	 * @param context the {@link IModificationContext} allows registering workspace runnables to be run
	 *    as a single batch workspace operation.
	 *    If possible the runnables will be run directly in the apply context(thread) after all
	 *    configuration datas get applied. Otherwise runnables will be run as a separate job.
	 *    This allows to perform all workspace modifications registered by different configurations
	 *    to be run as a single batch operation together with the workspace modifications performed by the
	 *    {@link ICProjectDescription} framework.
	 * @param monitor - progress monitor.
	 *
	 * @throws CoreException
	 */
	public CConfigurationData applyConfiguration(ICConfigurationDescription cfgDescription,
			ICConfigurationDescription baseCfgDescription, CConfigurationData baseData, IModificationContext context,
			IProgressMonitor monitor) throws CoreException {

		CConfigurationData data = applyConfiguration(cfgDescription, baseCfgDescription, baseData, monitor);
		if (baseCfgDescription instanceof ILanguageSettingsProvidersKeeper) {
			String[] defaultIds = ((ILanguageSettingsProvidersKeeper) baseCfgDescription)
					.getDefaultLanguageSettingsProvidersIds();
			List<ILanguageSettingsProvider> providers;
			if (defaultIds != null) {
				providers = ((ILanguageSettingsProvidersKeeper) baseCfgDescription).getLanguageSettingProviders();
			} else {
				ICProjectDescription prjDescription = baseCfgDescription.getProjectDescription();
				if (prjDescription != null) {
					IProject project = prjDescription.getProject();
					// propagate the preference to project properties
					ScannerDiscoveryLegacySupport.defineLanguageSettingsEnablement(project);
				}
				defaultIds = ScannerDiscoveryLegacySupport.getDefaultProviderIdsLegacy(baseCfgDescription);
				providers = LanguageSettingsManager.createLanguageSettingsProviders(defaultIds);
			}
			if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
				((ILanguageSettingsProvidersKeeper) cfgDescription).setDefaultLanguageSettingsProvidersIds(defaultIds);
				((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			}
		}
		return data;
	}

	/**
	 * Called to notify that the configuration data was cached. Implementors can do any necessary cleaning, etc.
	 * Default implementation is empty.
	 *
	 * @param cfgDescription - configuration description which was cached.
	 * @param data - configuration data.
	 * @param monitor - progress monitor.
	 */
	public void dataCached(ICConfigurationDescription cfgDescription, CConfigurationData data,
			IProgressMonitor monitor) {
	}
}
