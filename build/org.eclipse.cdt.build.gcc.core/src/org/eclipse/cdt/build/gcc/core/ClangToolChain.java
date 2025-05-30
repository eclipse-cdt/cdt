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
package org.eclipse.cdt.build.gcc.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;

/**
 * The Clang toolchain. There's little different from the GCC toolchain other
 * than the toolchain type and name.
 *
 * @author dschaefer
 *
 */
public class ClangToolChain extends GCCToolChain {

	public static final String TYPE_ID = "org.eclipse.cdt.build.clang"; //$NON-NLS-1$

	public ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,
			IEnvironmentVariable[] envVars) {
		super(provider, pathToToolChain, arch, addClangEnvVars(envVars));
	}

	private static IEnvironmentVariable[] addClangEnvVars(IEnvironmentVariable[] envVars) {
		List<IEnvironmentVariable> envVarsNew = new ArrayList<>(Arrays.asList(envVars));
		/*
		 * Set CC and CXX environment variables for clang and clang++. This is equivalent to setting these in the
		 * CMakeLists.txt:
		 * set(CMAKE_C_COMPILER clang)
		 * set(CMAKE_CXX_COMPILER clang++)
		 */
		addIfAbsent("cc", "clang", envVarsNew); //$NON-NLS-1$ //$NON-NLS-2$
		addIfAbsent("cxx", "clang++", envVarsNew); //$NON-NLS-1$ //$NON-NLS-2$

		return envVarsNew.toArray(new EnvironmentVariable[0]);
	}

	private static void addIfAbsent(String name, String value, List<IEnvironmentVariable> envVarsNew) {
		boolean isAbsent = envVarsNew.stream().noneMatch(ev -> ev.getName().equals(name));

		if (isAbsent) {
			envVarsNew.add(new EnvironmentVariable(name, value, IEnvironmentVariable.ENVVAR_APPEND, null));
		}
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

}
