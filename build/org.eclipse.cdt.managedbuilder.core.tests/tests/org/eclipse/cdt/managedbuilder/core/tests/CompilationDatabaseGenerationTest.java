/*******************************************************************************
 * Copyright (c) 2019, 2020 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.jsoncdb.CompilationDatabaseInformation;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder;
import org.eclipse.cdt.managedbuilder.testplugin.AbstractBuilderTest;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class CompilationDatabaseGenerationTest extends AbstractBuilderTest {

	/**
	 * Tests generation of compile_commands.json in "build" folder
	 */
	@Test
	public void testCompilationDatabaseGeneration() throws Exception {
		setWorkspace("regressions");
		final IProject app = loadProject("helloworldC");
		setGenerateFileOptionEnabled(true);
		app.build(IncrementalProjectBuilder.FULL_BUILD, null);
		IFile compilationDatabase = app.getFile("Debug/compile_commands.json");
		assertTrue(compilationDatabase.exists());
	}

	/**
	 * Tests format for compile_commands.json. JSON array is expected, containing an element for the c file
	 */
	@Test
	public void testJsonFormat() throws Exception {
		setWorkspace("regressions");
		final IProject app = loadProject("helloworldC");
		setGenerateFileOptionEnabled(true);
		app.build(IncrementalProjectBuilder.FULL_BUILD, null);
		IFile commandsFile = app.getFile("Debug/compile_commands.json");
		if (commandsFile.exists()) {

			try (FileReader reader = new FileReader(commandsFile.getLocation().toFile())) {
				Gson gson = new Gson();
				JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
				for (JsonElement element : jsonArray) {
					CompilationDatabaseInformation compileCommand = gson.fromJson(element,
							CompilationDatabaseInformation.class);

					assertTrue(compileCommand.directory() != null && !compileCommand.directory().isEmpty());
					assertTrue(compileCommand.command() != null && !compileCommand.command().isEmpty());
					assertTrue(compileCommand.file() != null && !compileCommand.file().isEmpty());
					assertTrue(compileCommand.file().endsWith("src/helloworldC.c"));
				}

			}

		}
	}

	/**
	 * Test that compile_commands.json is correctly generated when more than one .c file is present as a source file
	 */
	@Test
	public void testMultipleFiles() throws Exception {
		setWorkspace("regressions");
		final IProject app = loadProject("helloworldC");
		IFile aFile = ManagedBuildTestHelper.createFile(app, "src/newFile.c");
		setGenerateFileOptionEnabled(true);
		app.build(IncrementalProjectBuilder.FULL_BUILD, null);
		IFile commandsFile = app.getFile("Debug/compile_commands.json");
		int numberOfElementsFound = 0;
		boolean helloworldCIsPresent = false;
		boolean newFileIsPresent = false;
		try (FileReader reader = new FileReader(commandsFile.getLocation().toFile())) {
			Gson gson = new Gson();
			JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
			System.out.println(jsonArray);
			for (JsonElement element : jsonArray) {
				CompilationDatabaseInformation compileCommand = gson.fromJson(element,
						CompilationDatabaseInformation.class);
				numberOfElementsFound++;
				if (compileCommand.file().endsWith("helloworldC.c")) {
					helloworldCIsPresent = true;
				}
				if (compileCommand.file().endsWith("newFile.c")) {
					newFileIsPresent = true;
				}
			}
			assertEquals(2, numberOfElementsFound);
			assertTrue(helloworldCIsPresent);
			assertTrue(newFileIsPresent);
		}
	}

	/**
	 * Tests that cpp files are handled by compile_commands.json file generator
	 */
	@Test
	public void isCPPFileAllowed() throws Exception {
		setWorkspace("regressions");
		final IProject app = loadProject("helloworldCPP");
		setGenerateFileOptionEnabled(true);
		app.build(IncrementalProjectBuilder.FULL_BUILD, null);
		IFile commandsFile = app.getFile("Debug/compile_commands.json");
		if (commandsFile.exists()) {

			try (FileReader reader = new FileReader(commandsFile.getLocation().toFile())) {
				Gson gson = new Gson();
				JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
				for (JsonElement element : jsonArray) {
					CompilationDatabaseInformation compileCommand = gson.fromJson(element,
							CompilationDatabaseInformation.class);

					assertTrue(compileCommand.directory() != null && !compileCommand.directory().isEmpty());
					assertTrue(compileCommand.command() != null && !compileCommand.command().isEmpty());
					assertTrue(compileCommand.file() != null && !compileCommand.file().isEmpty());
					assertTrue(compileCommand.file().endsWith("src/helloworldCPP.cpp"));
				}

			}
		}
	}

	/**
	 * Tests that compilation database is not generated when feature is disabled
	 */
	@Test
	public void testCompilationDatabaseGenerationNotEnabled() throws Exception {
		setWorkspace("regressions");
		final IProject app = loadProject("helloworldC");
		setGenerateFileOptionEnabled(false);
		app.build(IncrementalProjectBuilder.FULL_BUILD, null);
		IFile compilationDatabase = app.getFile("Debug/compile_commands.json");
		assertFalse(compilationDatabase.exists());
	}

	private static void setGenerateFileOptionEnabled(boolean value) {
		IPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,
				"org.eclipse.cdt.managedbuilder.ui");
		preferenceStore.setValue(CommonBuilder.COMPILATION_DATABASE_ENABLEMENT, value);
	}

	@AfterEach
	public void restoreDefaultForGenerateFile() {
		IPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,
				"org.eclipse.cdt.managedbuilder.ui");
		preferenceStore.setToDefault(CommonBuilder.COMPILATION_DATABASE_ENABLEMENT);
	}

	@Test
	public void testCompilerPath() throws Exception {
		setWorkspace("regressions");
		final IProject app = loadProject("helloworldC");
		setGenerateFileOptionEnabled(true);
		app.build(IncrementalProjectBuilder.FULL_BUILD, null);
		IFile commandsFile = app.getFile("Debug/compile_commands.json");

		if (commandsFile.exists()) {
			try (FileReader reader = new FileReader(commandsFile.getLocation().toFile())) {
				Gson gson = new Gson();
				JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
				for (JsonElement element : jsonArray) {
					CompilationDatabaseInformation compileCommand = gson.fromJson(element,
							CompilationDatabaseInformation.class);
					String command = compileCommand.command();
					String[] commandParts = CommandLineUtil.argumentsToArray(command);
					String compilerPath = commandParts[0];
					assertNotNull("Compiler path should not be null", compilerPath);
					assertFalse(compilerPath.isEmpty(), "Compiler path should not be empty");
					IPath path = new Path(compilerPath);
					boolean isAbsolute = path.isAbsolute();
					assertTrue(isAbsolute, "Path should be absolute: " + path);
				}
			}
		}
	}

	@Test
	public void testMakeFileGenerationOff() throws Exception {
		setWorkspace("regressions");
		final IProject app = loadProject("helloworldC");
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(app);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		IToolChain toolChain = cfg.getToolChain();
		IBuilder builder = toolChain.getBuilder();
		setGenerateFileOptionEnabled(true);
		builder.setManagedBuildOn(false);
		app.build(IncrementalProjectBuilder.FULL_BUILD, null);
		assertFalse(app.getFile("Debug/compile_commands.json").exists());
	}

	@Test
	public void testGetCompilerArgsWithSpacesAndQuotes() {
		String[] commandLine = { "gcc", "My file.c", "-o", "My file.o", "-DNAME=\"My Value\"",
				"C:\\Program Files\\Lib" };
		List<String> argsList = Arrays.asList(commandLine).subList(1, commandLine.length);
		String result = escapeArgsForCompileCommand(argsList);
		String expected = "\"My file.c\" -o \"My file.o\" \"-DNAME=\\\"My Value\\\"\" \"C:\\\\Program Files\\\\Lib\"";

		assertEquals(expected, result);
	}

	private static String escapeArgsForCompileCommand(List<String> args) {
		return args.stream().map(arg -> {
			if (arg.contains(" ") || arg.contains("\"") || arg.contains("\\")) {
				String escaped = arg.replace("\\", "\\\\").replace("\"", "\\\"");
				return "\"" + escaped + "\"";
			} else {
				return arg;
			}
		}).collect(Collectors.joining(" "));
	}

}
