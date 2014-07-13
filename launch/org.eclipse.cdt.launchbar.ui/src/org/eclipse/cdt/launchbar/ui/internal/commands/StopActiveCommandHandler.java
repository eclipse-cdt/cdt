/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.ui.internal.commands;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.ui.internal.Activator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;

public class StopActiveCommandHandler extends AbstractHandler {
	private ILaunchBarManager launchBarManager;

	public StopActiveCommandHandler() {
		launchBarManager = Activator.getService(ILaunchBarManager.class);
	}

	@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
		stop();
		return null;
    }

	public void stop() {
		stopBuild();
		stopActiveLaunches();
	}

	protected void stopActiveLaunches() {
		final ILaunch[] activeLaunches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		if (activeLaunches != null && activeLaunches.length > 0) {
			new Job("Stopping launches") {
				protected IStatus run(IProgressMonitor monitor) {
					// TODO only stop the launches for the active launch descriptor
					// Not sure we have the API to map that out yet.
					for (ILaunch launch : activeLaunches) {
						try {
							launch.terminate();
						} catch (DebugException e) {
							return e.getStatus();
						}
					}
					return Status.OK_STATUS;
				};
			}.schedule();
		}
	}

	protected void stopBuild() {
		Job job = new Job("Stopping build") {
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
