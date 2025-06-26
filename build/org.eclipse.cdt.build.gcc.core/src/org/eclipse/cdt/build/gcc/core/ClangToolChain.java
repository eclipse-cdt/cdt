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

	public static final String TYPE_ID = "org.eclipse.cdt.build.clang"; //$NON-NLS-1$

	/**
	 * @param provider The toolchain provider which can discover toolchains.
	 * @param pathToToolChain Path to the filename of the clang or clang++ compiler. It may or may not have an extension.
	 * @param arch The CPU architecture the toolchain supports.
	 * @param envVars Environment variables originate from the core build toolchain provider, either "Discovered
	 *   toolchain" or"User defined toolchain". It is not expected to contain OS system envVars.
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
	 * Otherwise, convert between clang and clang++ while extensions.
	 *
	 * @param pathToToolChain Path to the filename of the clang or clang++ compiler. It may or may not have an extension.
	 *   Supports clang toolchains.
	 *   Does not support cc or c++. gcc and g++ are supported in {@link GCCToolChain}.
	 *
	 * @param desiredType {@link #CC} to convert clang++ to clang, {@link #CXX} to convert clang to clang++.
	 * @return mapped compiler path to the filename of the desired compiler.
	 *   Returns null if desiredType is not recognised. Returns null if originalPath is to an unsupported compiler.
	 */
	private static IPath mapCompilerPath(IPath pathToToolChain, String desiredType) {
		if (!CC.equals(desiredType) && !CXX.equals(desiredType)) {
			// Invalid desired compiler type
			return null;
		}

		String compilerType = getGnuCompilerType(pathToToolChain);
		if (compilerType == null) {
			// Unsupported compiler type
			return null;
		}

		// Already matches desiredType - return original path
		if (compilerType.equals(desiredType)) {
			return pathToToolChain;
		}

		String filename = pathToToolChain.removeFileExtension().lastSegment();
		String extension = pathToToolChain.getFileExtension();
		String mappedFilename;

		if (CC.equals(desiredType)) {
			/*
			 * Convert clang++ compiler to clang compiler.
			 * Matches exactly two '+' chars at the end of the string and replaces with "".
			 */
			mappedFilename = filename.replaceAll("\\+{2}$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			//Convert clang compiler to clang++ compiler
			mappedFilename = filename + "++"; //$NON-NLS-1$
		}

		// Reconstruct new path, possibly with extension.
		IPath mappedPath = pathToToolChain.removeLastSegments(1).append(mappedFilename);
		return (extension != null) ? mappedPath.addFileExtension(extension) : mappedPath;
	}

	/**
	 * Determines if the given path is a clang or clang++ compiler binary.
	 * @param compilerPath the path to the compiler binary
	 * @return "CC" for clang, "CXX" for clang++, otherwise null.
	 */
	private static String getGnuCompilerType(IPath compilerPath) {
		if (compilerPath == null || compilerPath.lastSegment() == null) {
			return null;
		}
		String filename = compilerPath.removeFileExtension().lastSegment();
		if (filename.endsWith("clang++")) { //$NON-NLS-1$
			return CXX;
		} else if (filename.endsWith("clang")) { //$NON-NLS-1$
			return CC;
		} else {
			return null;
		}
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

}
