/*******************************************************************************
 * Copyright (c) 2016-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class ParserDetectionTest {

	/**
	 * Test method for
	 * {@link org.eclipse.cdt.cmake.is.core.internal.ParserDetection#determineDetector(String, String,boolean)}
	 * .
	 */
	@Test
	public void testForCommandline_clang() {
		ParserDetection.ParserDetectionResult result = ParserDetection.determineDetector("/usr/bin/clang -C blah.c",
				null, true);
		assertNotNull(result);
	}

	@Test
	public void testForCommandline_clangplusplus() {
		ParserDetection.ParserDetectionResult result = ParserDetection.determineDetector("/usr/bin/clang++ -C blah.c",
				null, true);
		assertNotNull(result);
	}

	@Test
	public void testForCommandline_clangplusplus_basename() {
		ParserDetection.ParserDetectionResult result = ParserDetection.determineDetector("clang++ -C blah.c", null,
				false);
		assertNotNull(result);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.cdt.cmake.is.core.internal.ParserDetection#determineDetector(String, String,boolean)}
	 * . <a href="https://wiki.osdev.org/Target_Triplet"/>
	 */
	@Test
	public void testForCommandline_cross() {
		ParserDetection.ParserDetectionResult result = ParserDetection
				.determineDetector("/usr/bin/arm-none-eabi-gcc -C blah.c", null, true);
		assertNotNull(result);

		result = ParserDetection.determineDetector("/usr/bin/arm-none-eabi-gcc.exe -C blah.c", null, true);
		assertNotNull(result);
	}

	@Test
	public void testForCommandline_cross_withVersion() {
		final String versionSuffixRegex = "-?\\d+(\\.\\d+)*";

		ParserDetection.ParserDetectionResult result = ParserDetection
				.determineDetector("/usr/bin/arm-none-eabi-gcc-9.2.0 -C blah.c", versionSuffixRegex, true);
		assertNotNull(result);

		result = ParserDetection.determineDetector("/usr/bin/arm-none-eabi-gcc-9.2.0.exe -C blah.c", versionSuffixRegex,
				true);
		assertNotNull(result);
	}

	/**
	 * <a href="https://wiki.osdev.org/Target_Triplet"/>
	 */
	@Test
	public void testForCommandline_crossplusplus() {
		ParserDetection.ParserDetectionResult result = ParserDetection
				.determineDetector("/usr/bin/arm-none-eabi-g++ -C blah.c", null, true);
		assertNotNull(result);

		result = ParserDetection.determineDetector("/usr/bin/arm-none-eabi-g++.exe -C blah.c", null, true);
		assertNotNull(result);
	}

	/**
	 * <a href="https://wiki.osdev.org/Target_Triplet"/>
	 */
	@Test
	public void testForCommandline_crossplusplus_withVersion() {
		final String versionSuffixRegex = "-?\\d+(\\.\\d+)*";

		ParserDetection.ParserDetectionResult result = ParserDetection
				.determineDetector("/usr/bin/arm-none-eabi-g++-9.2.0 -C blah.c", versionSuffixRegex, true);
		assertNotNull(result);

		result = ParserDetection.determineDetector("/usr/bin/arm-none-eabi-g++-9.2.0.exe -C blah.c", versionSuffixRegex,
				true);
		assertNotNull(result);
	}

	/**
	 * <a href="https://wiki.osdev.org/Target_Triplet"/>
	 */
	@Test
	public void testForCommandline_crossplusplus_basename() {
		ParserDetection.ParserDetectionResult result = ParserDetection.determineDetector("arm-none-eabi-g++ -C blah.c",
				null, false);
		assertNotNull(result);

		result = ParserDetection.determineDetector("arm-none-eabi-g++.exe -C blah.c", null, false);
		assertNotNull(result);
	}

	@Test
	@Ignore("Requires NFTS to run")
	public void testForCommandline_MsdosShortNames() {
		ParserDetection.ParserDetectionResult result = ParserDetection
				.determineDetector("C:\\PROGRA2\\Atmel\\AVR8-G1\\bin\\AVR-G_~1.EXE -C blah.c", null, true);
		assertNotNull(result);
	}

	@Test
	public void testForCommandline_withVersion() {
		final String versionSuffixRegex = "-?\\d+(\\.\\d+)*";

		ParserDetection.ParserDetectionResult result = ParserDetection.determineDetector("/usr/bin/cc-4.1 -C blah.c",
				versionSuffixRegex, false);
		assertNotNull(result);

		result = ParserDetection.determineDetector("/usr/bin/cc-4.1.exe -C blah.c", versionSuffixRegex, true);
		assertNotNull(result);

		result = ParserDetection.determineDetector("/usr/bin/c++-4.1 -C blah.c", versionSuffixRegex, false);
		assertNotNull(result);

		result = ParserDetection.determineDetector("/usr/bin/c++-4.1.exe -C blah.c", versionSuffixRegex, true);
		assertNotNull(result);

		// clang for issue #43
		result = ParserDetection.determineDetector("/usr/local/bin/clang++40 -C blah.c", versionSuffixRegex, false);
		assertNotNull(result);
		result = ParserDetection.determineDetector("/usr/local/bin/clang++40 -C blah.c", "40", false);
		// result = ParserDetection.determineDetector("/usr/local/bin/clang++40
		// -I/home/me/workspace/first/test/../utility
		// -I/home/me/workspace/first/test/../include -I/home/me/workspace/first/test -g
		// -std=c++1y -stdlib=libc++ -include-pch
		// /home/me/workspace/first/build/Debug/test/catch.hpp.pch -include-pch
		// /home/me/workspace/first/build/Debug/test/pch.hpp.pch -o
		// CMakeFiles/first_test.test.dir/__/utility/fun.cpp.o -c
		// /home/me/workspace/first/utility/fun.cpp",
		// "40", false);
		assertNotNull(result);

		result = ParserDetection.determineDetector("/apps/tools/cent_os72/binlinks/g++-7.1 "
				+ "-I/apps/tools/cent_os72/thirdparty/boost/boost_1_64_0/include "
				+ "-I/home/XXX/repositories/bepa/common/include -g -Wall "
				+ "-c /home/XXX/repositories/bepa/common/settings/src/settings.cpp", versionSuffixRegex, true);
		assertNotNull(result);
	}

	@Test
	public void testForCommandline_quoted() {
		String more = " -DFoo=bar \"quoted\" 'quoted' -C blah.c";
		String[] quotes = { "\"", "'" };

		String name = "/us r/bi n/cc";

		for (String quote : quotes) {
			String args = String.format(Locale.ROOT, "%1$s%2$s%1$s %3$s", quote, name, more);
			ParserDetection.ParserDetectionResult result = ParserDetection.determineDetector(args, null, false);
			assertNotNull("Command in quotes= " + quote, result);
			assertEquals(name, result.getCommandLine().getCommand());
		}
		// NOTE: detecting just 'cc' in quotes does not work, but no on of a sane mind
		// would have compilers with spaces in
		// the name

		name += ".exe";
		for (String quote : quotes) {
			String args = String.format(Locale.ROOT, "%1$s%2$s%1$s %3$s", quote, name, more);
			ParserDetection.ParserDetectionResult result = ParserDetection.determineDetector(args, null, false);
			assertNotNull("Command in quotes= " + quote, result);
			assertEquals(name, result.getCommandLine().getCommand());
		}

		name = "C:\\us r\\bi n\\cc";
		for (String quote : quotes) {
			String args = String.format(Locale.ROOT, "%1$s%2$s%1$s %3$s", quote, name, more);
			ParserDetection.ParserDetectionResult result = ParserDetection.determineDetector(args, null, true);
			assertNotNull("Command in quotes= " + quote, result);
			assertEquals(name, result.getCommandLine().getCommand());
		}

		name += ".exe";
		for (String quote : quotes) {
			String args = String.format(Locale.ROOT, "%1$s%2$s%1$s %3$s", quote, name, more);
			ParserDetection.ParserDetectionResult result = ParserDetection.determineDetector(args, null, true);
			assertNotNull("Command in quotes= " + quote, result);
			assertEquals(name, result.getCommandLine().getCommand());
		}
	}

	/** Tests whether tool detection regex is too greedy, */
	@Test
	public void testForCommandline_greedyness() {
		String more = " -DFoo=bar -I /opt/mingw53_32/mkspecs/win32-c++ -C blah.c";

		String name = "/usr/bin/c++";
		ParserDetection.ParserDetectionResult result = ParserDetection.determineDetector(name + more, null, false);
		assertNotNull(result);
		assertEquals(name, result.getCommandLine().getCommand());

		result = ParserDetection.determineDetector("\"" + name + "\"" + more, null, false);
		assertNotNull(result);
		assertEquals(name, result.getCommandLine().getCommand());

		// with filename extension
		name += ".exe";
		result = ParserDetection.determineDetector(name + more, null, true);
		assertNotNull(result);
		assertEquals(name, result.getCommandLine().getCommand());

		result = ParserDetection.determineDetector("\"" + name + "\"" + more, null, true);
		assertNotNull(result);
		assertEquals(name, result.getCommandLine().getCommand());
	}
}
