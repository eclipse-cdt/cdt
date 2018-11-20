/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *     Marc-Andre Laperle - Moved to an operation for a custom wizard page
 *******************************************************************************/
package org.eclipse.cdt.internal.build.crossgcc;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.templateengine.SharedDefaults;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * An operation that runs when the new project wizard finishes for the Cross GCC toolchain.
 * It reuses the information from {@link SetCrossCommandWizardPage} to set build options (prefix and path).
 * It also clears and reruns scanner discovery to account for the modified command.
 *
 */
public class SetCrossCommandOperation implements IRunnableWithProgress {

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

		String projectName = (String) MBSCustomPageManager.getPageProperty(SetCrossCommandWizardPage.PAGE_ID,
				SetCrossCommandWizardPage.CROSS_PROJECT_NAME);
		String prefix = (String) MBSCustomPageManager.getPageProperty(SetCrossCommandWizardPage.PAGE_ID,
				SetCrossCommandWizardPage.CROSS_COMMAND_PREFIX);
		String path = (String) MBSCustomPageManager.getPageProperty(SetCrossCommandWizardPage.PAGE_ID,
				SetCrossCommandWizardPage.CROSS_COMMAND_PATH);

		SharedDefaults.getInstance().getSharedDefaultsMap().put(SetCrossCommandWizardPage.SHARED_DEFAULTS_PATH_KEY,
				path);
		SharedDefaults.getInstance().getSharedDefaultsMap().put(SetCrossCommandWizardPage.SHARED_DEFAULTS_PREFIX_KEY,
				prefix);
		SharedDefaults.getInstance().updateShareDefaultsMap(SharedDefaults.getInstance().getSharedDefaultsMap());

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists())
			return;

		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null)
			return;

		IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : configs) {
			IToolChain toolchain = config.getToolChain();
			IOption option = toolchain.getOptionBySuperClassId("cdt.managedbuild.option.gnu.cross.prefix"); //$NON-NLS-1$
			ManagedBuildManager.setOption(config, toolchain, option, prefix);
			option = toolchain.getOptionBySuperClassId("cdt.managedbuild.option.gnu.cross.path"); //$NON-NLS-1$
			ManagedBuildManager.setOption(config, toolchain, option, path);
		}

		ManagedBuildManager.saveBuildInfo(project, true);
	}

}
