/*******************************************************************************
 * Copyright (c) 2010, 2014 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.commands;

import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.ui.util.AbstractResourceActionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;

/**
 *  Implementation of the command that builds all configurations of the selected projects.
 */
public class BuildAllConfigurationsHandler extends AbstractResourceActionHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<IProject> projects = RebuildConfigurationsHandler.getSelectedCdtProjects(getSelection(event));
		if (!projects.isEmpty()) {
			// Setup the global build console.
			CUIPlugin.getDefault().startGlobalConsole();

			for (IProject project : projects) {
				ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project, false);
				if (projectDescription != null) {
					ICConfigurationDescription[] cfgds = projectDescription.getConfigurations();
					if (cfgds != null && cfgds.length > 0) {
						// Save all dirty editors.
						BuildUtilities.saveEditors(null);

						Job buildJob = new BuildConfigurationsJob(cfgds, 0,
								IncrementalProjectBuilder.INCREMENTAL_BUILD);
						buildJob.schedule();
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		super.setEnabled(evaluationContext);
		setBaseEnabled(!RebuildConfigurationsHandler.getSelectedCdtProjects(getSelection()).isEmpty());
	}
}
