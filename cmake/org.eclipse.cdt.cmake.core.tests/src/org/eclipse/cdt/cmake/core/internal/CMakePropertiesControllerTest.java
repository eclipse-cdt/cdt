/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.ICMakeProperties;
import org.eclipse.cdt.cmake.core.properties.IOsOverrides;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class CMakePropertiesControllerTest {

	/**
	 * Test method for {@link org.eclipse.cdt.cmake.core.internal.CMakePropertiesController#load()}.
	 * @throws IOException
	 */
	@Test
	public void testLoad() throws IOException {
		CMakePropertiesController testee;
		// test with non-existing file
		Path file = Path.of(new File("does-not-exist" + UUID.randomUUID().toString()).toURI());
		testee = new CMakePropertiesController(file, () -> {
		});
		assertNotNull(testee.load());

		// test with empty file
		File f = File.createTempFile("CMakePropertiesControllerTest", null);
		f.deleteOnExit();
		file = Path.of(f.toURI());
		testee = new CMakePropertiesController(file, () -> {
		});
		assertNotNull(testee.load());
	}

	/**
	 * Test method for {@link org.eclipse.cdt.cmake.core.internal.CMakePropertiesController#save(org.eclipse.cdt.cmake.core.properties.ICMakeProperties)}.
	 * @throws IOException
	 */
	@Test
	public void testSaveLoad() throws IOException {
		Path file = Path.of(File.createTempFile("CMakePropertiesControllerTest", null).toURI());
		CMakePropertiesController testee = new CMakePropertiesController(file, () -> {
		});
		ICMakeProperties props = testee.load();
		assertNotNull(props);

		props.setCacheFile("cacheFile");
		props.setClearCache(true);
		props.setDebugOutput(true);
		props.setDebugTryCompile(true);
		props.setTrace(true);
		props.setWarnNoDev(true);
		props.setWarnUnitialized(true);
		props.setWarnUnused(true);
		{
			IOsOverrides overrides = props.getLinuxOverrides();
			overrides.setGenerator(CMakeGenerator.Ninja);
			List<String> extraArgs = new ArrayList<>();
			extraArgs.add("arg1l=1");
			extraArgs.add("arg2l=2");
			overrides.setExtraArguments(extraArgs);
		}
		{
			IOsOverrides overrides = props.getWindowsOverrides();
			overrides.setGenerator(CMakeGenerator.BorlandMakefiles);
			List<String> extraArgs = new ArrayList<>();
			extraArgs.add("arg1w=1");
			extraArgs.add("arg2w=2");
			overrides.setExtraArguments(extraArgs);
		}

		List<String> extraArgs = new ArrayList<>();
		extraArgs.add("arg1");
		extraArgs.add("arg2");
		props.setExtraArguments(extraArgs);

		testee.save(props);

		ICMakeProperties in = testee.load();
		assertThat(in).usingRecursiveComparison().isEqualTo(props);
	}
}
