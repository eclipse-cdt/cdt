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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.junit.Test;

/**
 * Tests for org.eclipse.cdt.build.gcc.core.GCCToolChain
 * Tests that the environment variables CC and CXX are set correctly.
 */
public class TestGCCToolChain {

	/**
	 * Tests:
	 * GCCToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,	IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/gcc.exe
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/gcc.exe", "CXX=a/g++.exe"
	 */
	@Test
	public void gccExe() throws Exception {
		IToolChain tc = new GCCToolChain(null, Paths.get("a/gcc.exe"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/gcc.exe"), //
				new EnvironmentVariable("CXX", "a/g++.exe")));
	}

	/**
	 * Tests:
	 * GCCToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,	IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=null
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() is null
	 */
	@Test
	public void nullPathToToolChain() throws Exception {
		IToolChain tc = new GCCToolChain(null, (java.nio.file.Path) null, null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(variables, is(nullValue()));
	}

	/**
	 * Tests:
	 * GCCToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,	IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/g++.exe
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/gcc.exe", "CXX=a/g++.exe"
	 */
	@Test
	public void gPPExe() throws Exception {
		IToolChain tc = new GCCToolChain(null, Paths.get("a/g++.exe"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/gcc.exe"), //
				new EnvironmentVariable("CXX", "a/g++.exe")));
	}

	/**
	 * Tests:
	 * GCCToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,	IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/arm-none-eabi-gcc.exe (a cross gcc tc with extension)
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/arm-none-eabi-gcc.exe", "CXX=a/arm-none-eabi-g++.exe"
	 */
	@Test
	public void crossGccExe() throws Exception {
		IToolChain tc = new GCCToolChain(null, Paths.get("a/arm-none-eabi-gcc.exe"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/arm-none-eabi-gcc.exe"), //
				new EnvironmentVariable("CXX", "a/arm-none-eabi-g++.exe")));
	}

	/**
	 * Tests:
	 * GCCToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,	IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/arm-none-eabi-g++ (a cross g++ tc with no extension)
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/arm-none-eabi-gcc", "CXX=a/arm-none-eabi-g++"
	 */
	@Test
	public void crossGPP() throws Exception {
		IToolChain tc = new GCCToolChain(null, Paths.get("a/arm-none-eabi-g++"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/arm-none-eabi-gcc"), //
				new EnvironmentVariable("CXX", "a/arm-none-eabi-g++")));
	}

	/**
	 * Tests:
	 * GCCToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,	IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/c++.exe
	 * Where envVars is null.
	 *
	 * Expected:
	 *   Unsupported compiler. No compiler overrides added.
	 *   IToolChain.getVariables() has only the PATH item.
	 */
	@Test
	public void cPPExe() throws Exception {
		IToolChain tc = new GCCToolChain(null, Paths.get("a/c++.exe"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables),
				hasItems(new EnvironmentVariable("PATH", "a", IEnvironmentVariable.ENVVAR_PREPEND, null)));
	}

	/**
	 * Tests:
	 * GCCToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,	IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/cc
	 * Where envVars is null.
	 *
	 * Expected:
	 *   Unsupported compiler. No compiler overrides added.
	 *   IToolChain.getVariables() has only the PATH item.
	 */
	@Test
	public void cc() throws Exception {
		IToolChain tc = new GCCToolChain(null, Paths.get("a/cc"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables),
				hasItems(new EnvironmentVariable("PATH", "a", IEnvironmentVariable.ENVVAR_PREPEND, null)));
	}

	/**
	 * Tests:
	 * GCCToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,	IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/gcc-18.exe (gcc with version)
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/gcc-18.exe", "CXX=a/g++-18.exe"
	 */
	@Test
	public void gccVersionExe() throws Exception {
		IToolChain tc = new GCCToolChain(null, Paths.get("a/gcc-18.exe"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/gcc-18.exe"), //
				new EnvironmentVariable("CXX", "a/g++-18.exe")));
	}

	/**
	 * Tests:
	 * GCCToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,	IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/g++-18 (g++ with version)
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/gcc-18", "CXX=a/g++-18"
	 */
	@Test
	public void gPPVersion() throws Exception {
		IToolChain tc = new GCCToolChain(null, Paths.get("a/g++-18"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/gcc-18"), //
				new EnvironmentVariable("CXX", "a/g++-18")));
	}

	/**
	 * Tests:
	 * GCCToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,	IEnvironmentVariable[] envVars)
	 * Where pathToToolChain=a/arm-none-eabi-g++-18 (a cross g++ tc with no extension)
	 * Where envVars is null.
	 *
	 * Expected:
	 *   IToolChain.getVariables() has items "CC=a/arm-none-eabi-gcc-18", "CXX=a/arm-none-eabi-g++-18"
	 */
	@Test
	public void crossGPPVersion() throws Exception {
		IToolChain tc = new GCCToolChain(null, Paths.get("a/arm-none-eabi-g++-18"), null, null);
		IEnvironmentVariable[] variables = tc.getVariables();
		assertThat(Arrays.asList(variables), hasItems( //
				new EnvironmentVariable("CC", "a/arm-none-eabi-gcc-18"), //
				new EnvironmentVariable("CXX", "a/arm-none-eabi-g++-18")));
	}
}
