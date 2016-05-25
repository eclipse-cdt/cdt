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
	private Map<List<String>, IToolChain> toolChains;

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

	private List<String> getId(IToolChain toolChain) {
		List<String> id = new ArrayList<>(3);
		id.add(toolChain.getProvider().getId());
		id.add(toolChain.getId());
		id.add(toolChain.getVersion());
		return id;
	}

	@Override
	public void addToolChain(IToolChain toolChain) {
		toolChains.put(getId(toolChain), toolChain);
	}

	@Override
	public void removeToolChain(IToolChain toolChain) {
		toolChains.remove(getId(toolChain));
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
	public IToolChain getToolChain(String providerId, String id, String version) throws CoreException {
		init();
		List<String> tid = new ArrayList<>(3);
		tid.add(providerId);
		tid.add(id);
		tid.add(version);

		IToolChain toolChain = toolChains.get(tid);
		if (toolChain != null) {
			return toolChain;
		}

		// Try the provider
		IToolChainProvider realProvider = providers.get(providerId);
		if (realProvider != null) {
			toolChain = realProvider.getToolChain(id, version);
			if (toolChain != null) {
				toolChains.put(getId(toolChain), toolChain);
				return toolChain;
			}
		}

		return null;
	}

	@Override
	public Collection<IToolChain> getToolChainsMatching(Map<String, String> properties) {
		init();
		List<IToolChain> tcs = new ArrayList<>();
		for (IToolChain toolChain : toolChains.values()) {
			boolean matches = true;
			for (Map.Entry<String, String> property : properties.entrySet()) {
				if (!property.getValue().equals(toolChain.getProperty(property.getKey()))) {
					matches = false;
					break;
				}
			}
			if (matches) {
				tcs.add(toolChain);
			}
		}
		
		// Allow 32-bit compilers on 64-bit machines
		// TODO is there a cleaner way to do this?
		if ("x86_64".equals(properties.get(IToolChain.ATTR_ARCH))) { //$NON-NLS-1$
			Map<String, String> properties32 = new HashMap<>(properties);
			properties32.put(IToolChain.ATTR_ARCH, "x86"); //$NON-NLS-1$
			tcs.addAll(getToolChainsMatching(properties32));
		}

		return tcs;
	}

	@Override
	public Collection<IToolChain> getToolChains(String providerId) {
		init();
		List<IToolChain> tcs = new ArrayList<>();
		for (IToolChain toolChain : toolChains.values()) {
			if (toolChain.getProvider().getId().equals(providerId)) {
				tcs.add(toolChain);
			}
		}
		return tcs;
	}

	@Override
	public Collection<IToolChain> getToolChains(String providerId, String id) throws CoreException {
		init();
		List<IToolChain> tcs = new ArrayList<>();
		for (IToolChain toolChain : toolChains.values()) {
			if (toolChain.getProvider().getId().equals(providerId) && toolChain.getId().equals(id)) {
				tcs.add(toolChain);
			}
		}
		return tcs;
	}

}
