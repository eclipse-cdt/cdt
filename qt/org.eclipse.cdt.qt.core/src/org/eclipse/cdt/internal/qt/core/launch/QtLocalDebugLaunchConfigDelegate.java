/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.launch;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.sourcelookup.DsfSourceLookupDirector;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.ServicesLaunchSequence;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.qt.core.QtBuildConfiguration;
import org.eclipse.cdt.qt.core.QtLaunchConfigurationDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;

public class QtLocalDebugLaunchConfigDelegate extends QtLaunchConfigurationDelegate {

	@Override
	public ITargetedLaunch getLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target)
			throws CoreException {
		GdbLaunch launch = new GdbLaunch(configuration, mode, null);
		launch.setLaunchTarget(target);
		launch.initialize();

		DsfSourceLookupDirector locator = new DsfSourceLookupDirector(launch.getSession());
		String memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String) null);
		if (memento == null) {
			locator.initializeDefaults(configuration);
		} else {
			locator.initializeFromMemento(memento, configuration);
		}

		launch.setSourceLocator(locator);
		return launch;
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		GdbLaunch gdbLaunch = (GdbLaunch) launch;
		ILaunchTarget target = ((ITargetedLaunch) launch).getLaunchTarget();
		QtBuildConfiguration qtBuildConfig = getQtBuildConfiguration(configuration, mode, target, monitor);

		// TODO get it from the toolchain
		gdbLaunch.setGDBPath("/usr/local/bin/gdb");
		String gdbVersion = gdbLaunch.getGDBVersion();

		Path exeFile = qtBuildConfig.getProgramPath();
		gdbLaunch.setProgramPath(exeFile.toString());

		gdbLaunch.setServiceFactory(new GdbDebugServicesFactory(gdbVersion, configuration));

		Sequence servicesLaunchSequence = new ServicesLaunchSequence(gdbLaunch.getSession(), gdbLaunch, monitor);
		gdbLaunch.getSession().getExecutor().execute(servicesLaunchSequence);
		try {
			servicesLaunchSequence.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new DebugException(new Status(IStatus.ERROR, Activator.ID, "Failure launching with gdb", e));
		}

		gdbLaunch.initializeControl();

		gdbLaunch.addCLIProcess(gdbLaunch.getGDBPath().toOSString() + " (" + gdbVersion + ")"); //$NON-NLS-1$ //$NON-NLS-2$

		Query<Object> ready = new Query<Object>() {
			@Override
			protected void execute(final DataRequestMonitor<Object> rm) {
				DsfServicesTracker tracker = new DsfServicesTracker(
						Activator.getDefault().getBundle().getBundleContext(), gdbLaunch.getSession().getId());
				IGDBControl control = tracker.getService(IGDBControl.class);
				tracker.dispose();
				control.completeInitialization(
						new RequestMonitorWithProgress(ImmediateExecutor.getInstance(), monitor) {
					@Override
					protected void handleCompleted() {
						if (isCanceled()) {
							rm.cancel();
						} else {
							rm.setStatus(getStatus());
						}
						rm.done();
					}
				});
			}
		};

		// Start it up
		gdbLaunch.getSession().getExecutor().execute(ready);
		try {
			ready.get();
		} catch (ExecutionException | InterruptedException e) {
			throw new DebugException(new Status(IStatus.ERROR, Activator.ID, "Failure to start debug session", e));
		}
	}

}
