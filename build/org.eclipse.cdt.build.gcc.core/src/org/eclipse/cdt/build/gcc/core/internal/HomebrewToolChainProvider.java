/*******************************************************************************
 * Copyright (c) 2016, 2025 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 *     QNX Software Systems - MinGW implementation
 *     John Dallaway - Initial Homebrew implementation (#1175)
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core.internal;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.eclipse.cdt.build.gcc.core.ClangToolChain;
import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.internal.core.Homebrew;
import org.eclipse.core.runtime.Platform;

public class HomebrewToolChainProvider implements IToolChainProvider {

	private static final String ID = "org.eclipse.cdt.build.gcc.core.homebrewProvider"; //$NON-NLS-1$
	private static final Pattern CLANG_PATTERN = Pattern.compile("clang-\\d+"); //$NON-NLS-1$
	private static final Pattern GCC_PATTERN = Pattern.compile("gcc-\\d+"); //$NON-NLS-1$
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$
	private static final String HOMEBREW_PACKAGE = "homebrew"; //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(IToolChainManager manager) {
		final String homebrewHome = Homebrew.getHomebrewHome();
		if (null != homebrewHome) {
			Path homebrewPath = new File(homebrewHome).toPath();
			Path homebrewBinPath = homebrewPath.resolve("bin"); //$NON-NLS-1$
			Path homebrewLlvmBinPath = homebrewPath.resolve("opt/llvm/bin"); //$NON-NLS-1$
			for (File clangFile : getFiles(homebrewLlvmBinPath, CLANG_PATTERN)) {
				IEnvironmentVariable[] vars = createEnvironmentVariables(homebrewLlvmBinPath);
				IToolChain toolChain = new ClangToolChain(this, clangFile.toPath(), Platform.getOSArch(), vars);
				toolChain.setProperty(IToolChain.ATTR_PACKAGE, HOMEBREW_PACKAGE);
				manager.addToolChain(toolChain);
			}
			for (File gccFile : getFiles(homebrewBinPath, GCC_PATTERN)) {
				IEnvironmentVariable[] vars = createEnvironmentVariables(homebrewBinPath);
				IToolChain toolChain = new GCCToolChain(this, gccFile.toPath(), Platform.getOSArch(), vars);
				toolChain.setProperty(IToolChain.ATTR_PACKAGE, HOMEBREW_PACKAGE);
				manager.addToolChain(toolChain);
			}
		}
	}

	private File[] getFiles(Path path, Pattern filePattern) {
		File dir = path.toFile();
		if (dir.isDirectory()) {
			return dir.listFiles(file -> file.isFile() && filePattern.matcher(file.getName()).matches());
		}
		return new File[0];
	}

	private IEnvironmentVariable[] createEnvironmentVariables(Path path) {
		EnvironmentVariable pathVariable = new EnvironmentVariable(ENV_PATH, path.toString(),
				IEnvironmentVariable.ENVVAR_PREPEND, File.pathSeparator);
		return new IEnvironmentVariable[] { pathVariable };
	}

}
