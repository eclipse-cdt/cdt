/*******************************************************************************
 * Copyright (c) 2016, 2024 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 *     John Dallaway - Support multiple MSYS2 64-bit registry names (#237)
 *     John Dallaway - Detect UCRT64 toolchains (#887)
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core.internal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.build.gcc.core.ClangToolChain;
import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.Platform;

public class Msys2ToolChainProvider implements IToolChainProvider {

	private static final String ID = "org.eclipse.cdt.build.gcc.core.msys2Provider"; //$NON-NLS-1$
	private static final Set<String> MSYS2_64BIT_NAMES = Set.of("MSYS2", "MSYS2 64bit"); //$NON-NLS-1$ //$NON-NLS-2$

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
				if (displayName == null) {
					continue;
				}
				if (on64bit) {
					if (MSYS2_64BIT_NAMES.contains(displayName)) {
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
		boolean found = false;
		for (String variant : List.of("ucrt64", "mingw64")) { //$NON-NLS-1$ //$NON-NLS-2$
			Path gccPath = msysPath.resolve(variant + "\\bin\\gcc.exe"); //$NON-NLS-1$
			if (Files.exists(gccPath)) {
				IEnvironmentVariable[] vars = createEnvironmentVariables(msysPath, gccPath.getParent());
				IToolChain toolChain = new GCCToolChain(this, gccPath, Platform.ARCH_X86_64, vars);
				toolChain.setProperty(IToolChain.ATTR_PACKAGE, "msys2"); //$NON-NLS-1$
				manager.addToolChain(toolChain);
				found = true;
			}
			Path clangPath = msysPath.resolve(variant + "\\bin\\clang.exe"); //$NON-NLS-1$
			if (Files.exists(clangPath)) {
				IEnvironmentVariable[] vars = createEnvironmentVariables(msysPath, clangPath.getParent());
				IToolChain toolChain = new ClangToolChain(this, clangPath, Platform.ARCH_X86_64, vars);
				toolChain.setProperty(IToolChain.ATTR_PACKAGE, "msys2"); //$NON-NLS-1$
				manager.addToolChain(toolChain);
				found = true;
			}
		}
		return found || addToolChain32(manager, registry, key);
	}

	private boolean addToolChain32(IToolChainManager manager, WindowsRegistry registry, String key) {
		String installLocation = registry.getCurrentUserValue(key, "InstallLocation"); //$NON-NLS-1$
		Path msysPath = Paths.get(installLocation);
		Path gccPath = msysPath.resolve("mingw32\\bin\\gcc.exe"); //$NON-NLS-1$
		if (Files.exists(gccPath)) {
			IEnvironmentVariable[] vars = createEnvironmentVariables(msysPath, gccPath.getParent());
			IToolChain toolChain = new GCCToolChain(this, gccPath, Platform.ARCH_X86, vars);
			toolChain.setProperty(IToolChain.ATTR_PACKAGE, "msys2"); //$NON-NLS-1$
			manager.addToolChain(toolChain);
			return true;
		}
		return false;
	}

	private IEnvironmentVariable[] createEnvironmentVariables(Path msysPath, Path toolPath) {
		StringBuilder pathVar = new StringBuilder();
		pathVar.append(toolPath);
		pathVar.append(File.pathSeparatorChar);
		pathVar.append(msysPath);
		pathVar.append("\\usr\\local\\bin"); //$NON-NLS-1$
		pathVar.append(File.pathSeparatorChar);
		pathVar.append(msysPath);
		pathVar.append("\\usr\\bin"); //$NON-NLS-1$
		pathVar.append(File.pathSeparatorChar);
		pathVar.append(msysPath);
		pathVar.append("\\bin"); //$NON-NLS-1$
		EnvironmentVariable pathVariable = new EnvironmentVariable("PATH", pathVar.toString(), //$NON-NLS-1$
				IEnvironmentVariable.ENVVAR_PREPEND, File.pathSeparator);
		return new IEnvironmentVariable[] { pathVariable };
	}

}
