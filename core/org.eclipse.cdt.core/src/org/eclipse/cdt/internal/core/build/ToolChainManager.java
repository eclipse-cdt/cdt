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
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;

public class ToolChainManager implements IToolChainManager {

	private Map<String, IConfigurationElement> providerElements;
	private Map<String, IToolChainProvider> providers;
	private Map<List<String>, IToolChain> toolChains;
	private List<IToolChain> orderedToolChains;
	private List<ISafeRunnable> listeners = new ArrayList<>();

	private synchronized void init() {
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
			orderedToolChains = new ArrayList<>();
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
		return id;
	}

	@Override
	public void addToolChain(IToolChain toolChain) {
		orderedToolChains.add(toolChain);
		toolChains.put(getId(toolChain), toolChain);
		fireChange();
	}

	@Override
	public void removeToolChain(IToolChain toolChain) {
		orderedToolChains.remove(toolChain);
		toolChains.remove(getId(toolChain));
		fireChange();
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
	public IToolChain getToolChain(String providerId, String id) throws CoreException {
		init();
		List<String> tid = new ArrayList<>(3);
		tid.add(providerId);
		tid.add(id);

		IToolChain toolChain = toolChains.get(tid);
		if (toolChain != null) {
			return toolChain;
		}

		// Try the provider
		IToolChainProvider realProvider = providers.get(providerId);
		if (realProvider != null) {
			toolChain = realProvider.getToolChain(id);
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
				String tcProperty = toolChain.getProperty(property.getKey());
				if (tcProperty != null) {
					if (!property.getValue().equals(tcProperty)) {
						matches = false;
						break;
					}
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

	@Override
	public Collection<IToolChain> getAllToolChains() throws CoreException {
		init();
		return orderedToolChains;
	}

	@Override
	public void setToolChainOrder(List<IToolChain> orderedToolchains) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addToolChainListener(ISafeRunnable listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeToolChainListener(ISafeRunnable listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private void fireChange() {
		List<ISafeRunnable> runners;
		synchronized (listeners) {
			runners = new ArrayList<>(listeners);
		}
		for (ISafeRunnable runner : runners) {
			SafeRunner.run(runner);
		}
	}
}
