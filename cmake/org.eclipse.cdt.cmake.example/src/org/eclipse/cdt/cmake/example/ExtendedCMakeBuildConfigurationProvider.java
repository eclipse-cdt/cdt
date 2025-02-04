/*******************************************************************************
 * Copyright (c) 2016, 2025 QNX Software Systems and others.
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
import org.eclipse.cdt.cmake.core.CMakeBuildConfigurationProvider;
import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public class ExtendedCMakeBuildConfigurationProvider extends CMakeBuildConfigurationProvider {
	@SuppressWarnings("hiding")
	public static final String ID = "org.eclipse.cdt.cmake.example.provider.extended"; //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected CMakeBuildConfiguration createCMakeBuildConfiguration(IBuildConfiguration config, String name)
			throws CoreException {
		return new ExtendedCMakeBuildConfiguration(config, name);
	}

	@Override
	protected CMakeBuildConfiguration createCMakeBuildConfiguration(IBuildConfiguration config, String name,
			IToolChain toolChain, ICMakeToolChainFile toolChainFile, String launchMode, ILaunchTarget launchTarget) {
		return new ExtendedCMakeBuildConfiguration(config, name, toolChain, toolChainFile, launchMode, launchTarget);
	}
}
