/*******************************************************************************
 * Copyright (c) 2005, 2014 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     Anna Dushistova (MontaVista) - [366771] Converter fails to convert a CDT makefile project
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.ui.util.AbstractResourceActionHandler;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class ConvertTargetHandler extends AbstractResourceActionHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShellChecked(event);

		IProject project;
		IStructuredSelection selection = getSelection(event);
		if (selection.size() != 1)
			return null;

		Object obj = selection.getFirstElement();
		if (!(obj instanceof IProject))
			return null;

		project = (IProject) obj;

		// Check whether the converters available for the selected project.
		// If there are no converters display error dialog otherwise display converters list.
		if (hasTargetConverters(project)) {
			handleConvertTargetAction(project, shell);
		} else {
			MessageDialog.openError(shell, Messages.ConvertTargetHandler_No_Converter, NLS
					.bind(Messages.ProjectConvert_noConverterErrorDialog_message, new String[] { project.getName() }));
		}
		return null;
	}

	public static boolean hasTargetConverters(IProject project) {
		return ManagedBuildManager.hasTargetConversionElements(getProjectType(project))
				|| ManagedBuildManager.hasAnyTargetConversionElements(getProjectToolchains(project));
	}

	private static IProjectType getProjectType(IProject project) {
		IProjectType projectType = null;

		// Get the projectType from project.
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		if (info != null) {
			IManagedProject managedProject = info.getManagedProject();
			if (managedProject != null)
				projectType = managedProject.getProjectType();
		}
		return projectType;
	}

	private static List<IBuildObject> getProjectToolchains(IProject project) {
		List<IBuildObject> projectToolchains = new ArrayList<>();

		// Get the projectType from project.
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		if (info != null) {
			IConfiguration[] configs = info.getManagedProject().getConfigurations();
			for (IConfiguration config : configs) {
				IToolChain toolchain = config.getToolChain();
				if (toolchain != null) {
					projectToolchains.add(toolchain);
				}
			}
		}
		return projectToolchains;
	}

	private static void handleConvertTargetAction(IProject project, Shell shell) {
		String title = NLS.bind(Messages.ProjectConvert_title, project.getName());
		ConvertTargetDialog dialog = new ConvertTargetDialog(shell, project, title);
		if (dialog.open() != ConvertTargetDialog.OK)
			return;
		if (!ConvertTargetDialog.isConversionSuccessful()) {
			MessageDialog.openError(shell, Messages.ProjectConvert_conversionErrordialog_title,
					NLS.bind(Messages.ProjectConvert_conversionErrordialog_message, project.getName()));
		}
	}
}
