/***************************************************************************************************
 * Copyright (c) 2002 - 2004 QNX Software Systems and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 **************************************************************************************************/
package org.eclipse.cdt.launch.internal;

import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;

public class CoreFileLaunchDelegate extends AbstractCLaunchDelegate {

	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(LaunchMessages.getString("CoreFileLaunchDelegate.Launching_postmortem_debugger"), 10); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.worked(1);
			IPath exePath = verifyProgramPath(config);
			ICProject project = verifyCProject(config);
			IBinaryExecutable exeFile = verifyBinary(project, exePath);

			ICDebugConfiguration debugConfig = getDebugConfig(config);
			ICDISession dsession = null;
			ICProject cproject = getCProject(config);

			String path = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, (String)null);
			if (path == null) {
				IPath corefile = promptForCoreFilePath((IProject)cproject.getResource(), debugConfig);
				if (corefile == null) {
					cancel(LaunchMessages.getString("CoreFileLaunchDelegate.No_Corefile_selected"), //$NON-NLS-1$
							ICDTLaunchConfigurationConstants.ERR_NO_COREFILE);
				}
				ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, corefile.toString());
				wc.launch(mode, new SubProgressMonitor(monitor, 9));
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, (String)null);
				cancel("", -1); //$NON-NLS-1$
			} else {
				dsession = debugConfig.createDebugger().createDebuggerSession(launch, exeFile, new SubProgressMonitor(monitor, 8));
				try {
					// set the source locator
					setDefaultSourceLocator(launch, config);
					ICDITarget[] targets = dsession.getTargets();
					for (int i = 0; i < targets.length; i++) {
						Process process = targets[i].getProcess();
						IProcess iprocess = null;
						if (process != null) {
							iprocess = DebugPlugin.newProcess(launch, process, renderProcessLabel(exePath.toOSString()));
						}
						CDIDebugModel.newDebugTarget(launch, project.getProject(), targets[i], renderTargetLabel(debugConfig),
														iprocess, exeFile, true, false, false);
					}
				} catch (CoreException e) {
					try {
						dsession.terminate();
					} catch (CDIException cdi) {
					}
					throw e;
				}
			}
		} finally {
			monitor.done();
		}

	}

	protected IPath promptForCoreFilePath(final IProject project, final ICDebugConfiguration debugConfig) throws CoreException {
		IStatus fPromptStatus = new Status(IStatus.INFO, "org.eclipse.debug.ui", 200, "", null); //$NON-NLS-1$//$NON-NLS-2$
		IStatus processPrompt = new Status(IStatus.INFO, "org.eclipse.cdt.launch", 101, "", null); //$NON-NLS-1$//$NON-NLS-2$
		// consult a status handler
		IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(fPromptStatus);
		if (prompter != null) {
			try {
				Object result = prompter.handleStatus(processPrompt, new Object[]{project, debugConfig});
				if (result instanceof IPath) {
					return (IPath)result;
				}
			} catch (CoreException e) {
			}
		}
		return null;
	}

	public String getPluginID() {
		return LaunchUIPlugin.getUniqueIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.launch.AbstractCLaunchConfigurationDelegate#preLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return true; // no pre launch check for core file
	}
}