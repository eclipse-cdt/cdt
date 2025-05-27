/*******************************************************************************
 * Copyright (c) 2017, 2025 QNX Software Systems and others.
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.runtime.Platform;

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

	/**
	 * Add the environment variables CC and CXX, set to clang and clang++, to override the C/C++ compiler executable
	 * that CMake uses.
	 *
	 * Other methods of setting the compiler in CMake (eg; CMAKE_&lt;LANG&gt;_COMPILER in CMakeLists.txt,
	 * -DCMAKE_TOOLCHAIN_FILE=file) take precedence over these environment variables, so the user is not
	 * inhibited by this approach.
	 *
	 * @param envVars Environment variables originate from the core build toolchain (either "Discovered toolchain" or
	 * "User defined toolchain"); these are not the OS system envVars. When the tc is a user defined, it is possible to
	 * specify arbitrary environment variables, meaning the user may have already defined CC/CXX.
	 * If CC/CXX is already defined then nothing is added.
	 *
	 * @return the existing array of envVars, plus if they didn't already exist the envVars CC and CXX set for clang.
	 */
	private static IEnvironmentVariable[] addClangEnvVars(IEnvironmentVariable[] envVars) {
		List<IEnvironmentVariable> envVarsNew = new ArrayList<>();
		// envVars can be null if the user defined tc did not specify any env variables.
		if (envVars != null) {
			envVarsNew.addAll(Arrays.asList(envVars));
		}
		addIfAbsent("CC", "clang", envVarsNew); //$NON-NLS-1$ //$NON-NLS-2$
		addIfAbsent("CXX", "clang++", envVarsNew); //$NON-NLS-1$ //$NON-NLS-2$

		return envVarsNew.toArray(new EnvironmentVariable[0]);
	}

	/**
	 * @param name The name of the environment variable.
	 * On case insensitive platforms (windows) the case is ignored. If an envVar with the name "cc" already exists,
	 * a new envVar will NOT be added.<br>
	 *
	 * However, on Windows, existing cc/cxx envVars with lower-case names will have their names changed to upper case;
	 * the value remains unchanged.<br>
	 *
	 * On case sensitive platforms (Non-windows) the case is respected. If an envVar with the name "cc" already exists,
	 * a new envVar with name "CC" will be added using the value in {@code value}.
	 * @param value The value of the environment variable.
	 * @param envVars List of environment variables to check and potentially add to.
	 */
	private static void addIfAbsent(String name, String value, List<IEnvironmentVariable> envVars) {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			// On Windows: case-insensitive check ignores case.
			Iterator<IEnvironmentVariable> iterator = envVars.iterator();
			boolean found = false;

			while (iterator.hasNext()) {
				IEnvironmentVariable ev = iterator.next();
				if (ev.getName().equalsIgnoreCase(name)) {
					found = true;
					// Replace if existing name is in lower-case and new name is in upper-case
					if (!ev.getName().equals(name) && name.equals(name.toUpperCase())) {
						// Original value is respected.
						String evOldValue = ev.getValue();
						iterator.remove();
						envVars.add(new EnvironmentVariable(name, evOldValue));
					}
					break;
				}
			}

			if (!found) {
				envVars.add(new EnvironmentVariable(name, value));
			}

		} else {
			// On non-Windows: case-sensitive check respects case (exact match).
			boolean isAbsent = envVars.stream().noneMatch(ev -> ev.getName().equals(name));
			if (isAbsent) {
				envVars.add(new EnvironmentVariable(name, value));
			}
		}
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

}
