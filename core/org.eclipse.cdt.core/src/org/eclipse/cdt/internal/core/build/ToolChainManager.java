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
import org.eclipse.cdt.core.build.IToolChainType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

public class ToolChainManager implements IToolChainManager {

	private Map<String, IConfigurationElement> typeElements;
	private Map<String, IToolChainType> types;
	private Map<String, Map<String, IToolChain>> toolChains;

	private void init() {
		if (typeElements == null) {
			typeElements = new HashMap<>();
			types = new HashMap<>();

			// Load the types
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint typesPoint = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID + ".toolChainType"); //$NON-NLS-1$
			for (IConfigurationElement element : typesPoint.getConfigurationElements()) {
				String id = element.getAttribute("id"); //$NON-NLS-1$
				typeElements.put(id, element);
			}

			// Load the discovered toolchains
			toolChains = new HashMap<>();
			IExtensionPoint providersPoint = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID + ".toolChainProvider"); //$NON-NLS-1$
			for (IConfigurationElement element : providersPoint.getConfigurationElements()) {
				// TODO check for enablement
				try {
					IToolChainProvider provider = (IToolChainProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
					for (IToolChain toolChain : provider.getToolChains()) {
						String typeId = toolChain.getType().getId();
						Map<String, IToolChain> tcs = toolChains.get(typeId);
						if (tcs == null) {
							tcs = new HashMap<>();
							toolChains.put(typeId, tcs);
						}
						tcs.put(toolChain.getName(), toolChain);
					}
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}
	}

	@Override
	public IToolChainType getToolChainType(String id) {
		init();
		IToolChainType type = types.get(id);
		if (type == null) {
			IConfigurationElement element = typeElements.get(id);
			if (element != null) {
				try {
					type = (IToolChainType) element.createExecutableExtension("class"); //$NON-NLS-1$
					types.put(id, type);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}
		return type;
	}

	@Override
	public IToolChain getToolChain(String typeId, String name) {
		init();
		Map<String, IToolChain> tcs = toolChains.get(typeId);
		return tcs != null ? tcs.get(name) : null;
	}

	@Override
	public Collection<IToolChain> getToolChainsMatching(Map<String, String> properties) {
		init();
		List<IToolChain> tcs = new ArrayList<>();
		for (Map<String, IToolChain> types : toolChains.values()) {
			tcLoop:
			for (IToolChain toolChain : types.values()) {
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

}
