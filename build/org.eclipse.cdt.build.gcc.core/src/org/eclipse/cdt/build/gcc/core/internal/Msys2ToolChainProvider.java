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
import org.eclipse.cdt.core.build.IToolChain;
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
			boolean on64bit = Platform.getOSArch().equals(Platform.ARCH_X86_64);
			String key32bit = null;
			for (int i = 0; (subkey = registry.getCurrentUserKeyName(uninstallKey, i)) != null; i++) {
				String compKey = uninstallKey + '\\' + subkey;
				String displayName = registry.getCurrentUserValue(compKey, "DisplayName"); //$NON-NLS-1$
				if (on64bit) {
					if ("MSYS2 64bit".equals(displayName)) { //$NON-NLS-1$
						if (addToolChain64(manager, registry, compKey)) {
							key32bit = null;
							break;
						}
					} else if ("MSYS2 32bit".equals(displayName)) { //$NON-NLS-1$
						key32bit = compKey;
					}
				} else {
					if ("MSYS2 32bit".equals(displayName)) { //$NON-NLS-1$
						if (addToolChain32(manager, registry, compKey)) {
							break;
						}
					}
				}
			}

			if (on64bit && key32bit != null) {
				addToolChain64(manager, registry, key32bit);
			}
		}
	}

	private boolean addToolChain64(IToolChainManager manager, WindowsRegistry registry, String key) {
		String installLocation = registry.getCurrentUserValue(key, "InstallLocation"); //$NON-NLS-1$
		Path msysPath = Paths.get(installLocation);
		Path gccPath = msysPath.resolve("mingw64\\bin\\gcc.exe"); //$NON-NLS-1$
		if (Files.exists(gccPath)) {
			GCCToolChain toolChain = new GCCToolChain(this, "x86_64-w64-mingw32", "msys2.x86_64", new Path[] { //$NON-NLS-1$ //$NON-NLS-2$
					gccPath.getParent(), msysPath.resolve("bin"), msysPath.resolve("usr\\bin") }); //$NON-NLS-1$ //$NON-NLS-2$
			toolChain.setProperty(IToolChain.ATTR_PACKAGE, "msys2"); //$NON-NLS-1$
			manager.addToolChain(toolChain);
			return true;
		} else {
			return addToolChain32(manager, registry, key);
		}
	}

	private boolean addToolChain32(IToolChainManager manager, WindowsRegistry registry, String key) {
		String installLocation = registry.getCurrentUserValue(key, "InstallLocation"); //$NON-NLS-1$
		Path msysPath = Paths.get(installLocation);
		Path gccPath = msysPath.resolve("mingw32\\bin\\gcc.exe"); //$NON-NLS-1$
		if (Files.exists(gccPath)) {
			GCCToolChain toolChain = new GCCToolChain(this, "i686-w64-mingw32", "msys2.i686", new Path[] { //$NON-NLS-1$ //$NON-NLS-2$
					gccPath.getParent(), msysPath.resolve("bin"), msysPath.resolve("usr\\bin") }); //$NON-NLS-1$ //$NON-NLS-2$
			toolChain.setProperty(IToolChain.ATTR_PACKAGE, "msys2"); //$NON-NLS-1$
			manager.addToolChain(toolChain);
			return true;
		} else {
			return false;
		}
	}

}
