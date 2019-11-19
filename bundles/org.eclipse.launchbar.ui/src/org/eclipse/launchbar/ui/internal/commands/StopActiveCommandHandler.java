/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;

public class StopActiveCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		stop();
		return null;
	}

	public void stop() {
		stopBuild();
		stopActiveLaunches(Activator.getService(ILaunchBarManager.class));
	}

	static void stopActiveLaunches(ILaunchBarManager launchBarManager) {
		final ILaunch[] activeLaunches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		if (activeLaunches != null && activeLaunches.length > 0) {
			new Job(Messages.StopActiveCommandHandler_0) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						ILaunchConfiguration activeConfig = launchBarManager.getActiveLaunchConfiguration();
						if (activeConfig == null) {
							return Status.OK_STATUS;
						}
						for (ILaunch launch : activeLaunches) {
							if (launch.canTerminate()) {
								ILaunchConfiguration launchConfig = launch.getLaunchConfiguration();
								if (activeConfig.equals(launchConfig)) {
									launch.terminate();
									continue;
								}
								if (launchConfig instanceof ILaunchConfigurationWorkingCopy) {
									// There are evil delegates that use a
									// working copy for scratch storage
									if (activeConfig
											.equals(((ILaunchConfigurationWorkingCopy) launchConfig).getOriginal())) {
										launch.terminate();
										continue;
									}
								}
							}
						}
						return Status.OK_STATUS;
					} catch (CoreException e) {
						return e.getStatus();
					}
				};
			}.schedule();
		}
	}

	protected void stopBuild() {
		Job job = new Job(Messages.StopActiveCommandHandler_1) {
			@Override
			protected IStatus run(IProgressMonitor progress) {
				// stops all builds
				final IJobManager jobManager = Job.getJobManager();
				Job[] jobs = jobManager.find(ResourcesPlugin.FAMILY_MANUAL_BUILD);
				for (int i = 0; i < jobs.length; i++) {
					Job job = jobs[i];
					job.cancel();
				}
				jobs = jobManager.find(ResourcesPlugin.FAMILY_AUTO_BUILD);
				for (int i = 0; i < jobs.length; i++) {
					Job job = jobs[i];
					job.cancel();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

}
