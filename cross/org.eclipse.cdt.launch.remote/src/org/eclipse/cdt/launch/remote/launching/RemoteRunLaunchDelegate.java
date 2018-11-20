/*******************************************************************************
 * Copyright (c) 2006, 2016 PalmSource, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Ewa Matejska     (PalmSource)      - Adapted from LocalRunLaunchDelegate
 * Martin Oberhuber (Wind River)      - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River)      - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River)      - [226301][api] IShellService should throw SystemMessageException on error
 * Anna Dushistova  (MontaVista)      - [234490][remotecdt] Launching with disconnected target fails
 * Anna Dushistova  (MontaVista)      - [235298][remotecdt] Further improve progress reporting and cancellation of Remote CDT Launch
 * Anna Dushistova  (MontaVista)      - [244173][remotecdt][nls] Externalize Strings in RemoteRunLaunchDelegate
 * Anna Dushistova  (MontaVista)      - [181517][usability] Specify commands to be run before remote application launch
 * Nikita Shulga    (EmbeddedAlley)   - [265236][remotecdt] Wait for RSE to initialize before querying it for host list
 * Anna Dushistova  (MontaVista)      - [267951][remotecdt] Support systemTypes without files subsystem
 * Anna Dushistova  (Mentor Graphics) - [314659]Fixed deprecated methods
 * Anna Dushistova  (Mentor Graphics) - moved to org.eclipse.cdt.launch.remote.launching
 * Anna Dushistova  (MontaVista)      - [318051][remote debug] Terminating when "Remote shell" process is selected doesn't work
 * Anna Dushistova  (MontaVista)      - [368597][remote debug] if gdbserver fails to launch on target, launch doesn't get terminated
 *******************************************************************************/

package org.eclipse.cdt.launch.remote.launching;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.internal.launch.remote.Activator;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.remote.IRemoteConnectionConfigurationConstants;
import org.eclipse.cdt.launch.remote.RemoteHelper;
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
import org.eclipse.osgi.util.NLS;

public class RemoteRunLaunchDelegate extends AbstractCLaunchDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		IPath exePath = CDebugUtils.verifyProgramPath(config);
		ICProject project = CDebugUtils.verifyCProject(config);
		if (exePath != null) {
			verifyBinary(project, exePath);
			String arguments = getProgramArguments(config);
			String remoteExePath = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH, ""); //$NON-NLS-1$
			String prelaunchCmd = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_PRERUN_COMMANDS, ""); //$NON-NLS-1$

			if (monitor == null)
				monitor = new NullProgressMonitor();

			if (mode.equals(ILaunchManager.RUN_MODE)) {
				monitor.beginTask(Messages.RemoteRunLaunchDelegate_0, 100);
				Process remoteProcess = null;
				try {
					// Download the binary to the remote before debugging.
					monitor.setTaskName(Messages.RemoteRunLaunchDelegate_2);
					RemoteHelper.remoteFileDownload(config, launch, exePath.toString(), remoteExePath,
							new SubProgressMonitor(monitor, 80));
					// Use a remote shell to launch the binary.
					monitor.setTaskName(Messages.RemoteRunLaunchDelegate_12);
					remoteProcess = RemoteHelper.remoteShellExec(config, prelaunchCmd, remoteExePath, arguments,
							new SubProgressMonitor(monitor, 20));
					DebugPlugin.newProcess(launch, remoteProcess, renderProcessLabel(exePath.toOSString()));
				} catch (CoreException e) {
					throw e;
				} finally {
					monitor.done();
				}

			} else {
				IStatus status = new Status(IStatus.ERROR, getPluginID(), IStatus.OK,
						NLS.bind(Messages.RemoteRunLaunchDelegate_1, mode), null);
				throw new CoreException(status);
			}
		}
	}

	@Override
	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	}
}
