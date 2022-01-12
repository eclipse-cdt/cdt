/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.templateengine.processes;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A configurable stage of the New Project wizard that is able to add implementations of
 * {@link ILanguageSettingsProvider} to the new project's build configurations.
 * E.g.,
 * <pre>
    &lt;process type="org.eclipse.cdt.managedbuilder.core.AddLanguageSettingsProvider">
        &lt;simple name="projectName" value="$(projectName)"/>
        &lt;simple-array name="languageSettingsProviders">
            &lt;element value="org.eclipse.cdt.qt.core.QtPathsProvider"/>
        &lt;/simple-array>
    &lt;/process>
 * </pre>
 *
 * @since 8.3
 */
public class AddLanguageSettingsProvider extends ProcessRunner {

	private static final String PROJECTNAME_VARNAME = "projectName"; //$NON-NLS-1$
	private static final String PROVIDERS_VARNAME = "languageSettingsProviderIds"; //$NON-NLS-1$

	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException {
		IProject project = null;
		String[] extraProviderIds = null;

		for (ProcessArgument arg : args) {
			String argName = arg.getName();
			if (PROJECTNAME_VARNAME.equals(argName))
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(arg.getSimpleValue());
			else if (PROVIDERS_VARNAME.equals(argName))
				extraProviderIds = arg.getSimpleArrayValue();
		}

		if (project == null)
			throw missingArgException(processId, PROJECTNAME_VARNAME);
		if (extraProviderIds == null)
			throw missingArgException(processId, PROVIDERS_VARNAME);

		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		ICProjectDescription des = mngr.getProjectDescription(project, true);
		ICConfigurationDescription[] configDescs = des.getConfigurations();

		for (ICConfigurationDescription configDesc : configDescs)
			if (configDesc instanceof ILanguageSettingsProvidersKeeper) {
				IConfiguration config = ManagedBuildManager.getConfigurationForDescription(configDesc);

				// Create a merged array of the old and new ids
				String[] ids = config.getDefaultLanguageSettingsProviderIds();
				String[] newIds = new String[ids.length + extraProviderIds.length];
				System.arraycopy(ids, 0, newIds, 0, ids.length);
				System.arraycopy(extraProviderIds, 0, newIds, ids.length, extraProviderIds.length);

				ILanguageSettingsProvidersKeeper keeper = (ILanguageSettingsProvidersKeeper) configDesc;
				keeper.setLanguageSettingProviders(LanguageSettingsManager.createLanguageSettingsProviders(newIds));
			}

		try {
			mngr.setProjectDescription(project, des);
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
		}
	}
}
