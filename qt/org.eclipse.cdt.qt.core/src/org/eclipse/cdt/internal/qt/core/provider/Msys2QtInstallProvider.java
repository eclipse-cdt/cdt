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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.internal.qt.core.QtInstall;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallProvider;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * QtInstall provider for Qt in MSYS2. Use the registry to find out where MSYS2 is installed.
 */
public class Msys2QtInstallProvider implements IQtInstallProvider {

	@Override
	public Collection<IQtInstall> getInstalls() {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			List<IQtInstall> installs = new ArrayList<>();
			// Look in the current user Uninstall key to look for the uninstaller
			WindowsRegistry registry = WindowsRegistry.getRegistry();
			String uninstallKey = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"; //$NON-NLS-1$
			String subkey;
			for (int i = 0; (subkey = registry.getCurrentUserKeyName(uninstallKey, i)) != null; i++) {
				String compKey = uninstallKey + '\\' + subkey;
				String displayName = registry.getCurrentUserValue(compKey, "DisplayName"); //$NON-NLS-1$
				// On Windows, look for MSYS2, MinGW 64/32 locations
				if ("MSYS2 64bit".equals(displayName)) { //$NON-NLS-1$
					String installLocation = registry.getCurrentUserValue(compKey, "InstallLocation"); //$NON-NLS-1$
					Path qmakePath = Paths.get(installLocation + "\\mingw64\\bin\\qmake.exe"); //$NON-NLS-1$
					QtInstall install = new QtInstall(qmakePath);
					install.setProperty(IToolChain.ATTR_PACKAGE, "msys2"); //$NON-NLS-1$
					installs.add(install);
				}
			}
			return installs;
		} else {
			return Collections.emptyList();
		}
	}

}
