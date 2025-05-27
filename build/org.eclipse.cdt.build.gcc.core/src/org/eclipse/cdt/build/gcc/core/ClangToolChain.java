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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * The Clang toolchain. There's little different from the GCC toolchain other
 * than the toolchain type and name.
 *
 * @author dschaefer
 *
 */
public class ClangToolChain extends GCCToolChain {
	private static final String CC = "CC"; //$NON-NLS-1$
	private static final String CXX = "CXX"; //$NON-NLS-1$
	/**
	 * Pattern to handle clang (CC) toolchain filename. Following forms supported:
	 * Starts with clang, optional -number version suffix, optional .exe file extension, nothing else before or after.
	 * For example:
	 *   clang, clang.exe, clang-18, clang-18.exe
	 * Use {@code matcher.group(0)} to retrieve the full filename.
	 */
	private static final Pattern CC_PATTERN = Pattern.compile("^clang(?:-\\d+)?(?:\\.exe)?$"); //$NON-NLS-1$

	/**
	 * Pattern to handle clang++ (CXX) toolchain filename. Following forms supported:
	 * Starts with clang++, optional -number version suffix, optional .exe file extension, nothing else before or after.
	 * For example:
	 *   clang++, clang++.exe, clang++-18, clang++-18.exe
	 * Use {@code matcher.group(0)} to retrieve the full filename.
	 */
	private static final Pattern CXX_PATTERN = Pattern.compile("^clang\\+\\+(?:-\\d+)?(?:\\.exe)?$"); //$NON-NLS-1$

	public static final String TYPE_ID = "org.eclipse.cdt.build.clang"; //$NON-NLS-1$

	/**
	 * @param provider The toolchain provider which can discover toolchains.
	 * @param pathToToolChain Path to the filename of the clang or clang++ compiler. It may or may not have a version
	 *   suffix. It may or may not have an extension.
	 * @param arch The CPU architecture the toolchain supports.
	 * @param envVars Environment variables originate from the core build toolchain provider, either "Discovered
	 *   toolchain" or "User defined toolchain". It is not expected to contain OS system envVars.
	 *   When the tc is a user defined, it is possible to specify arbitrary environment variables, meaning the user may
	 *   have already defined CC/CXX. If CC/CXX is already defined then nothing is added. If CC/CXX is not defined,
	 *   they are added and set to the appropriate compiler path.
	 *   Other methods of setting the compiler in CMake (eg; CMAKE_&lt;LANG&gt;_COMPILER in CMakeLists.txt or
	 *   -DCMAKE_TOOLCHAIN_FILE=file) take precedence over these environment variables, so the user is not
	 *   inhibited by this approach.
	 */
	public ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,
			IEnvironmentVariable[] envVars) {
		super(provider, pathToToolChain, arch, addCompilerOverrides(pathToToolChain, envVars));
	}

	private static IEnvironmentVariable[] addCompilerOverrides(Path pathToToolChain, IEnvironmentVariable[] envVars) {
		if (pathToToolChain == null) {
			return envVars;
		}

		List<IEnvironmentVariable> envVarsNew = new ArrayList<>();
		// envVars can be null if the user defined tc did not specify any env variables.
		if (envVars != null) {
			envVarsNew.addAll(Arrays.asList(envVars));
		}

		for (String type : new String[] { CC, CXX }) {
			if (isAbsent(type, envVarsNew)) {
				IPath mapped = mapCompilerPath(IPath.fromPath(pathToToolChain), type);
				if (mapped != null) {
					envVarsNew.add(new EnvironmentVariable(type, mapped.toString()));
				}
			}
		}
		return envVarsNew.toArray(new EnvironmentVariable[0]);
	}

	private static boolean isAbsent(String name, List<IEnvironmentVariable> envVarsNew) {
		return envVarsNew.stream().noneMatch(ev -> {
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				return ev.getName().equalsIgnoreCase(name);
			} else {
				return ev.getName().equals(name);
			}
		});
	}

	/**
	 * Given a compiler path and a desiredType ("CC" or "CXX"), return the correct path for that type.
	 * If the path already matches the desired type, return it as-is.
	 *
	 * @param pathToToolChain Path to the filename of the CC or CXX compiler. It may or may not have an extension.
	 *   Supports clang toolchains, with optional version suffix.
	 *   Does not support cc or c++. gcc and g++ are supported in {@link GCCToolChain}.
	 *
	 * @param desiredType The desired compiler type to convert to.
	 * When {@link #CC}, clang++ or clang++-version is converted to clang or clang-version.
	 * When {@link #CXX}, clang or clang-version is converted to clang++ or clang++-version.
	 *
	 * For example:
	 *   clang, clang.exe, clang-18, clang-18.exe
	 * converts to:
	 *   clang++, clang++.exe, clang++-18, clang++-18.exe
	 *
	 * @return mapped compiler path to the filename of the desired compiler.
	 *   Returns null if desiredType is not recognised. Returns null if originalPath is to an unsupported compiler.
	 */
	private static IPath mapCompilerPath(IPath pathToToolChain, String desiredType) {
		if (!CC.equals(desiredType) && !CXX.equals(desiredType)) {
			return null; // unsupported desiredType
		}

		String filename = pathToToolChain.lastSegment();
		Matcher cxxMatcher = CXX_PATTERN.matcher(filename);
		Matcher ccMatcher = CC_PATTERN.matcher(filename);
		boolean isCXX = cxxMatcher.matches();
		boolean isCC = ccMatcher.matches();
		if (!(isCC || isCXX)) {
			return null; // unsupported compiler
		}

		if (isCXX && CC.equals(desiredType)) {
			// Get the filename part (group0) and just swap the clang++ part with clang.
			String mappedFilename = cxxMatcher.group(0).replaceFirst("^clang\\+\\+", "clang"); //$NON-NLS-1$ //$NON-NLS-2$
			return pathToToolChain.removeLastSegments(1).append(mappedFilename);
		}
		if (isCC && CXX.equals(desiredType)) {
			// Get the filename part (group0) and just swap the clang part with clang++.
			String mappedFilename = ccMatcher.group(0).replaceFirst("^clang", "clang++"); //$NON-NLS-1$ //$NON-NLS-2$
			return pathToToolChain.removeLastSegments(1).append(mappedFilename);
		}
		// Compiler already matches desired type
		return pathToToolChain;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

}
