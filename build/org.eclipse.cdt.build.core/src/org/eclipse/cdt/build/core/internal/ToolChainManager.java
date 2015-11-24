package org.eclipse.cdt.build.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.cdt.build.core.IToolChainManager;
import org.eclipse.cdt.build.core.IToolChainProvider;
import org.eclipse.cdt.build.core.IToolChainType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.launchbar.core.target.ILaunchTarget;

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
			IExtensionPoint typesPoint = registry.getExtensionPoint(Activator.getId() + ".toolChainType"); //$NON-NLS-1$
			for (IConfigurationElement element : typesPoint.getConfigurationElements()) {
				String id = element.getAttribute("id"); //$NON-NLS-1$
				typeElements.put(id, element);
			}

			// Load the discovered toolchains
			toolChains = new HashMap<>();
			IExtensionPoint providersPoint = registry.getExtensionPoint(Activator.getId() + ".toolChainProvider"); //$NON-NLS-1$
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
					Activator.log(e);
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
					Activator.log(e);
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
	public Collection<IToolChain> getToolChainsSupporting(ILaunchTarget target) {
		init();

		List<IToolChain> supportingtcs = new ArrayList<>();
		for (Map<String, IToolChain> tcs : toolChains.values()) {
			for (IToolChain tc : tcs.values()) {
				if (tc.supports(target)) {
					supportingtcs.add(tc);
				}
			}
		}
		return supportingtcs;
	}

}
