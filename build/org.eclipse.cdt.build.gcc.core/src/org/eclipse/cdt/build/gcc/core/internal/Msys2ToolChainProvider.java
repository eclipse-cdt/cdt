/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core.internal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.Platform;

public class Msys2ToolChainProvider implements IToolChainProvider {

	private static final String ID = "org.eclipse.cdt.build.gcc.core.msys2Provider"; //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(IToolChainManager manager) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			WindowsRegistry registry = WindowsRegistry.getRegistry();
			String uninstallKey = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"; //$NON-NLS-1$
			String subkey;
			for (int i = 0; (subkey = registry.getCurrentUserKeyName(uninstallKey, i)) != null; i++) {
				String compKey = uninstallKey + '\\' + subkey;
				String displayName = registry.getCurrentUserValue(compKey, "DisplayName"); //$NON-NLS-1$
				if ("MSYS2 64bit".equals(displayName)) { //$NON-NLS-1$
					String installLocation = registry.getCurrentUserValue(compKey, "InstallLocation"); //$NON-NLS-1$
					Path gccPath = Paths.get(installLocation + "\\mingw64\\bin\\gcc.exe"); //$NON-NLS-1$
					if (Files.exists(gccPath)) {
						manager.addToolChain(new GCCToolChain(this, "msys2.x86_64", "", gccPath.getParent())); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		}
	}

}
