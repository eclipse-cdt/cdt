/*******************************************************************************
 * Copyright (c) 2002 - 2004 QNX Software Systems and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

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
			IBinaryExecutable exeFile = createBinary(project, exePath);

			ICDebugConfiguration debugConfig = getDebugConfig(config);
			ICDISession dsession = null;
			ICProject cproject = getCProject(config);

			String path = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, (String)null);
			IPath corefile;
			if (path == null) {
				corefile = promptForCoreFilePath((IProject)cproject.getResource());
				if (corefile == null) {
					cancel(LaunchMessages.getString("CoreFileLaunchDelegate.No_Corefile_selected"), //$NON-NLS-1$
							ICDTLaunchConfigurationConstants.ERR_NO_COREFILE);
				}
				ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, corefile.toString());
				launch(wc, mode, launch, new SubProgressMonitor(monitor, 9));
				return;
			}
			corefile = new Path(path);
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
					CDIDebugModel.newDebugTarget(launch, project.getProject(), targets[i], renderTargetLabel(debugConfig), iprocess,
												exeFile, false, false, false);
				}
			} catch (CoreException e) {
				try {
					dsession.terminate();
				} catch (CDIException cdi) {
				}
				throw e;
			}
		} finally {
			monitor.done();
		}
											
	}

	protected IPath promptForCoreFilePath(final IProject project) throws CoreException {
		final Shell shell = LaunchUIPlugin.getShell();
		final String res[] = {null};
		if (shell == null) {
			abort(LaunchMessages.getString("CoreFileLaunchDelegate.No_Shell_available_in_Launch"), null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		Display display = shell.getDisplay();
		display.syncExec(new Runnable() {

			public void run() {
				FileDialog dialog = new FileDialog(shell);
				dialog.setText(LaunchMessages.getString("CoreFileLaunchDelegate.Select_Corefile")); //$NON-NLS-1$

				String initPath = null;
				try {
					initPath = project.getPersistentProperty(new QualifiedName(LaunchUIPlugin.getUniqueIdentifier(), "SavePath")); //$NON-NLS-1$
				} catch (CoreException e) {
				}
				if (initPath == null || initPath.equals("")) { //$NON-NLS-1$
					initPath = project.getLocation().toString();
				}
				dialog.setFilterPath(initPath);
				res[0] = dialog.open();
			}
		});
		if (res[0] != null) {
			return new Path(res[0]);
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