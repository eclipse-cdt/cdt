/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.core.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

public class ToolChainManager implements IToolChainManager {

	private Map<String, IConfigurationElement> providerElements;
	private Map<String, IToolChainProvider> providers;
	private Map<String, Map<String, IToolChain>> toolChains;

	private void init() {
		if (providerElements == null) {
			providerElements = new HashMap<>();
			providers = new HashMap<>();

			// Load the types
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint typesPoint = registry
					.getExtensionPoint(CCorePlugin.PLUGIN_ID + ".toolChainProvider"); //$NON-NLS-1$
			for (IConfigurationElement element : typesPoint.getConfigurationElements()) {
				String id = element.getAttribute("id"); //$NON-NLS-1$
				providerElements.put(id, element);
			}

			// Load the discovered toolchains
			toolChains = new HashMap<>();
			for (IConfigurationElement element : providerElements.values()) {
				// TODO check for enablement
				try {
					IToolChainProvider provider = (IToolChainProvider) element
							.createExecutableExtension("class"); //$NON-NLS-1$
					providers.put(element.getAttribute("id"), provider); //$NON-NLS-1$
					provider.init(this);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}
	}

	@Override
	public void addToolChain(IToolChain toolChain) {
		String providerId = toolChain.getProvider().getId();
		Map<String, IToolChain> provider = toolChains.get(providerId);
		if (provider == null) {
			provider = new HashMap<>();
			toolChains.put(providerId, provider);
		}
		provider.put(toolChain.getName(), toolChain);
	}

	@Override
	public void removeToolChain(IToolChain toolChain) {
		String providerId = toolChain.getProvider().getId();
		Map<String, IToolChain> provider = toolChains.get(providerId);
		if (provider != null) {
			provider.remove(toolChain.getName());
		}
	}

	@Override
	public IToolChainProvider getProvider(String providerId) throws CoreException {
		init();
		IToolChainProvider provider = providers.get(providerId);
		if (provider == null) {
			IConfigurationElement element = providerElements.get(providerId);
			if (element != null) {
				provider = (IToolChainProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
				providers.put(providerId, provider);
			}
		}
		return provider;
	}

	@Override
	public IToolChain getToolChain(String providerId, String name) throws CoreException {
		init();
		Map<String, IToolChain> provider = toolChains.get(providerId);
		if (provider != null) {
			IToolChain toolChain = provider.get(name);
			if (toolChain != null) {
				return toolChain;
			}
		}

		// Try the provider
		IToolChainProvider realProvider = providers.get(providerId);
		if (realProvider != null) {
			IToolChain toolChain = realProvider.getToolChain(name);
			if (toolChain != null) {
				if (provider == null) {
					provider = new HashMap<>();
					toolChains.put(providerId, provider);
				}
				provider.put(name, toolChain);
				return toolChain;
			}
		}

		return null;
	}

	@Override
	public Collection<IToolChain> getToolChainsMatching(Map<String, String> properties) {
		init();
		List<IToolChain> tcs = new ArrayList<>();
		for (Map<String, IToolChain> types : toolChains.values()) {
			tcLoop: for (IToolChain toolChain : types.values()) {
				for (Map.Entry<String, String> property : properties.entrySet()) {
					if (!property.getValue().equals(toolChain.getProperty(property.getKey()))) {
						// doesn't match, move on to next toolchain
						continue tcLoop;
					}
				}
				tcs.add(toolChain);
			}
		}
		return tcs;
	}

	@Override
	public Collection<IToolChain> getToolChains(String providerId) {
		init();
		Map<String, IToolChain> provider = toolChains.get(providerId);
		if (provider != null) {
			return Collections.unmodifiableCollection(provider.values());
		} else {
			return Collections.emptyList();
		}
	}

}
