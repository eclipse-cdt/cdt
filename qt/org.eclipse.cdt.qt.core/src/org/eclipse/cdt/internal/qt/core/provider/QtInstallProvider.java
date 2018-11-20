/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.provider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.eclipse.cdt.codan.core.cxx.Activator;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.internal.qt.core.QtInstall;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallProvider;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Qt Install provider that attempts to find the Qt package as installed using Qt's own installer.
 */
public class QtInstallProvider implements IQtInstallProvider {

	private static boolean isWin32 = Platform.getOS().equals(Platform.OS_WIN32);

	@Override
	public Collection<IQtInstall> getInstalls() {
		Path root = getQtRoot();
		Path qmake = Paths.get(isWin32 ? "bin/qmake.exe" : "bin/qmake"); //$NON-NLS-1$ //$NON-NLS-2$
		if (root != null && Files.exists(root)) {
			try {
				return Files.walk(root, 2).filter((path) -> Files.exists(path.resolve(qmake))).map((path) -> {
					QtInstall install = new QtInstall(path.resolve(qmake));
					if (isWin32 && "win32-g++".equals(install.getSpec())) { //$NON-NLS-1$
						install.setProperty(IToolChain.ATTR_PACKAGE, "qt"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					return install;
				}).collect(Collectors.toList());
			} catch (IOException e) {
				Activator.log(e);
			}
		}
		return Collections.emptyList();
	}

	private Path getQtRoot() {
		if (isWin32) {
			WindowsRegistry registry = WindowsRegistry.getRegistry();
			String uninstallKey = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"; //$NON-NLS-1$
			String subkey;
			for (int i = 0; (subkey = registry.getCurrentUserKeyName(uninstallKey, i)) != null; i++) {
				String compKey = uninstallKey + '\\' + subkey;
				String displayName = registry.getCurrentUserValue(compKey, "DisplayName"); //$NON-NLS-1$
				if ("Qt".equals(displayName)) { //$NON-NLS-1$
					String installLocation = registry.getCurrentUserValue(compKey, "InstallLocation"); //$NON-NLS-1$
					return Paths.get(installLocation);
				}
			}
		} else {
			Path qtDir = Paths.get(System.getProperty("user.home"), "Qt"); //$NON-NLS-1$ //$NON-NLS-2$
			if (Files.exists(qtDir)) {
				return qtDir;
			}
		}
		return null;
	}

	//   gcc is in C:\Qt\Tools\mingw492_32\bin
}
