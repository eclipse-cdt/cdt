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

import static org.eclipse.cdt.cmake.core.tests.CMakeServerTestUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.cdt.cmake.core.server.CMakeCache;
import org.eclipse.cdt.cmake.core.server.CMakeCodeModel;
import org.eclipse.cdt.cmake.core.server.CMakeConfiguration;
import org.eclipse.cdt.cmake.core.server.CMakeFileGroup;
import org.eclipse.cdt.cmake.core.server.CMakeFileSystemWatchers;
import org.eclipse.cdt.cmake.core.server.CMakeGlobalSettings;
import org.eclipse.cdt.cmake.core.server.CMakeInputs;
import org.eclipse.cdt.cmake.core.server.CMakeProgress;
import org.eclipse.cdt.cmake.core.server.CMakeServerException;
import org.eclipse.cdt.cmake.core.server.CMakeServerFactory;
import org.eclipse.cdt.cmake.core.server.CMakeTarget;
import org.eclipse.cdt.cmake.core.server.ICMakeServer;
import org.eclipse.cdt.cmake.core.server.ICMakeServerBackend;
import org.eclipse.cdt.cmake.core.server.ICMakeServerListener;
import org.eclipse.cdt.cmake.core.server.StdoutCMakeServerListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

public class CMakeServerTest {

	private ICMakeServer server;
	private ICMakeServerBackend backend;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Rule
	public TestName testName = new TestName();

	private File cmakeExe;
	private File sourceDirectory;
	private File buildDirectory;

	private String generator = "Unix Makefiles";
	private String compileFlags = "-g -O2";

	@Before
	public void setup() throws CMakeServerException {
		Optional<File> hasCMakeExe = getSystemPath()
				.flatMap(p -> Stream.of(new File(p.toFile(), "cmake"), new File(p.toFile(), "cmake.exe")))
				.filter(f -> f.canExecute()).findFirst();

		assumeTrue("Skipping test, cmake not found in PATH", hasCMakeExe.isPresent());
		cmakeExe = hasCMakeExe.get();

		backend = CMakeServerFactory.createBackend(cmakeExe, true);
		server = CMakeServerFactory.createServer();
		server.addListener(new StdoutCMakeServerListener());
	}

	@Before
	public void setupSources() throws IOException {
		sourceDirectory = folder.getRoot();
		File f = folder.newFile("CMakeLists.txt");
		Files.write(f.toPath(),
				Arrays.asList(//
						"project(\"testproj\" C CXX)", //
						"cmake_minimum_required(VERSION 3.7)", //
						String.format("add_compile_options(%s)", compileFlags), //
						"add_executable(foo foo.cpp)", //
						""));

		File src = folder.newFile("foo.cpp");
		Files.write(src.toPath(), Arrays.asList("int main() { return 42; }"));
		buildDirectory = folder.newFolder("build");
	}

	@After
	public void stopServer() throws Exception {
		server.close();
	}

	@Test(expected = CMakeServerException.class)
	public void testConfigureWithoutHandshaked() throws CMakeServerException {
		server.startServer(backend);
		server.configure(); // should throw exception
	}

	@Test
	public void testHandshake() throws CMakeServerException {
		server.startServer(backend);
		server.handshake(sourceDirectory, buildDirectory, generator);

	}

	@Test
	public void testGetSupportedProtocols() throws CMakeServerException {
		server.startServer(backend);
		assertThat(server.getSupportedProtocolVersions(), hasSize(greaterThan(0)));
	}

	@Test
	public void testGlobalSettings() throws IOException, CMakeServerException {
		server.startServer(backend);
		server.handshake(sourceDirectory, buildDirectory, generator);
		CMakeGlobalSettings settings = server.getGlobalSettings();
		assertThat(settings.generator, equalTo(generator));
	}

	@Test
	public void testConfigureAndCompute() throws Exception {
		server.startServer(backend);
		server.handshake(sourceDirectory, buildDirectory, generator);
		server.configure();
		server.compute();
		CMakeCodeModel codeModel = server.getCodeModel();
		assertThat(codeModel.configurations, hasSize(greaterThanOrEqualTo(1)));

		CMakeConfiguration configDebug = codeModel.configurations.get(0);
		assertThat(configDebug.name, anyOf(equalTo("Debug"), equalTo("")));

		CMakeTarget target = configDebug.projects.get(0).targets.get(0);
		assertThat(target.name, equalTo("foo"));

		CMakeFileGroup fileGroup = target.fileGroups.get(0);
		assertThat(fileGroup.sources, hasSize(1));
		assertThat(fileGroup.sources, hasItem(containsString("foo.c")));
		assertThat(fileGroup.compileFlags, containsString(compileFlags));
	}

