/*******************************************************************************
 * Copyright (c) 2002 - 2004 QNX Software Systems and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.launch.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;

public class LocalRunLaunchDelegate extends AbstractCLaunchDelegate {

	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(LaunchMessages.getString("LocalRunLaunchDelegate.Launching_Local_C_Application"), 10); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.worked(1);
			IPath exePath = verifyProgramPath(config);
			ICProject project = verifyCProject(config);
			IBinaryExecutable exeFile = createBinary(project, exePath);
			String arguments[] = getProgramArgumentsArray(config);

			// set the default source locator if required
			setDefaultSourceLocator(launch, config);

			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				ICDebugConfiguration debugConfig = getDebugConfig(config);
				ICDISession dsession = null;
				String debugMode = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
														ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
				if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
					dsession = debugConfig.createDebugger().createDebuggerSession(launch, exeFile, new SubProgressMonitor(monitor, 8));
					try {
						ICDITarget[] dtargets = dsession.getTargets();
						for (int i = 0; i < dtargets.length; ++i) {
							ICDIRuntimeOptions opt = dtargets[i].getRuntimeOptions();
							opt.setArguments(arguments);
							File wd = getWorkingDirectory(config);
							if (wd != null) {
								opt.setWorkingDirectory(wd.getAbsolutePath());
							}
							opt.setEnvironment(expandEnvironment(config));
						}
					} catch (CDIException e) {
						try {
							dsession.terminate();
						} catch (CDIException ex) {
							// ignore
						}
						abort(LaunchMessages.getString("LocalRunLaunchDelegate.Failed_setting_runtime_option_though_debugger"), e, //$NON-NLS-1$
								ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
					}
					monitor.worked(1);
					try {
						boolean stopInMain = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);

						ICDITarget[] targets = dsession.getTargets();
						for (int i = 0; i < targets.length; i++) {
							Process process = targets[i].getProcess();
							IProcess iprocess = null;
							if (process != null) {
								iprocess = DebugPlugin.newProcess(launch, process, renderProcessLabel(exePath.toOSString()));
							}
							CDIDebugModel.newDebugTarget(launch, project.getProject(), targets[i], renderTargetLabel(debugConfig),
														iprocess, exeFile, true, false, stopInMain, true);
						}
					} catch (CoreException e) {
						try {
							dsession.terminate();
						} catch (CDIException e1) {
							// ignore
						}
						throw e;
					}
				}
			} else {
				File wd = getWorkingDirectory(config);
				if (wd == null) {
					wd = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				ArrayList command = new ArrayList(1 + arguments.length);
				command.add(exePath.toOSString());
				command.addAll(Arrays.asList(arguments));
				String[] commandArray = (String[])command.toArray(new String[command.size()]);
				monitor.worked(5);
				Process process = exec(commandArray, getEnvironmentProperty(config), wd);
				monitor.worked(3);
				DebugPlugin.newProcess(launch, process, renderProcessLabel(commandArray[0]));
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Performs a runtime exec on the given command line in the context of the
	 * specified working directory, and returns the resulting process. If the
	 * current runtime does not support the specification of a working
	 * directory, the status handler for error code
	 * <code>ERR_WORKING_DIRECTORY_NOT_SUPPORTED</code> is queried to see if
	 * the exec should be re-executed without specifying a working directory.
	 * 
	 * @param cmdLine
	 *            the command line
	 * @param workingDirectory
	 *            the working directory, or <code>null</code>
	 * @return the resulting process or <code>null</code> if the exec is
	 *         cancelled
	 * @see Runtime
	 */
	protected Process exec(String[] cmdLine, Properties environ, File workingDirectory) throws CoreException {
		Process p = null;
		Properties props = getDefaultEnvironment();
		props.putAll(expandEnvironment(environ));
		String[] envp = null;
		ArrayList envList = new ArrayList();
		Enumeration names = props.propertyNames();
		if (names != null) {
			while (names.hasMoreElements()) {
				String key = (String)names.nextElement();
				envList.add(key + "=" + props.getProperty(key)); //$NON-NLS-1$
			}
			envp = (String[])envList.toArray(new String[envList.size()]);
		}
		try {

			if (workingDirectory == null) {
				p = ProcessFactory.getFactory().exec(cmdLine, envp);
			} else {
				p = ProcessFactory.getFactory().exec(cmdLine, envp, workingDirectory);
			}
		} catch (IOException e) {
			if (p != null) {
				p.destroy();
			}
			abort(LaunchMessages.getString("LocalRunLaunchDelegate.Error_starting_process"), e, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (NoSuchMethodError e) {
			//attempting launches on 1.2.* - no ability to set working
			// directory

			IStatus status = new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(),
					ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_NOT_SUPPORTED,
					LaunchMessages.getString("LocalRunLaunchDelegate.Does_not_support_working_dir"), //$NON-NLS-1$
					e);
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);

			if (handler != null) {
				Object result = handler.handleStatus(status, this);
				if (result instanceof Boolean && ((Boolean)result).booleanValue()) {
					p = exec(cmdLine, environ, null);
				}
			}
		}
		return p;
	}

	protected String getPluginID() {
		return LaunchUIPlugin.getUniqueIdentifier();
	}
}