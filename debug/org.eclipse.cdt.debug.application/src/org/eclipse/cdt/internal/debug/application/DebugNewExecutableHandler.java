/*******************************************************************************
 * Copyright (c) 2012, 2014 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 * Red Hat Inc. - modified for use in Standalone Debugger
 *******************************************************************************/

package org.eclipse.cdt.internal.debug.application;

import org.eclipse.cdt.debug.application.Activator;
import org.eclipse.cdt.debug.application.Messages;
import org.eclipse.cdt.debug.application.NewExecutableDialog;
import org.eclipse.cdt.debug.application.NewExecutableInfo;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DebugNewExecutableHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		NewExecutableDialog dialog = new NewExecutableDialog(new Shell());

		if (dialog.open() == IDialogConstants.OK_ID) {
			NewExecutableInfo info = dialog.getExecutableInfo();
			String executable = info.getHostPath();
			String arguments = info.getArguments();
			String buildLog = info.getBuildLog();

			try {
				final ILaunchConfiguration config = DebugExecutable
						.importAndCreateLaunchConfig(new NullProgressMonitor(), executable, buildLog, arguments, false);
				if (config != null) {
					final JobContainer LaunchJobs = new JobContainer();
					Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {

						@Override
						public void scheduled(IJobChangeEvent event) {
							Job job = event.getJob();
							if (job.getName().contains(config.getName()))
								LaunchJobs.setLaunchJob(job);
						}

						@Override
						public void done(IJobChangeEvent event) {
						}
					});
					Display.getDefault().syncExec(() -> DebugUITools.launch(config, ILaunchManager.DEBUG_MODE));
					if (LaunchJobs.getLaunchJob() != null) {
						try {
							LaunchJobs.getLaunchJob().join();
						} catch (InterruptedException e) {
							IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
									Messages.LaunchInterruptedError, e);
							ResourcesPlugin.getPlugin().getLog().log(status);
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}

		return null;
	}

}
