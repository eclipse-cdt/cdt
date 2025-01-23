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

import org.eclipse.cdt.cmake.core.CMakeBuildConfiguration;
import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.CoreException;

/**
 * An example CMake build configuration that demonstrates how an ISV can provide their own customisations.
 */
public class ExtendedCMakeBuildConfiguration extends CMakeBuildConfiguration {

	public ExtendedCMakeBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
	}

	public ExtendedCMakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			ICMakeToolChainFile toolChainFile, String launchMode) {
		super(config, name, toolChain, toolChainFile, launchMode);
	}

	public ExtendedCMakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		super(config, name, toolChain);
	}

	// TODO: Here goes the example extending that is being developed in #1046 https://github.com/eclipse-cdt/cdt/pull/1046
}