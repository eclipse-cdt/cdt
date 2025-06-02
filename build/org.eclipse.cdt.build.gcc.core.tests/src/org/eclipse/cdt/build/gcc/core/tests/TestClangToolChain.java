/*******************************************************************************
 * Copyright (c) 2025 Renesas Electronics Europe.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.util.Arrays;

import org.eclipse.cdt.build.gcc.core.ClangToolChain;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.runtime.Platform;
import org.junit.Test;

/**
 * Tests for org.eclipse.cdt.build.gcc.core.ClangToolChain
 * Tests that the environment variables CC and CXX are set to clang values.
 */
public class TestClangToolChain {

	/**
	 * Tests ClangToolChain.ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,
			IEnvironmentVariable[] envVars).
	 * Where envVars is null.
	 * Expected:
	 * 	envVars contains "CC=clang", "CXX=clang++"
	 */
	@Test
	public void clangContainsEnvVarsNull() throws Exception {
		IToolChain tc = new ClangToolChain(null, null, null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), contains(//
				new EnvironmentVariable("CC", "clang"), //
				new EnvironmentVariable("CXX", "clang++")));
	}

	/**
	 * Tests ClangToolChain.ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,
			IEnvironmentVariable[] envVars).
	 * Where envVars is empty.
	 * Expected:
	 * 	envVars contains "CC=clang", "CXX=clang++"
	 */
	@Test
	public void clangContainsEnvVarsEmpty() throws Exception {
		IEnvironmentVariable[] envVars = {};
		IToolChain tc = new ClangToolChain(null, null, null, envVars);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), contains(//
				new EnvironmentVariable("CC", "clang"), //
				new EnvironmentVariable("CXX", "clang++")));
	}

	/**
	 * Tests ClangToolChain.ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,
			IEnvironmentVariable[] envVars).
	 * Where envVars contains "cc=testvalue"
	 * Expected:
	 * 	(Windows):     envVars contains "CC=testvalue", "CXX=clang++"
	 *  (non-Windows): envVars contains "cc=testvalue", "CC=clang", "CXX=clang++"
	 */
	@Test
	public void clangContainscc() throws Exception {
		IEnvironmentVariable[] envVars = { new EnvironmentVariable("cc", "testvalue") };
		IToolChain tc = new ClangToolChain(null, null, null, envVars);
		IEnvironmentVariable[] variables = tc.getVariables();

		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			/*
			 * Windows: case-insensitive environment; cc is replaced with uppercase CC. value remains unchanged.
			 */
			assertThat(Arrays.asList(variables), not(hasItem(new EnvironmentVariable("cc", "testvalue"))));
			assertThat(Arrays.asList(variables),
					contains(new EnvironmentVariable("CC", "testvalue"), new EnvironmentVariable("CXX", "clang++")));
			// the clang override value not applied because the envVar already existed.
			assertThat(Arrays.asList(variables), not(hasItem(new EnvironmentVariable("CC", "clang"))));
		} else {
			/*
			 * Non-Windows: case-sensitive environment; cc is not replaced with uppercase CC. CC is added in addition to any cc.
			 */
			assertThat(Arrays.asList(variables), contains(//
					new EnvironmentVariable("cc", "testvalue"), //
					new EnvironmentVariable("CC", "clang"), //
					new EnvironmentVariable("CXX", "clang++")));
		}
	}

	/**
	 * Tests ClangToolChain.ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,
			IEnvironmentVariable[] envVars).
	 * Where envVars contains "CC=testvalue"
	 * Expected:
	 * 	envVars contains "CC=testvalue", "CXX=clang++"
	 */
	@Test
	public void clangContainsCC() throws Exception {
		IEnvironmentVariable[] envVars = { new EnvironmentVariable("CC", "testvalue") };
		IToolChain tc = new ClangToolChain(null, null, null, envVars);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), contains(//
				new EnvironmentVariable("CC", "testvalue"), //
				new EnvironmentVariable("CXX", "clang++")));
	}
}
