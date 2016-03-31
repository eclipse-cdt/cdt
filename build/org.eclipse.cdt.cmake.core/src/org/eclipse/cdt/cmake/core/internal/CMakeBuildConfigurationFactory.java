/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;

public class CMakeBuildConfigurationFactory implements IAdapterFactory {

	private static IToolChainManager toolChainManager = Activator.getService(IToolChainManager.class);
	private static Map<IBuildConfiguration, CMakeBuildConfiguration> cache = new HashMap<>();

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { CMakeBuildConfiguration.class };
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType.equals(CMakeBuildConfiguration.class) && adaptableObject instanceof IBuildConfiguration) {
			IBuildConfiguration config = (IBuildConfiguration) adaptableObject;
			synchronized (cache) {
				CMakeBuildConfiguration cmakeConfig = cache.get(config);
				if (cmakeConfig == null) {
					if (!config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
						cmakeConfig = new CMakeBuildConfiguration(config);
						cache.put(config, cmakeConfig);
						return (T) cmakeConfig;
					} else {
						// Default to local toolchain
						Map<String, String> properties = new HashMap<>();
						properties.put(IToolChain.ATTR_OS, Platform.getOS());
						properties.put(IToolChain.ATTR_ARCH, Platform.getOSArch());
						Collection<IToolChain> toolChains = toolChainManager.getToolChainsMatching(properties);
						if (!toolChains.isEmpty()) {
							// TODO propery handle when we have more than one
							cmakeConfig = new CMakeBuildConfiguration(config, toolChains.iterator().next());
							cache.put(config, cmakeConfig);
							return (T) cmakeConfig;
						}

						// Use the first toolchain we can find
						toolChains = toolChainManager.getToolChainsMatching(new HashMap<>());
						if (!toolChains.isEmpty()) {
							// TODO propery handle when we have more
							// than one
							cmakeConfig = new CMakeBuildConfiguration(config, toolChains.iterator().next());
							cache.put(config, cmakeConfig);
							return (T) cmakeConfig;
						}
					}
				} else {
					return (T) cmakeConfig;
				}
			}
		}
		return null;
	}

}
