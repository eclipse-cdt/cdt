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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

public class ContainerLaunchUtils {

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
	public static final String toDockerVolume(IPath path) {
		IPath p = path.makeAbsolute();
		String rv = toDockerPath(p);
		rv += ":HOST_FILE_SYSTEM:"; //$NON-NLS-1$
		rv += p.toOSString();
		rv += ":false:true"; //$NON-NLS-1$ RO=false, selected = true
		return rv;
	}

}
