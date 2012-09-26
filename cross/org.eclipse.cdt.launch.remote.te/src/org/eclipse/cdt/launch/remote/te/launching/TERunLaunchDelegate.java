package org.eclipse.cdt.launch.remote.te.launching;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.internal.remote.te.Messages;
import org.eclipse.cdt.launch.remote.te.Activator;
import org.eclipse.cdt.launch.remote.te.IRemoteTEConfigurationConstants;
import org.eclipse.cdt.launch.remote.te.utils.TEHelper;
import org.eclipse.cdt.launch.remote.te.utils.TERunProcess;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.callback.Callback;

public class TERunLaunchDelegate extends AbstractCLaunchDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IPath exePath = checkBinaryDetails(config);
		if (exePath != null) {
			// -1. Initialize TE
			Activator.getDefault().initializeTE();
			// 0. Get the peer from the launch configuration
			IPeer peer = TEHelper.getCurrentConnection(config).getPeer();
			// 1.Download binary if needed
			String remoteExePath = config.getAttribute(
					IRemoteTEConfigurationConstants.ATTR_REMOTE_PATH, ""); //$NON-NLS-1$
			monitor.setTaskName(Messages.RemoteRunLaunchDelegate_2);
			boolean skipDownload = config
					.getAttribute(
							IRemoteTEConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
							false);

			if (!skipDownload) {
				TEHelper.remoteFileTransfer(peer, exePath.toString(),
						remoteExePath, new SubProgressMonitor(monitor, 80));
			}
			// 2. Run the binary
			monitor.setTaskName(Messages.RemoteRunLaunchDelegate_12);
			String arguments = getProgramArguments(config);
			String prelaunchCmd = config.getAttribute(
					IRemoteTEConfigurationConstants.ATTR_PRERUN_COMMANDS, ""); //$NON-NLS-1$

			TEHelper.launchCmd(peer, prelaunchCmd, null,
					new SubProgressMonitor(monitor, 2), new Callback());
			new TERunProcess(launch, remoteExePath, arguments,
					renderProcessLabel(exePath.toOSString()), peer,
					new SubProgressMonitor(monitor, 20));
		}

	}

	protected IPath checkBinaryDetails(final ILaunchConfiguration config)
			throws CoreException {
		// First verify we are dealing with a proper project.
		ICProject project = CDebugUtils.verifyCProject(config);
		// Now verify we know the program to debug.
		IPath exePath = LaunchUtils.verifyProgramPath(config, project);
		// Finally, make sure the program is a proper binary.
		LaunchUtils.verifyBinary(config, exePath);
		return exePath;
	}

	@Override
	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	}

}
