/**********************************************************************
 * Copyright (c) 2002 - 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.launch.internal;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class CoreFileLaunchDelegate extends AbstractCLaunchDelegate {

	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(LaunchUIPlugin.getResourceString("CoreFileLaunchDelegate.Launching_postmortem_debugger"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		IFile exeFile = getProgramFile(config);

		ICDebugConfiguration debugConfig = getDebugConfig(config);
		ICDISession dsession = null;
		ICProject cproject = getCProject(config);

		IPath corefile = getCoreFilePath((IProject) cproject.getResource());
		if (corefile == null) {
			cancel(LaunchUIPlugin.getResourceString("CoreFileLaunchDelegate.No_Corefile_selected"), ICDTLaunchConfigurationConstants.ERR_NO_COREFILE); //$NON-NLS-1$
		}
		Process debugger = null;
		IProcess debuggerProcess = null;
		try {
			dsession = debugConfig.getDebugger().createCoreSession(config, exeFile, corefile);
			debugger = dsession.getSessionProcess();
		} catch (CDIException e) {
			if (dsession != null) {
				try {
					dsession.terminate();
				} catch (CDIException ex) {
					// ignore
				}
			}
			abort(LaunchUIPlugin.getResourceString("CoreFileLaunchDelegate.Failed_Launching_CDI_Debugger"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		}
		if ( debugger != null ) {
			debuggerProcess = DebugPlugin.newProcess(launch, debugger, renderDebuggerProcessLabel());
		}
		// set the source locator
		setSourceLocator(launch, config);

		CDIDebugModel.newCoreFileDebugTarget(
			launch,
			dsession.getCurrentTarget(),
			renderTargetLabel(debugConfig),
			debuggerProcess,
			exeFile);
		
		monitor.done();
	}

	protected IPath getCoreFilePath(final IProject project) throws CoreException {
		final Shell shell = LaunchUIPlugin.getShell();
		final String res[] = { null };
		if (shell == null) {
			abort(LaunchUIPlugin.getResourceString("CoreFileLaunchDelegate.No_Shell_available_in_Launch"), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		}
		Display display = shell.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				FileDialog dialog = new FileDialog(shell);
				dialog.setText(LaunchUIPlugin.getResourceString("CoreFileLaunchDelegate.Select_Corefile")); //$NON-NLS-1$

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
}
