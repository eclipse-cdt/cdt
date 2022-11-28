/*******************************************************************************
 * Copyright (c) 2022 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher;

import java.util.Map;

import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnection4;
import org.eclipse.osgi.util.NLS;

public class ContainerLaunchUtils {

	private static final String LATEST = ":latest"; //$NON-NLS-1$

	/**
	 * Maps the local path, to a path that is used within a docker container.
	 * @param path The host path
	 * @return the path within the docker container
	 * @see  toDockerPath(String)
	 */
	public static final String toDockerPath(IPath path) {
		String pathstring = path.toPortableString();
		if (path.getDevice() != null) {
			if (pathstring.charAt(0) != '/') {
				pathstring = '/' + pathstring;
			}
		}
		return ContainerLaunchUtils.toDockerPath(pathstring);

	}

	/**
	 * Maps the local path, to a path that is used within a docker container.
	 * C: is mapped to /c, etc.
	 * //$WSL/<NAME>/ is a bit more tricky. For now it will be mapped to /WSL/<NAME>/
	 * @param path The host path
	 * @return the path within the docker container
	 */
	public static final String toDockerPath(String path) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			path = path.replace(':', '/');
			//Fix WSL which starts with //$WSL - TODO: Make more robust
			path = path.replace("//WSL$/", "/WSL/"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		//Ensure the path is global.

		return path;
	}

	/**
	 * Convert a Path to a string that can be passed as docker volume to be mapped into the Docker container
	 * toDockerPath() is used to get the path within the Docker container.
	 * @param path The path on the hose
	 * @return The string to be passed to the docker daemon
	 */
	public static final String toDockerVolume(Map<String, String> pMap, IPath path) {
		// The path on the Docker host
		var dhPath = path.makeAbsolute().toString();

		for (var me : pMap.entrySet()) {
			var elp = me.getKey();
			var edhp = me.getValue();
			if (dhPath.startsWith(elp)) {
				dhPath = edhp + dhPath.substring(elp.length());
				break;
			}
		}

		// docker-path first, docker-host-path third
		String rv = toDockerPath(path.makeAbsolute());
		rv += ":HOST_FILE_SYSTEM:"; //$NON-NLS-1$
		rv += dhPath;
		rv += ":false:true"; //$NON-NLS-1$ RO=false, selected = true
		return rv;
	}

	/**
	 * Pull an Docker-Image if is not locally available.
	 *
	 * @param monitor A Monitor to show progress
	 * @param connectionName The name of the connection
	 * @param imageName The image to pull
	 * @return Status.OK on success, Status.CANCEL_STATUS if cancelled, and Status.ERROR if an error occurs
	 *
	 */
	public static @NonNull IStatus provideDockerImage(IProgressMonitor monitor, @NonNull final String connectionName,
			@NonNull String imageName) {

		// Try to pull image, if necessary
		final var connection = (IDockerConnection4) DockerConnectionManager.getInstance()
				.getConnectionByName(connectionName);

		if (connection == null) {
			// This is unlikely to happen, but who knows
			return new Status(Status.ERROR, DockerLaunchUIPlugin.PLUGIN_ID,
					NLS.bind(Messages.ContainerCommandLauncher_pullerr_noConn, imageName, connectionName));
		}

		// Make sure to not pull all images if no tag is found
		// Nothing found -> -1 ; Make sure the : comes after the last /. If neither exists, both are -1.
		if (imageName.lastIndexOf(':') <= imageName.lastIndexOf('/')) {
			imageName = imageName + LATEST;
		}

		// See if the image already exists
		if (connection.getImageInfo(imageName) != null)
			return Status.OK_STATUS;

		try {
			final var prghandler = connection.getDefaultPullImageProgressHandler(imageName, monitor);
			// Seems like Eclipse/javac(?) confuses this call with pullImage(String, IRegistryAccount, IDockerProgressHandler)
			// Feel free to remove the cast, if the error is not triggered anymore.
			((IDockerConnection) connection).pullImage(imageName, prghandler);
			return Status.OK_STATUS;
		} catch (DockerException e) {
			final IStatus rv;
			var causeE = e.getCause();
			// The first cause seems to a have more informative text.
			if (causeE != null && causeE.getClass().getName().startsWith("org.mandas.docker.client.exceptions")) { //$NON-NLS-1$
				rv = new Status(Status.ERROR, DockerLaunchUIPlugin.PLUGIN_ID,
						causeE.getMessage() + " (" + connectionName + ')'); //$NON-NLS-1$
			} else {
				rv = new Status(Status.ERROR, DockerLaunchUIPlugin.PLUGIN_ID,
						NLS.bind(Messages.ContainerCommandLauncher_pullerr, imageName, e.getMessage()));
			}
			// Write to error, to support finding the root cause
			ManagedBuilderCorePlugin.log(rv);
			return rv;
		} catch (InterruptedException e) {
			return new Status(Status.CANCEL, DockerLaunchUIPlugin.PLUGIN_ID, Messages.ContainerCommandLauncher_pullint);
		}
	}
}
