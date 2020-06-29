/*******************************************************************************
 * Copyright (c) 2016, 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.dap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class InitializeLaunchConfigurations {

	private boolean alreadyWarned;
	private Runnable warnNodeJSMissing;

	public InitializeLaunchConfigurations(Runnable warnNodeJSMissing) {
		Assert.isNotNull(warnNodeJSMissing);
		this.warnNodeJSMissing = warnNodeJSMissing;
	}

	public Optional<String> getVSCodeLocation(String appendPathSuffix) {
		String res = null;
		if (Platform.getOS().equals(Platform.OS_LINUX)) {
			res = "/usr/share/code"; //$NON-NLS-1$
		} else if (Platform.getOS().equals(Platform.OS_WIN32)) {
			res = "C:/Program Files (x86)/Microsoft VS Code"; //$NON-NLS-1$
		} else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			res = "/Applications/Visual Studio Code.app"; //$NON-NLS-1$

			IPath path = new Path(appendPathSuffix);
			// resources/ maps to Contents/Resources on macOS
			if (path.segmentCount() > 1 && path.segment(0).equals("resources")) { //$NON-NLS-1$
				path = path.removeFirstSegments(1);
				appendPathSuffix = new Path("/Contents/Resources").append(path).toOSString(); //$NON-NLS-1$
			}
		}
		if (res != null && new File(res).isDirectory()) {
			if (res.contains(" ") && Platform.getOS().equals(Platform.OS_WIN32)) { //$NON-NLS-1$
				return Optional.of("\"" + res + appendPathSuffix + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return Optional.of(res + appendPathSuffix);
		}
		return Optional.empty();
	}

	public Optional<String> getNodeJsLocation() {
		{
			String nodeJsLocation = System.getProperty("org.eclipse.wildwebdeveloper.nodeJSLocation"); //$NON-NLS-1$
			if (nodeJsLocation != null && Files.exists(Paths.get(nodeJsLocation))) {
				return Optional.of(nodeJsLocation);
			}
		}

		String res = "/path/to/node"; //$NON-NLS-1$
		String[] command = new String[] { "/bin/bash", "-c", "which node" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] { "cmd", "/c", "where node" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream()));) {
			res = reader.readLine();
		} catch (IOException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}

		// Try default install path as last resort
		if (res == null && Platform.getOS().equals(Platform.OS_MACOSX)) {
			res = "/usr/local/bin/node"; //$NON-NLS-1$
		}

		if (res != null && Files.exists(Paths.get(res))) {
			return Optional.of(res);
		} else if (!alreadyWarned) {
			warnNodeJSMissing.run();
			alreadyWarned = true;
		}
		return Optional.empty();
	}

}
