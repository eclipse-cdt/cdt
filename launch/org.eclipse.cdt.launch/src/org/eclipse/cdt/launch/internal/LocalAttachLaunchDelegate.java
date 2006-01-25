/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launch.internal;

import java.io.FileNotFoundException;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
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
import org.eclipse.core.resources.IFile;
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
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;

public class LocalAttachLaunchDelegate extends AbstractCLaunchDelegate {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.debug.core.ILaunch,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IBinaryObject exeFile = null;
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(LaunchMessages.getString("LocalAttachLaunchDelegate.Attaching_to_Local_C_Application"), 10); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.worked(1);
			ICProject cproject = verifyCProject(config);
			IPath exePath = getProgramPath(config);
			if (exePath != null && !exePath.isEmpty()) {
				if (!exePath.isAbsolute()) {
					IFile wsProgramPath = cproject.getProject().getFile(exePath);
					exePath = wsProgramPath.getLocation();
				}
				if (!exePath.toFile().exists()) {
					abort(LaunchMessages.getString("AbstractCLaunchDelegate.Program_file_does_not_exist"), //$NON-NLS-1$
							new FileNotFoundException(LaunchMessages.getFormattedString(
									"AbstractCLaunchDelegate.PROGRAM_PATH_not_found", exePath.toOSString())), //$NON-NLS-1$
							ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
				}
				exeFile = verifyBinary(cproject, exePath);
			}

			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				ICDebugConfiguration debugConfig = getDebugConfig(config);
				ICDISession dsession = null;
				String debugMode = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
						ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
				if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
					// It may be that we have already been provided with a
					// process id
					if (config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, -1) == -1) {
						int pid = promptForProcessID(config);
						if (pid == -1) {
							cancel(LaunchMessages.getString("LocalAttachLaunchDelegate.No_Process_ID_selected"), //$NON-NLS-1$
									ICDTLaunchConfigurationConstants.ERR_NO_PROCESSID);
						}
						ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
						wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, pid);
						wc.launch(mode, new SubProgressMonitor(monitor, 9));
						wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, (String)null);
						cancel("", -1); //$NON-NLS-1$\
					} else {
						dsession = debugConfig.createDebugger().createDebuggerSession(launch, exeFile,
								new SubProgressMonitor(monitor, 8));
						try {
							// set the default source locator if required
							setDefaultSourceLocator(launch, config);
							ICDITarget[] targets = dsession.getTargets();
							for (int i = 0; i < targets.length; i++) {
								CDIDebugModel.newDebugTarget(launch, cproject.getProject(), targets[i],
										renderTargetLabel(debugConfig), null, exeFile, true, true, false);
							}
						} catch (CoreException e) {
							try {
								dsession.terminate();
							} catch (CDIException ex) {
								// ignore
							}
							throw e;
						}
					}
				}
			}
		} finally {
			monitor.done();
		}
	}

	protected int promptForProcessID(ILaunchConfiguration config) throws CoreException {
		IStatus fPromptStatus = new Status(IStatus.INFO, "org.eclipse.debug.ui", 200, "", null); //$NON-NLS-1$//$NON-NLS-2$
		IStatus processPrompt = new Status(IStatus.INFO, "org.eclipse.cdt.launch", 100, "", null); //$NON-NLS-1$//$NON-NLS-2$
		// consult a status handler
		IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(fPromptStatus);
		if (prompter != null) {
			Object result = prompter.handleStatus(processPrompt, config);
			if (result instanceof Integer) {
				return ((Integer)result).intValue();
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.launch.AbstractCLaunchConfigurationDelegate#getPluginID()
	 */
	protected String getPluginID() {
		return LaunchUIPlugin.getUniqueIdentifier();
	}
}
