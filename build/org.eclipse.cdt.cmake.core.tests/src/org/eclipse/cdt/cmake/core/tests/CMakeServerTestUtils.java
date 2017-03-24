/*******************************************************************************
 * Copyright (c) 2017 IAR Systems AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jesper Eskilson (IAR Systems AB) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.hamcrest.Matcher;

public class CMakeServerTestUtils {
	/**
	 * Return the system path as a stream of Path objects
	 * 
	 * @return
	 */
	public static Stream<Path> getSystemPath() {
		return Arrays.stream(Optional.ofNullable(System.getenv("PATH")).orElseGet(() -> System.getenv("Path"))
				.split(File.pathSeparator)).filter(s -> s.length() > 0).map(p -> Paths.get(p));
	}

	/**
	 * Return a stream of CMake executables found in PATH
	 * 
	 * @return
	 */
	public static Stream<File> getCMakeInSystemPath() {
		return getSystemPath().flatMap(p -> Stream.of(new File(p.toFile(), "cmake"), new File(p.toFile(), "cmake.exe")))
				.filter(f -> f.canExecute());
	}

	/**
	 * Return the CMake version given the path to the CMake executable.
	 * 
	 * @param cmakeExe
	 * @return An optional containing the version, or empty if the version could not
	 *         be obtained.
	 */
	public static Optional<String> getCMakeVersion(File cmakeExe) {
		ProcessBuilder pb = new ProcessBuilder(cmakeExe.toString(), "--version");
		try {
			Process p = pb.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				return reader.lines().map(s -> s.replaceFirst("cmake version (.*)", "$1")).findFirst();
			}
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	public static Optional<File> getCompatibleCMakeFromSystemPath(String minimumVersion) {
		/*
		 * Take all CMake found in PATH, filter out all the incompatible ones, and
		 * return the first one of the ones which are left.
		 */
		return getCMakeInSystemPath().filter(cmakeExe -> getCMakeVersion(cmakeExe)
				.map(version -> VersionComparator.compareVersions(version, minimumVersion))
				.filter(cmpResult -> cmpResult >= 0).isPresent()).findFirst();
	}

	public static void waitUntil(Object object, Matcher<?> matcher) throws InterruptedException, TimeoutException {
		int timeout = 5000;
		long start = System.currentTimeMillis();

		while (true) {
			if (matcher.matches(object))
				return;

			Thread.sleep(10);
			if (System.currentTimeMillis() - start > timeout) {
				throw new TimeoutException(String.format("Timeout while waiting for %s to match %s", object, matcher));
			}
		}
	}

}
