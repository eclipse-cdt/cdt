/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems          - initial API and implementation
 * Anna Dushistova (MontaVista)- adapted from org.eclipse.tcf.te.tcf.launch.core.steps.LaunchProcessStep
 *******************************************************************************/

package org.eclipse.cdt.launch.remote;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.core.utils.text.StringUtil;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.filetransfer.FileTransferItem;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.ILineSeparatorConstants;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.runtime.utils.net.IPAddressUtil;
import org.eclipse.tcf.te.tcf.core.interfaces.ITransportTypes;
import org.eclipse.tcf.te.tcf.filesystem.core.services.FileTransferService;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.processes.core.interfaces.launcher.IProcessLauncher;
import org.eclipse.tcf.te.tcf.processes.core.launcher.ProcessLauncher;

public class TEHelper {

	public static void initializeTE() {
		IPeerModel[] peers = Model.getModel().getPeers();
		if(peers.length == 0){
			// Sleep shortly
			try { Thread.sleep(300); } catch (InterruptedException e) {}
		}
	}

	public static void remoteFileTransfer(IPeer peer, String localFilePath,
			String remoteFilePath, SubProgressMonitor monitor) {
		monitor.beginTask(Messages.RemoteRunLaunchDelegate_2 + " " //$NON-NLS-1$
				+ localFilePath + " to " + remoteFilePath, 100); //$NON-NLS-1$
		FileTransferItem item = new FileTransferItem(new Path(localFilePath),
				new Path(remoteFilePath));
		// FIXME why cannot I do
		// item.setProperty(FileTransferItem.PROPERTY_DIRECTION,FileTransferItem.HOST_TO_TARGET);
		final Callback callback = new Callback();
		FileTransferService.transfer(peer, null, item, monitor, callback);
		// Wait till the step finished, an execution occurred or the
		// user hit cancel on the progress monitor.
		ExecutorsUtil.waitAndExecute(0, callback.getDoneConditionTester(null));
	}

	public static IPeerModel getPeer(final String peerId) {
		if (peerId != null) {
			final AtomicReference<IPeerModel> parent = new AtomicReference<IPeerModel>();
			final Runnable runnable = new Runnable() {
				public void run() {
					parent.set(Model.getModel()
							.getService(ILocatorModelLookupService.class)
							.lkupPeerModelById(peerId));
				}
			};
			ExecutorsUtil.executeWait(new Runnable() {
				public void run() {
					Protocol.invokeAndWait(runnable);
				}
			});
			return parent.get();
		}
		return null;
	}

	public static IPeerModel getCurrentConnection(ILaunchConfiguration config)
			throws CoreException {
		String peerId = config.getAttribute(
				IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION,
				""); //$NON-NLS-1$
		IPeerModel connection = getPeer(peerId);
		if (connection == null) {
			RSEHelper.abort(Messages.RemoteRunLaunchDelegate_13, null,
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		return connection;
	}

	public static void launchCmd(final IPeer peer, String command,
			SubProgressMonitor monitor, ICallback callback)
			throws CoreException {
		if (command != null && !command.trim().equals("")) { //$NON-NLS-1$
			String[] args = StringUtil.tokenize(command, 0, true);
			if (args.length > 0) {
				String cmd = args[0];
				String[] arguments = null;
				if (args.length > 1) {
					arguments = Arrays.copyOfRange(args, 1, args.length);
				}
				launchCmd(peer, cmd, arguments, monitor, callback);
			}
		}
	}

	public static void launchCmd(final IPeer peer, String remoteCommandPath,
			String arguments, SubProgressMonitor monitor, ICallback callback)
			throws CoreException {
		String[] args = arguments != null && !"".equals(arguments.trim()) ? StringUtil.tokenize(arguments, 0, true) : null; //$NON-NLS-1$
		launchCmd(peer, remoteCommandPath, args, monitor, callback);
	}

	public static void launchCmd(final IPeer peer, String remoteCommandPath,
			String[] args, SubProgressMonitor monitor, ICallback callback)
			throws CoreException {
		if (remoteCommandPath != null && !remoteCommandPath.trim().equals("")) { //$NON-NLS-1$
			monitor.beginTask(NLS.bind(Messages.RemoteRunLaunchDelegate_8,
					remoteCommandPath, args), 10);

			// Construct the launcher object
			ProcessLauncher launcher = new ProcessLauncher();

			Map<String, Object> launchAttributes = new HashMap<String, Object>();

			launchAttributes.put(IProcessLauncher.PROP_PROCESS_PATH,
					RSEHelper.spaceEscapify(remoteCommandPath));
			launchAttributes.put(IProcessLauncher.PROP_PROCESS_ARGS, args);

			launchAttributes.put(ITerminalsConnectorConstants.PROP_LOCAL_ECHO,
					Boolean.FALSE);

			boolean outputConsole = true;
			if (outputConsole) {
				launchAttributes.put(
						IProcessLauncher.PROP_PROCESS_ASSOCIATE_CONSOLE,
						Boolean.TRUE);
			}

			// Fill in the launch attributes
			IPropertiesContainer container = new PropertiesContainer();
			container.setProperties(launchAttributes);

			// If the line separator setting is not set explicitly, try to
			// determine
			// it automatically (local host only).
			if (container
					.getProperty(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR) == null) {
				// Determine if the launch is on local host. If yes, we can
				// preset
				// the
				// line ending character.
				final AtomicBoolean isLocalhost = new AtomicBoolean();

				Runnable runnable = new Runnable() {
					public void run() {
						if (ITransportTypes.TRANSPORT_TYPE_TCP.equals(peer
								.getTransportName())
								|| ITransportTypes.TRANSPORT_TYPE_SSL
										.equals(peer.getTransportName())) {
							isLocalhost.set(IPAddressUtil.getInstance()
									.isLocalHost(
											peer.getAttributes().get(
													IPeer.ATTR_IP_HOST)));
						}
					}
				};

				if (Protocol.isDispatchThread())
					runnable.run();
				else
					Protocol.invokeAndWait(runnable);

				if (isLocalhost.get()) {
					container
							.setProperty(
									ITerminalsConnectorConstants.PROP_LINE_SEPARATOR,
									Host.isWindowsHost() ? ILineSeparatorConstants.LINE_SEPARATOR_CRLF
											: ILineSeparatorConstants.LINE_SEPARATOR_LF);
				}
			}

			// Launch the process
			launcher.launch(peer, container, new Callback(callback) {
				@Override
				protected void internalDone(Object caller, IStatus status) {
					if (!status.isOK()) {
						System.out.println(status.getMessage());
					}
					super.internalDone(caller, status);
				}
			});
			monitor.done();
		}
	}
}