	@Test
	public void testCMakeInputs() throws CMakeServerException {
		server.startServer(backend);
		server.handshake(sourceDirectory, buildDirectory, generator);
		server.configure();
		CMakeInputs inputs = server.getCMakeInputs();

		assertThat(inputs.buildFiles, hasSize(greaterThan(0)));

		// CMakeLists.txt should be among these
		assertThat("CMakeLists.txt should be among the CMake inputs",
				inputs.buildFiles.stream().flatMap(bf -> bf.sources.stream()).collect(Collectors.toList()),
				hasItem(equalTo("CMakeLists.txt")));
	}

	@Test
	public void testCache() throws CMakeServerException {
		server.startServer(backend);
		server.handshake(sourceDirectory, buildDirectory, generator);
		server.configure();
		CMakeCache cache = server.getCMakeCache();

		assertThat(cache.cache, hasSize(greaterThan(0)));
		assertThat("The build directory is correctly specified in the cache", cache.cache.stream()
				.filter(entry -> entry.key.equals("CMAKE_CACHEFILE_DIR")).map(entry -> entry.value).findFirst().get(),
				equalTo(buildDirectory.toString()));

	}

	@Test
	public void testSetGlobalSetting() throws CMakeServerException {
		server.startServer(backend);
		server.handshake(sourceDirectory, buildDirectory, generator);
		server.setGlobalSetting("debugOutput", true);
		assertTrue(server.getGlobalSettings().debugOutput);
	}

	@Test(expected = CMakeServerException.class)
	public void testSendAfterClose() throws Exception {
		server.startServer(backend);
		server.close();
		server.handshake(sourceDirectory, buildDirectory, generator);
	}

	@Test
	public void testConfigureWithArgs() throws Exception {
		server.startServer(backend);
		server.handshake(sourceDirectory, buildDirectory, generator);
		Map<String, String> args = new HashMap<>();
		args.put("FOO", "BAR");
		server.configure(args);

		assertThat(server.getCMakeCache().cache.stream().filter(entry -> entry.key.trim().equals("FOO"))
				.map(entry -> entry.value).findFirst().get(), equalTo("BAR"));

	}

	@Test
	public void testGetWatchers() throws CMakeServerException, InterruptedException, TimeoutException {
		List<String> signals = new ArrayList<>();
		List<String> changedFiles = new ArrayList<>();

		server.addListener(new ICMakeServerListener() {

			@Override
			public void onSignal(String name) {
				signals.add(name);
			}

			@Override
			public void onProgress(CMakeProgress progress) {
			}

			@Override
			public void onMessage(String title, String message) {
			}

			@Override
			public void onFileChange(String path, List<String> properties) {
				if (properties.contains("change")) {
					changedFiles.add(path);
				}
			}
		});

		server.startServer(backend);
		server.handshake(sourceDirectory, buildDirectory, generator);
		server.configure();
		CMakeFileSystemWatchers watchers = server.getFileSystemWatchers();

		assertThat(watchers.watchedDirectories, hasSize(greaterThan(0)));
		assertThat(watchers.watchedFiles, hasSize(greaterThan(0)));

		// CMakeLists.txt should be watched

		File cmakelists = watchers.watchedFiles.stream().map(s -> new File(s))
				.filter(f -> f.getName().equals("CMakeLists.txt")).findFirst().get();

		assertThat(cmakelists, equalTo(new File(sourceDirectory, "CMakeLists.txt")));

		// Modify CMakeLists.txt
		cmakelists.setLastModified(System.currentTimeMillis());

		// Ensure that we get a "dirty" signal
		waitUntil(signals, hasItem(equalTo("dirty")));

		// Ensure that we get a "fileChange" signal containing the name of the
		// file.
		waitUntil(changedFiles, hasItem(equalTo(cmakelists.toString())));
	}

}
