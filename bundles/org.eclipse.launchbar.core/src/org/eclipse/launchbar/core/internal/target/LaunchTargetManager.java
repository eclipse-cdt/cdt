/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.core.internal.target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetListener;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.TargetStatus;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class LaunchTargetManager implements ILaunchTargetManager {

	private Map<String, Map<String, ILaunchTarget>> targets;
	private Map<String, IConfigurationElement> typeElements;
	private Map<String, ILaunchTargetProvider> typeProviders = new HashMap<>();
	private List<ILaunchTargetListener> listeners = new LinkedList<>();

	private static final String DELIMETER1 = ","; //$NON-NLS-1$
	private static final String DELIMETER2 = "!"; //$NON-NLS-1$
	private static final String DELIMETER3 = ":"; //$NON-NLS-1$

	private Preferences getTargetsPref() {
		return InstanceScope.INSTANCE.getNode(Activator.getDefault().getBundle().getSymbolicName())
				.node(getClass().getSimpleName());
	}

	private synchronized void initTargets() {
		if (targets == null) {
			// load target type elements from registry
			typeElements = new HashMap<>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint point = registry
					.getExtensionPoint(Activator.getDefault().getBundle().getSymbolicName() + ".launchTargetTypes"); //$NON-NLS-1$
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (id != null) {
						typeElements.put(id, element);
					}
				}
			}

			// load targets from preference store
			targets = new HashMap<>();
			Preferences prefs = getTargetsPref();
			try {
				for (String typeId : prefs.keys()) {
					Map<String, ILaunchTarget> type = targets.get(typeId);
					if (type == null) {
						type = new HashMap<>();
						targets.put(typeId, type);
					}

					for (String name : prefs.get(typeId, "").split(DELIMETER1)) { //$NON-NLS-1$
						if (name.contains(DELIMETER2)) {
							String[] list = name.split(DELIMETER2);
							type.put(list[0], new LaunchTarget(typeId, list[0], list[1]));
						} else {
							type.put(name, new LaunchTarget(typeId, name, name));
						}
					}
				}
			} catch (BackingStoreException e) {
				Activator.log(e);
			}

			// Call the provider's init
			// TODO check enablement so we don't call them if we don't need to
			for (String typeId : typeElements.keySet()) {
				ILaunchTargetProvider provider = getProvider(typeId);
				if (provider != null) {
					provider.init(this);
				}
			}
		}
	}

	private ILaunchTargetProvider getProvider(String typeId) {
		ILaunchTargetProvider provider = typeProviders.get(typeId);
		if (provider == null) {
			IConfigurationElement element = typeElements.get(typeId);
			if (element != null) {
				try {
					provider = (ILaunchTargetProvider) element.createExecutableExtension("provider"); //$NON-NLS-1$
				} catch (CoreException e) {
					Activator.log(e);
				}
			}

			if (provider == null) {
				provider = new ILaunchTargetProvider() {
					@Override
					public void init(ILaunchTargetManager targetManager) {
					}

					@Override
					public TargetStatus getStatus(ILaunchTarget target) {
						return TargetStatus.OK_STATUS;
					}
				};
			}
		}
		return provider;
	}

	@Override
	public ILaunchTarget[] getLaunchTargets() {
		initTargets();
		List<ILaunchTarget> targetList = new ArrayList<>();
		for (Map<String, ILaunchTarget> type : targets.values()) {
			targetList.addAll(type.values());
		}
		return targetList.toArray(new ILaunchTarget[targetList.size()]);
	}

	@Override
	public ILaunchTarget[] getLaunchTargetsOfType(String typeId) {
		initTargets();
		Map<String, ILaunchTarget> type = targets.get(typeId);
		if (type != null) {
			return type.values().toArray(new ILaunchTarget[type.size()]);
		}
		return new ILaunchTarget[0];
	}

	@Override
	public ILaunchTarget getLaunchTarget(String typeId, String id) {
		initTargets();
		Map<String, ILaunchTarget> type = targets.get(typeId);
		if (type != null) {
			return type.get(id);
		}
		return null;
	}

	@Override
	public TargetStatus getStatus(ILaunchTarget target) {
		return getProvider(target.getTypeId()).getStatus(target);
	}

	@Override
	public ILaunchTarget addLaunchTarget(String typeId, String id, String name) {
		initTargets();
		Map<String, ILaunchTarget> type = targets.get(typeId);
		if (type == null) {
			type = new HashMap<>();
			targets.put(typeId, type);
		}

		ILaunchTarget target = new LaunchTarget(typeId, id, name);
		type.put(id, target);

		getTargetsPref().put(typeId, type.values().stream().map(t -> t.getId() + DELIMETER2 + t.getName())
				.collect(Collectors.joining(DELIMETER1)));

		for (ILaunchTargetListener listener : listeners) {
			listener.launchTargetAdded(target);
		}

		return target;
	}

	@Override
	public void removeLaunchTarget(ILaunchTarget target) {
		initTargets();
		Map<String, ILaunchTarget> type = targets.get(target.getTypeId());
		if (type != null) {
			type.remove(target.getName());
			if (type.isEmpty()) {
				targets.remove(target.getTypeId());
				getTargetsPref().remove(target.getTypeId());
			} else {
				getTargetsPref().put(target.getTypeId(), type.values().stream()
						.map(t -> t.getId() + DELIMETER2 + t.getName()).collect(Collectors.joining(DELIMETER1)));
			}

			for (ILaunchTargetListener listener : listeners) {
				listener.launchTargetRemoved(target);
			}
		}
	}

	@Override
	public void targetStatusChanged(ILaunchTarget target) {
		for (ILaunchTargetListener listener : listeners) {
			listener.launchTargetStatusChanged(target);
		}
	}

	@Override
	public ILaunchTarget getDefaultLaunchTarget(ILaunchConfiguration configuration) {
		Preferences prefs = getTargetsPref().node("configs"); //$NON-NLS-1$
		String targetId = prefs.get(configuration.getName(), null);
		if (targetId != null) {
			String[] parts = targetId.split(DELIMETER3);
			return getLaunchTarget(parts[0], parts[1]);
		}
		return null;
	}

	@Override
	public void setDefaultLaunchTarget(ILaunchConfiguration configuration, ILaunchTarget target) {
		Preferences prefs = getTargetsPref().node("configs"); //$NON-NLS-1$
		String targetId = String.join(DELIMETER3, target.getTypeId(), target.getId());
		prefs.put(configuration.getName(), targetId);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	@Override
	public void addListener(ILaunchTargetListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(ILaunchTargetListener listener) {
		listeners.remove(listener);
	}

}
