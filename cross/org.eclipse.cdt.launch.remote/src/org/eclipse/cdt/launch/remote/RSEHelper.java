/********************************************************************************
 * Copyright (c) 2009 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista) - initial API and implementation
 ********************************************************************************/

package org.eclipse.cdt.launch.remote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;

public class RSEHelper {

	public static IHost getRemoteConnectionByName(String remoteConnection) {
		if (remoteConnection == null)
			return null;
		IHost[] connections = RSECorePlugin.getTheSystemRegistry().getHosts();
		for (int i = 0; i < connections.length; i++)
			if (connections[i].getAliasName().equals(remoteConnection))
				return connections[i];
		return null; // TODO Connection is not found in the list--need to react
		// somehow, throw the exception?

	}

	public static IService getConnectedRemoteFileService(
			IHost currentConnection, IProgressMonitor monitor) throws Exception {
		final ISubSystem subsystem = getFileSubsystem(currentConnection);

		if (subsystem == null)
			throw new Exception(Messages.RemoteRunLaunchDelegate_4);

		try {
			subsystem.connect(monitor, false);
		} catch (CoreException e) {
			throw e;
		} catch (OperationCanceledException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		if (!subsystem.isConnected())
			throw new Exception(Messages.RemoteRunLaunchDelegate_5);

		return ((IFileServiceSubSystem) subsystem).getFileService();
	}

	public static IService getConnectedRemoteShellService(
			IHost currentConnection, IProgressMonitor monitor) throws Exception {
		ISubSystem subsystem = getSubSystemWithShellService(currentConnection);
		if (subsystem != null) {
			try {
				subsystem.connect(monitor, false);
			} catch (CoreException e) {
				throw e;
			} catch (OperationCanceledException e) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			if (!subsystem.isConnected())
				throw new Exception(Messages.RemoteRunLaunchDelegate_5);

			return (IShellService) subsystem.getSubSystemConfiguration()
					.getService(currentConnection).getAdapter(
							IShellService.class);
		} else {
			throw new Exception(Messages.RemoteRunLaunchDelegate_4);
		}
	}

	/**
	 * Find the first shell service associated with the host.
	 * 
	 * @param host
	 *            the connection
	 * @return shell service object, or <code>null</code> if not found.
	 */
	public static IShellService getShellService(IHost host) {
		ISubSystem ss = getSubSystemWithShellService(host);
		if (ss != null) {
			return (IShellService) ss.getSubSystemConfiguration().getService(
					host).getAdapter(IShellService.class);
		}
		return null;
	}

	/**
	 * Find the first IShellServiceSubSystem service associated with the host.
	 * 
	 * @param host
	 *            the connection
	 * @return shell service subsystem, or <code>null</code> if not found.
	 */
	public static ISubSystem getSubSystemWithShellService(IHost host) {
		if (host == null)
			return null;
		ISubSystem[] subSystems = host.getSubSystems();
		IShellService ssvc = null;
		for (int i = 0; subSystems != null && i < subSystems.length; i++) {
			IService svc = subSystems[i].getSubSystemConfiguration()
					.getService(host);
			if (svc != null) {
				ssvc = (IShellService) svc.getAdapter(IShellService.class);
				if (ssvc != null) {
					return subSystems[i];
				}
			}
		}
		return null;
	}

	public static ISubSystem getFileSubsystem(IHost host) {
		if (host == null)
			return null;
		ISubSystem[] subSystems = host.getSubSystems();
		for (int i = 0; i < subSystems.length; i++) {
			if (subSystems[i] instanceof IFileServiceSubSystem)
				return subSystems[i];
		}
		return null;
	}

	public static IHost[] getSuitableConnections() {
		ArrayList shellConnections = new ArrayList(Arrays.asList(RSECorePlugin.getTheSystemRegistry()
				.getHostsBySubSystemConfigurationCategory("shells"))); //$NON-NLS-1$
		ArrayList terminalConnections = new ArrayList(Arrays.asList(RSECorePlugin.getTheSystemRegistry()
		.getHostsBySubSystemConfigurationCategory("terminals")));//$NON-NLS-1$

		Iterator iter = terminalConnections.iterator();
		while(iter.hasNext()){
			Object terminalConnection = iter.next();
			if(!shellConnections.contains(terminalConnection)){
				shellConnections.add(terminalConnection);
			}
		}
		
		return (IHost[]) shellConnections.toArray(new IHost[shellConnections.size()]);
	}
}
