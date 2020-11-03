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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import org.eclipse.cdt.cmake.core.internal.properties.CMakePropertiesBean;
import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.ICMakeProperties;
import org.eclipse.cdt.cmake.core.properties.ICMakePropertiesController;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

/**
 * A {@code ICMakePropertiesController} that monitors modifications to the project properties that force
 * us to delete file CMakeCache.txt to avoid complaints by cmake.
 * @author Martin Weber
 */
class CMakePropertiesController implements ICMakePropertiesController {

	private final Path storageFile;
	private final Runnable cmakeCacheDirtyMarker;

	private String cacheFile;
	private List<String> extraArguments;
	private CMakeGenerator generatorLinux;
	private List<String> extraArgumentsLinux;
	private CMakeGenerator generatorWindows;
	private List<String> extraArgumentsWindows;
	private String buildType;

	/** Creates a new CMakePropertiesController object.
	 *
	 * @param storageFile
	 * 		the file where to persist the properties. The file may not exist, but its parent directory is supposed to exist.
	 * @param cmakeCacheDirtyMarker
	 * 		the object to notify when modifications to the project properties force
	 * us to delete file CMakeCache.txt to avoid complaints by cmake
	 */
	CMakePropertiesController(Path storageFile, Runnable cmakeCacheDirtyMarker) {
		this.storageFile = Objects.requireNonNull(storageFile);
		this.cmakeCacheDirtyMarker = Objects.requireNonNull(cmakeCacheDirtyMarker);
	}

	@Override
	public ICMakeProperties load() throws IOException {
		CMakePropertiesBean props = null;
		if (Files.exists(storageFile)) {
			try (InputStream is = Files.newInputStream(storageFile)) {
				props = new Yaml(new CustomClassLoaderConstructor(this.getClass().getClassLoader())).loadAs(is,
						CMakePropertiesBean.class);
				// props is null here if if no document was available in the file
			}
		}
		if (props == null) {
			// nothing was persisted, return default properties
			props = new CMakePropertiesBean();
		}

		setupModifyDetection(props);
		return props;
	}

	@Override
	public void save(ICMakeProperties properties) throws IOException {
		// detect whether changes force us to delete file CMakeCache.txt to avoid complaints by cmake
		if (!Objects.equals(buildType, properties.getBuildType())
				|| !Objects.equals(cacheFile, properties.getCacheFile())
				|| !Objects.equals(generatorLinux, properties.getLinuxOverrides().getGenerator())
				|| !Objects.equals(generatorWindows, properties.getWindowsOverrides().getGenerator())) {
			cmakeCacheDirtyMarker.run(); // must remove cmake cachefile
		} else if (hasExtraArgumentChanged(extraArguments, properties.getExtraArguments())
				|| hasExtraArgumentChanged(extraArgumentsLinux, properties.getLinuxOverrides().getExtraArguments())
				|| hasExtraArgumentChanged(extraArgumentsWindows,
						properties.getWindowsOverrides().getExtraArguments())) {
			cmakeCacheDirtyMarker.run(); // must remove cmake cachefile
		}
		if (!Files.exists(storageFile)) {
			try {
				Files.createFile(storageFile);
			} catch (FileAlreadyExistsException ignore) {
			}
		}
		try (Writer wr = new OutputStreamWriter(Files.newOutputStream(storageFile))) {
			new Yaml().dump(properties, wr);
		}

		setupModifyDetection(properties);
	}

	/** Sets up detection of modifications that force us to delete file CMakeCache.txt to avoid complaints by cmake
	 */
	private void setupModifyDetection(ICMakeProperties properties) {
		buildType = properties.getBuildType();
		cacheFile = properties.getCacheFile();
		extraArguments = properties.getExtraArguments();
		generatorLinux = properties.getLinuxOverrides().getGenerator();
		extraArgumentsLinux = properties.getLinuxOverrides().getExtraArguments();
		generatorWindows = properties.getWindowsOverrides().getGenerator();
		extraArgumentsWindows = properties.getWindowsOverrides().getExtraArguments();
	}

	private boolean hasExtraArgumentChanged(List<String> expected, List<String> actual) {
		String wanted = "CMAKE_TOOLCHAIN_FILE"; //$NON-NLS-1$
		// extract the last arguments that contain String wanted..
		Predicate<? super String> predContains = a -> a.contains(wanted);
		BinaryOperator<String> keepLast = (first, second) -> second;
		String a1 = expected.stream().filter(predContains).reduce(keepLast).orElse(null);
		String a2 = actual.stream().filter(predContains).reduce(keepLast).orElse(null);
		if (!Objects.equals(a1, a2)) {
			return true;
		}
		return false;
	}
}