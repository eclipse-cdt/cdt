/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

public class QtMinGWToolChainProvider implements IToolChainProvider {

	public static final String ID = "org.eclipse.cdt.qt.core.qtMinGWProvider"; //$NON-NLS-1$
	public static final String TOOLCHAIN_ID = "qt.mingw"; //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(IToolChainManager manager) throws CoreException {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			WindowsRegistry registry = WindowsRegistry.getRegistry();
			String uninstallKey = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"; //$NON-NLS-1$
			String subkey;
			for (int i = 0; (subkey = registry.getCurrentUserKeyName(uninstallKey, i)) != null; i++) {
				String compKey = uninstallKey + '\\' + subkey;
				String displayName = registry.getCurrentUserValue(compKey, "DisplayName"); //$NON-NLS-1$
				if ("Qt".equals(displayName)) { //$NON-NLS-1$
					Path installLocation = Paths.get(registry.getCurrentUserValue(compKey, "InstallLocation")); //$NON-NLS-1$
					if (Files.exists(installLocation)) {
						Path gcc = Paths.get("bin\\gcc.exe"); //$NON-NLS-1$
						try {
							Files.walk(installLocation.resolve("Tools"), 1) //$NON-NLS-1$
									.filter(path -> Files.exists(path.resolve(gcc))).map(path -> {
										GCCToolChain toolChain = new GCCToolChain(this, TOOLCHAIN_ID, "", //$NON-NLS-1$
												new Path[] { path.resolve("bin") }); //$NON-NLS-1$
										toolChain.setProperty(IToolChain.ATTR_PACKAGE, "qt"); //$NON-NLS-1$
										return toolChain;
									}).forEach(toolChain -> manager.addToolChain(toolChain));
						} catch (IOException e) {
							Activator.log(e);
						}
					}
				}
			}
		}
	}

}
