/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.ui.workingsets;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.ui.workingsets.IWorkingSetConfiguration;
import org.eclipse.cdt.internal.ui.workingsets.WorkingSetProjectConfiguration;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;

/**
 * A managed-build implementation of the working set project configuration. It
 * knows how to build the selected configuration without activating it.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 */
class MBSProjectConfiguration extends WorkingSetProjectConfiguration {

	/**
	 * Initializes me.
	 * 
	 * @param parent
	 *            my parent working set configuration
	 */
	public MBSProjectConfiguration(IWorkingSetConfiguration parent) {
		super(parent);
	}

	@Override
	public IStatus build(IProgressMonitor monitor) {
		return MBSProjectConfiguration.build(resolveProject(),
				resolveSelectedConfiguration(), monitor);
	}

	/**
	 * Builds the MBS configuration selected by the specified working set
	 * project configuration.
	 * 
	 * @param projectConfig
	 *            a project configuration to build
	 * @param monitor
	 *            for reporting build progress
	 * 
	 * @return the result of the MBS build
	 */
	static IStatus build(IProject project, ICConfigurationDescription config,
			IProgressMonitor monitor) {

		IStatus result = Status.OK_STATUS;

		IConfiguration mbsConfig = (config == null) ? null
				: ManagedBuildManager.getConfigurationForDescription(config);

		if (mbsConfig == null) {
			result = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, NLS.bind(
					"No configuration selected for project \"{0}\".", project
							.getName()));
		} else {
			monitor = SubMonitor.convert(monitor);

			try {
				ManagedBuildManager.buildConfigurations(
						new IConfiguration[] { mbsConfig }, monitor);
			} catch (CoreException e) {
				result = e.getStatus();
			}
		}

		return result;
	}
}
