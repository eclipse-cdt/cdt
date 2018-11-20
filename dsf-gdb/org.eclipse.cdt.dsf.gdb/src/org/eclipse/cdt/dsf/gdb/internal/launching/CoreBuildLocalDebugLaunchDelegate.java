/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.launching;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchConfigDelegate;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.Messages;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.GdbSourceLookupDirector;
import org.eclipse.cdt.dsf.gdb.launching.ServicesLaunchSequence;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;

public class CoreBuildLocalDebugLaunchDelegate extends CoreBuildLaunchConfigDelegate {

	@Override
	public ITargetedLaunch getLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target)
			throws CoreException {
		GdbLaunch launch = new GdbLaunch(configuration, mode, null);
		launch.setLaunchTarget(target);
		launch.initialize();

		GdbSourceLookupDirector locator = new GdbSourceLookupDirector(launch.getSession());
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
		ICBuildConfiguration buildConfig = getBuildConfiguration(configuration, mode, target, monitor);

		Map<String, String> buildEnv = new HashMap<>();
		buildConfig.setBuildEnvironment(buildEnv);
		Properties envProps = new Properties();
		envProps.putAll(buildEnv);
		gdbLaunch.setInitialEnvironment(envProps);

		IToolChain toolChain = buildConfig.getToolChain();
		Path gdbPath = toolChain.getCommandPath(Paths.get("gdb")); //$NON-NLS-1$
		gdbLaunch.setGDBPath(gdbPath != null ? gdbPath.toString() : "gdb"); //$NON-NLS-1$
		String gdbVersion = gdbLaunch.getGDBVersion();

		Path exeFile = Paths.get(getBinary(buildConfig).getLocationURI());
		gdbLaunch.setProgramPath(exeFile.toString());

		gdbLaunch.setServiceFactory(new GdbDebugServicesFactory(gdbVersion, configuration));

		Sequence servicesLaunchSequence = new ServicesLaunchSequence(gdbLaunch.getSession(), gdbLaunch, monitor);
		gdbLaunch.getSession().getExecutor().execute(servicesLaunchSequence);
		try {
			servicesLaunchSequence.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					Messages.CoreBuildLocalDebugLaunchDelegate_FailureLaunching, e));
		}

		gdbLaunch.initializeControl();

		gdbLaunch.addCLIProcess(gdbLaunch.getGDBPath().toOSString() + " (" + gdbVersion + ")"); //$NON-NLS-1$ //$NON-NLS-2$

		Query<Object> ready = new Query<Object>() {
			@Override
			protected void execute(final DataRequestMonitor<Object> rm) {
				DsfServicesTracker tracker = new DsfServicesTracker(
						GdbPlugin.getDefault().getBundle().getBundleContext(), gdbLaunch.getSession().getId());
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
			throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					Messages.CoreBuildLocalDebugLaunchDelegate_FailureStart, e));
		}
	}

}
