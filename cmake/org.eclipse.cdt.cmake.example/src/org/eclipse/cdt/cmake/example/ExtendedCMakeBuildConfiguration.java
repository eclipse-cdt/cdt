/*******************************************************************************
 * Copyright (c) 2015, 2025 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.example;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cmake.core.CMakeBuildConfiguration;
import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * An example CMake build configuration that demonstrates how an ISV can provide their own customisations.
 */
public class ExtendedCMakeBuildConfiguration extends CMakeBuildConfiguration {

	public ExtendedCMakeBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
	}

	public ExtendedCMakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			ICMakeToolChainFile toolChainFile, String launchMode, ILaunchTarget launchTarget) {
		super(config, name, toolChain, toolChainFile, launchMode, launchTarget);
	}

	@Override
	public Map<String, String> getDefaultProperties() {
		/*
		 * Here we demonstrate how an ISV can provide a different default generator.
		 * More examples can be found in CMakeBuildConfigurationTests
		 */
		var defs = new HashMap<>(super.getDefaultProperties());
		defs.put(CMAKE_GENERATOR, CMakeGenerator.UnixMakefiles.getCMakeName());
		return defs;
	}
}
