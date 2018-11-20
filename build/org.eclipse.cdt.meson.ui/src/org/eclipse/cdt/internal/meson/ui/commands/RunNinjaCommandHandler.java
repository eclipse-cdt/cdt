/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.meson.core.MesonBuildConfiguration;
import org.eclipse.cdt.internal.meson.core.MesonUtils;
import org.eclipse.cdt.internal.meson.ui.wizards.RunNinja;
import org.eclipse.cdt.meson.core.IMesonConstants;
import org.eclipse.cdt.meson.ui.Activator;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class RunNinjaCommandHandler extends AbstractMesonCommandHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		return execute1(event);
	}

	@Override
	public void run(Shell shell) {
		// Set up console
		IConsole console = CCorePlugin.getDefault().getConsole();
		IProject project = getSelectedContainer().getAdapter(IProject.class);
		console.start(project);
		try {
			ICBuildConfiguration buildConfig = project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class);

			if (buildConfig instanceof MesonBuildConfiguration) {
				MesonBuildConfiguration config = (MesonBuildConfiguration) buildConfig;
				RunNinja wizard = new RunNinja(buildConfig);
				final WizardDialog dialog = new WizardDialog(shell, wizard);
				Display.getDefault().syncExec(() -> {
					dialog.create();
					dialog.open();
				});
				if (dialog.getReturnCode() == Window.OK) {
					// Run ninja command in a Job so user can cancel if it stalls
					Job buildJob = new Job("Running Ninja") {
						@Override
						public IStatus run(final IProgressMonitor monitor) {

							String envString = config.getProperty(IMesonConstants.NINJA_ENV);
							String[] ninjaEnv = null;
							if (envString != null) {
								ninjaEnv = MesonUtils.stripEnvVars(envString).toArray(new String[0]);
							}

							String argString = config.getProperty(IMesonConstants.NINJA_ARGUMENTS);
							String[] ninjaArgs = null;
							if (argString != null) {
								List<String> ninjaArgList = new ArrayList<>();
								Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(argString);
								while (m.find()) {
									ninjaArgList.add(m.group(1));
								}
								ninjaArgs = ninjaArgList.toArray(new String[0]);
							}
							try {
								config.build(IncrementalProjectBuilder.FULL_BUILD, null, ninjaEnv, ninjaArgs, console,
										monitor);
								if (monitor.isCanceled()) {
									return Status.CANCEL_STATUS;
								}
							} catch (CoreException e) {
								Activator.log(e);
							}
							return Status.OK_STATUS;
						}
					};
					buildJob.schedule();
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

}
