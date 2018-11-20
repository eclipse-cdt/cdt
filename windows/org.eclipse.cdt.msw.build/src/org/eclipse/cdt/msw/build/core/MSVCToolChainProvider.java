/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.msw.build.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.msw.build.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Toolchain provider for Microsoft's Visual C++ Compiler (MSVC).
 *
 * This implementation only supports Microsoft Build Tools 2017 and
 * the Windows 10 SDK (Kit).
 */
public class MSVCToolChainProvider implements IToolChainProvider {

	public MSVCToolChainProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getId() {
		return "org.eclipse.cdt.msw.build"; //$NON-NLS-1$
	}

	@Override
	public void init(IToolChainManager manager) throws CoreException {
		// See if cl is installed
		Path vsPath = Paths.get("C:", "Program Files (x86)", "Microsoft Visual Studio"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (!Files.exists(vsPath)) {
			return;
		}

		Path vs2017Path = vsPath.resolve("2017"); //$NON-NLS-1$
		if (!Files.exists(vs2017Path)) {
			return;
		}

		Path msvcPath = vs2017Path.resolve("BuildTools").resolve("VC").resolve("Tools").resolve("MSVC"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (!Files.exists(msvcPath)) {
			return;
		}

		String hostPath = Platform.getOSArch().equals(Platform.ARCH_X86) ? "HostX86" : "HostX64"; //$NON-NLS-1$ //$NON-NLS-2$
		String archPath = Platform.getOSArch().equals(Platform.ARCH_X86) ? "x86" : "x64"; //$NON-NLS-1$ //$NON-NLS-2$

		try {
			Files.find(msvcPath, 6, (path, attr) -> {
				return path.getFileName().toString().equalsIgnoreCase("cl.exe") //$NON-NLS-1$
						&& path.getParent().getParent().getFileName().toString().equalsIgnoreCase(hostPath)
						&& path.getParent().getFileName().toString().equalsIgnoreCase(archPath);
			}).forEach((path) -> {
				manager.addToolChain(new MSVCToolChain(this, path.getParent()));
			});
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Finding cl.exe", e)); //$NON-NLS-1$
		}
	}

}
