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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.cdt.build.gcc.core.ClangToolChain;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.runtime.Platform;
import org.junit.Test;

/**
 * Tests for org.eclipse.cdt.build.gcc.core.ClangToolChain
 * Tests that the environment variables CC and CXX are set correctly.
 */
public class TestClangToolChain {

	/**
	 * Tests:
	 * ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch, IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/clang.exe
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/clang.exe", "CXX=a/clang++.exe"
	 */
	@Test
	public void clangExe() throws Exception {
		IToolChain tc = new ClangToolChain(null, Paths.get("a/clang.exe"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/clang.exe"), //
				new EnvironmentVariable("CXX", "a/clang++.exe")));
	}

	/**
	 * Tests:
	 * ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch, IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=null
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() is null
	 */
	@Test
	public void nullPathToToolChain() throws Exception {
		IToolChain tc = new ClangToolChain(null, (java.nio.file.Path) null, null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(variables, is(nullValue()));
	}

	/**
	 * Tests:
	 * ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch, IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/clang++.exe
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/clang.exe", "CXX=a/clang++.exe"
	 */
	@Test
	public void clangPPExe() throws Exception {
		IToolChain tc = new ClangToolChain(null, Paths.get("a/clang++.exe"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/clang.exe"), //
				new EnvironmentVariable("CXX", "a/clang++.exe")));
	}

	/**
	 * Tests:
	 * ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch, IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/clang (no extension)
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/clang", "CXX=a/clang++"
	 */
	@Test
	public void clang() throws Exception {
		IToolChain tc = new ClangToolChain(null, Paths.get("a/clang"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/clang"), //
				new EnvironmentVariable("CXX", "a/clang++")));
	}

	/**
	 * Tests:
	 * ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch, IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/clang++  (no extension)
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/clang", "CXX=a/clang++"
	 */
	@Test
	public void clangPP() throws Exception {
		IToolChain tc = new ClangToolChain(null, Paths.get("a/clang++"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/clang"), //
				new EnvironmentVariable("CXX", "a/clang++")));
	}

	/**
	 * Tests:
	 * ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch, IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/c++.exe
	 * Where envVars is null.
	 *
	 * Expected:
	 *   Unsupported compiler. No compiler overrides added.
	 *   IToolChain.getVariables() has only the PATH item.
	 */
	@Test
	public void cPPExe() throws Exception {
		IToolChain tc = new ClangToolChain(null, Paths.get("a/c++.exe"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables),
				hasItems(new EnvironmentVariable("PATH", "a", IEnvironmentVariable.ENVVAR_PREPEND, null)));
	}

	/**
	 * Tests:
	 * ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch, IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/cc
	 * Where envVars is null.
	 *
	 * Expected:
	 *   Unsupported compiler. No compiler overrides added.
	 *   IToolChain.getVariables() has only the PATH item.
	 */
	@Test
	public void cc() throws Exception {
		IToolChain tc = new ClangToolChain(null, Paths.get("a/cc"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables),
				hasItems(new EnvironmentVariable("PATH", "a", IEnvironmentVariable.ENVVAR_PREPEND, null)));
	}

	/**
	 * Tests:
	 * ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch, IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/clang.exe
	 * Where envVars has items "CC=CCtestvalue".
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=CCtestvalue", "CXX=a/clang++.exe"
	 */
	@Test
	public void clangExeCCExists() throws Exception {
		IEnvironmentVariable[] envVars = { new EnvironmentVariable("CC", "CCtestvalue") };
		IToolChain tc = new ClangToolChain(null, Paths.get("a/clang.exe"), null, envVars);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "CCtestvalue"), //
				new EnvironmentVariable("CXX", "a/clang++.exe")));
	}

	/**
	 * Tests:
	 * ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch, IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/clang++.exe
	 * Where envVars has items "CC=CCtestvalue", "CXX=CXXtestvalue".
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=CCtestvalue", "CXX=CXXtestvalue"
	 */
	@Test
	public void clangPPExeCCCXXExists() throws Exception {
		IEnvironmentVariable[] envVars = { new EnvironmentVariable("CC", "CCtestvalue"),
				new EnvironmentVariable("CXX", "CXXtestvalue") };
		IToolChain tc = new ClangToolChain(null, Paths.get("a/clang++.exe"), null, envVars);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "CCtestvalue"), //
				new EnvironmentVariable("CXX", "CXXtestvalue")));
	}

	/**
	 * Tests:
	 * ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch, IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/clang
	 * Where envVars has items "cc=cctestvalue" (lowercase cc).
	 *
	 * Expected:
	 *   envVars has items "CC=CCtestvalue", "CXX=a/clang++.exe"
	 * 	(Windows):     IToolChain.getVariables() has items "cc=cctestvalue", "CXX=clang++"
	 *  (non-Windows): IToolChain.getVariables() has items "cc=cctestvalue", "CC=clang", "CXX=clang++"
	 */
	@Test
	public void clangccExists() throws Exception {
		IEnvironmentVariable[] envVars = { new EnvironmentVariable("cc", "cctestvalue") };
		IToolChain tc = new ClangToolChain(null, Paths.get("a/clang"), null, envVars);
		IEnvironmentVariable[] variables = tc.getVariables();
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			/*
			 * Windows: case-insensitive environment; cc is treated the same to CC; CC is not added.
			 */
			assertThat(Arrays.asList(variables), not(hasItem(new EnvironmentVariable("CC", "cctestvalue"))));
			assertThat(Arrays.asList(variables), hasItems( //
					new EnvironmentVariable("cc", "cctestvalue"), //
					new EnvironmentVariable("CXX", "a/clang++")));
			// the clang override value not applied because the envVar already existed.
			assertThat(Arrays.asList(variables), not(hasItem(new EnvironmentVariable("CC", "a/clang"))));
		} else {
			/*
			 * Non-Windows: case-sensitive environment; cc is treated differently to CC; CC is added in addition to any cc.
			 */
			assertThat(Arrays.asList(variables), hasItems(//
					new EnvironmentVariable("cc", "cctestvalue"), //
					new EnvironmentVariable("CC", "a/clang"), //
					new EnvironmentVariable("CXX", "a/clang++")));
		}
	}

	/**
	 * Tests:
	 * ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch, IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/clang-18.exe  (clang with version)
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/clang-18.exe", "CXX=a/clang++-18.exe"
	 */
	@Test
	public void clangVersionExe() throws Exception {
		IToolChain tc = new ClangToolChain(null, Paths.get("a/clang-18.exe"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/clang-18.exe"), //
				new EnvironmentVariable("CXX", "a/clang++-18.exe")));
	}

	/**
	 * Tests:
	 * ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch, IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/clang++-18  (clang++ with version, no extension)
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/clang-18.exe", "CXX=a/clang++-18.exe"
	 */
	@Test
	public void clangPPVersion() throws Exception {
		IToolChain tc = new ClangToolChain(null, Paths.get("a/clang++-18"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/clang-18"), //
				new EnvironmentVariable("CXX", "a/clang++-18")));
	}
}
