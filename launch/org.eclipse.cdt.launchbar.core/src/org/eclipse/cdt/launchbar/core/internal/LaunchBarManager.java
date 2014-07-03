/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationsProvider;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class LaunchBarManager extends PlatformObject implements ILaunchBarManager, ILaunchConfigurationListener {

	private List<Listener> listeners = new LinkedList<>();
	private List<ProviderExtensionDescriptor> providers = new ArrayList<>();
	private Map<String, ILaunchConfigurationDescriptor> configDescs = new HashMap<>();
	private ILaunchConfigurationDescriptor lastConfigDesc;
	
	private ILaunchConfigurationDescriptor activeConfigDesc;
	private ILaunchMode activeLaunchMode;
	private ILaunchTarget activeLaunchTarget;

	private static final LocalTarget localLaunchTarget = new LocalTarget();

	private static final String PREF_ACTIVE_CONFIG_DESC = "activeConfigDesc";
	private static final String PREF_ACTIVE_LAUNCH_MODE = "activeLaunchMode";
	private static final String PREF_ACTIVE_LAUNCH_TARGET = "activeLaunchTarget";

	private class ProviderExtensionDescriptor {
		private ILaunchConfigurationsProvider provider;
		private int priority;

		public ProviderExtensionDescriptor(ILaunchConfigurationsProvider provider, int priority) {
			super();
			this.provider = provider;
			this.priority = priority;
		}

		public ILaunchConfigurationsProvider getProvider() {
			return provider;
		}

		public int getPriority() {
			return priority;
		}

	}

	public LaunchBarManager() throws CoreException {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, "launchConfigProvider");
		IExtension[] extensions = point.getExtensions();
		for (IExtension extension : extensions) {
			// provider is manditory
			IConfigurationElement element = extension.getConfigurationElements()[0];
			ILaunchConfigurationsProvider provider = (ILaunchConfigurationsProvider) element.createExecutableExtension("class");
			String priorityString = element.getAttribute("priority");
			int priority = 100;
			if (priorityString != null) {
				try {
					priority = Integer.parseInt(priorityString);
				} catch (NumberFormatException e) {
					// Log it but keep going with the default
					Activator.log(e);
				}
			}
			providers.add(new ProviderExtensionDescriptor(provider, priority));
		}

		Collections.sort(providers, new Comparator<ProviderExtensionDescriptor>() {
			@Override
			public int compare(ProviderExtensionDescriptor o1, ProviderExtensionDescriptor o2) {
				int p1 = o1.getPriority();
				int p2 = o2.getPriority();
				if (p1 > p2)
					return 1;
				else if (p1 < p2)
					return -1;
				else
					return 0;
			}
		});

		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations()) {
			ILaunchConfigurationDescriptor configDesc = new DefaultLaunchConfigurationDescriptor(this, configuration);
			
			
			for (ProviderExtensionDescriptor provider : providers) {
				configDesc = provider.getProvider().filterDescriptor(this, configDesc);
			}
			configDescs.put(configDesc.getName(), configDesc);
		}

		for (ProviderExtensionDescriptor providerDesc : providers) {
			providerDesc.getProvider().init(this);
		}

		launchManager.addLaunchConfigurationListener(this);

		// Load up the active from the preferences or pick reasonable defaults
		IEclipsePreferences store = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String activeConfigDescName = store.get(PREF_ACTIVE_CONFIG_DESC, null);
		if (activeConfigDescName == null && !configDescs.isEmpty()) {
			activeConfigDescName = configDescs.values().iterator().next().getName();
		}

		if (activeConfigDescName != null) {
			ILaunchConfigurationDescriptor configDesc = configDescs.get(activeConfigDescName);
			if (configDesc != null) {
				setActiveLaunchConfigurationDescriptor(configDesc);
			}
		}
	}

	@Override
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		ILaunchConfigurationDescriptor configDesc = new DefaultLaunchConfigurationDescriptor(this, configuration);
		for (ProviderExtensionDescriptor provider : providers) {
			configDesc = provider.getProvider().filterDescriptor(this, configDesc);
		}
		try {
			addLaunchConfigurationDescriptor(configDesc);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub
	}

	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		try {
			ILaunchConfigurationDescriptor configDesc = getLaunchConfigurationDescriptor(configuration);
			if (configDesc != null)
				removeLaunchConfigurationDescriptor(configDesc);
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
	}

	@Override
	public ILaunchConfigurationDescriptor[] getLaunchConfigurationDescriptors() {
		ILaunchConfigurationDescriptor[] descs = configDescs.values().toArray(new ILaunchConfigurationDescriptor[configDescs.size()]);
		Arrays.sort(descs, new Comparator<ILaunchConfigurationDescriptor>() {
			@Override
			public int compare(ILaunchConfigurationDescriptor o1, ILaunchConfigurationDescriptor o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return descs;
	}

	@Override
	public ILaunchConfigurationDescriptor getActiveLaunchConfigurationDescriptor() {
		return activeConfigDesc;
	}

	@Override
	public ILaunchConfigurationDescriptor getLaunchConfigurationDescriptor(ILaunchConfiguration configuration) throws CoreException {
		// Check by name
		ILaunchConfigurationDescriptor configDesc = configDescs.get(configuration.getName());
		if (configDesc.matches(configuration))
			return configDesc;

		// Nope, try all descs
		for (ILaunchConfigurationDescriptor cd : configDescs.values()) {
			if (cd.matches(configuration))
				return cd;
		}

		// nothing, weird
		return null;
	}

	@Override
	public void setActiveLaunchConfigurationDescriptor(ILaunchConfigurationDescriptor configDesc) throws CoreException {
		if (activeConfigDesc == configDesc)
			return;
		lastConfigDesc = activeConfigDesc;
		activeConfigDesc = configDesc;

		IEclipsePreferences store = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		if (activeConfigDesc != null) {
			store.put(PREF_ACTIVE_CONFIG_DESC, activeConfigDesc.getName());
		} else {
			store.remove(PREF_ACTIVE_CONFIG_DESC);
		}
		try {
			store.flush();
		} catch (BackingStoreException e) {
			// TODO log
			e.printStackTrace();
		}

		if (activeConfigDesc == null) {
			setActiveLaunchMode(null);
			setActiveLaunchTarget(null);
			return;
		}

		// Send notifications
		for (Listener listener : listeners) {
			listener.activeConfigurationDescriptorChanged();
		}

		// Set active mode
		String activeModeName = store.node(activeConfigDesc.getName()).get(PREF_ACTIVE_LAUNCH_MODE, null);
		ILaunchMode[] launchModes = activeConfigDesc.getLaunchModes();
		boolean foundMode = false;
		if (activeModeName != null) {
			for (ILaunchMode mode : launchModes) {
				if (activeModeName.equals(mode.getIdentifier())) {
					setActiveLaunchMode(mode);
					foundMode = true;
					break;
				}
			}
		}
		if (!foundMode) {
			if (launchModes.length > 0) {
				ILaunchMode mode = activeConfigDesc.getLaunchMode("debug");
				if (mode == null) {
					mode = activeConfigDesc.getLaunchMode("run");
				}
				if (mode == null) {
					mode = launchModes[0];
				}
				setActiveLaunchMode(mode);
			} else {
				setActiveLaunchMode(null);
			}
		}

		// Set active target
		String activeTargetId = store.node(activeConfigDesc.getName()).get(PREF_ACTIVE_LAUNCH_TARGET, null);
		ILaunchTarget target = null;
		if (activeTargetId != null) {
			target = activeConfigDesc.getLaunchTarget(activeTargetId);
		}
		if (target == null) {
			ILaunchTarget[] targets = activeConfigDesc.getLaunchTargets();
			if (targets.length > 0) {
				target = targets[0];
			}
		}
		setActiveLaunchTarget(target);
	}

	@Override
	public void addLaunchConfigurationDescriptor(ILaunchConfigurationDescriptor configDesc) throws CoreException {
		configDescs.put(configDesc.getName(), configDesc);
		setActiveLaunchConfigurationDescriptor(configDesc);
	}

	@Override
	public void removeLaunchConfigurationDescriptor(ILaunchConfigurationDescriptor configDesc) {
		configDescs.remove(configDesc.getName());

		// Fix up the active config if this one was it
		if (activeConfigDesc.equals(configDesc)) {
			try {
				if (configDescs.isEmpty()) {
					setActiveLaunchConfigurationDescriptor(null);
				} else if (lastConfigDesc != null) {
					setActiveLaunchConfigurationDescriptor(lastConfigDesc);
					// Clear out the last desc since we just used it
					lastConfigDesc = null;
				} else {
					setActiveLaunchConfigurationDescriptor(configDescs.values().iterator().next());
				}
			} catch (CoreException e) {
				// TODO log
				e.printStackTrace();
			}
		}
	}

	@Override
	public ILaunchMode getActiveLaunchMode() {
		return activeLaunchMode;
	}

	@Override
	public void setActiveLaunchMode(ILaunchMode mode) {
		if (activeLaunchMode == mode)
			return;
		activeLaunchMode = mode;

		if (activeConfigDesc == null)
			return;
		
		Preferences store = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(activeConfigDesc.getName());
		if (mode != null) {
			store.put(PREF_ACTIVE_LAUNCH_MODE, mode.getIdentifier());
		} else {
			store.remove(PREF_ACTIVE_LAUNCH_MODE);
		}
		try {
			store.flush();
		} catch (BackingStoreException e) {
			// TODO log
			e.printStackTrace();
		}

		activeConfigDesc.setActiveLaunchMode(mode);
		for (Listener listener : listeners)
			listener.activeLaunchModeChanged();
	}

	@Override
	public ILaunchTarget getActiveLaunchTarget() {
		return activeLaunchTarget;
	}

	@Override
	public void setActiveLaunchTarget(ILaunchTarget target) {
		if (activeLaunchTarget == target) return;
		
		activeLaunchTarget = target;
		
		if (activeConfigDesc == null)
			return;

		Preferences store = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(activeConfigDesc.getName());
		if (target != null) {
			store.put(PREF_ACTIVE_LAUNCH_TARGET, target.getId());
		} else {
			store.remove(PREF_ACTIVE_LAUNCH_TARGET);
		}
		try {
			store.flush();
		} catch (BackingStoreException e) {
			// TODO log
			e.printStackTrace();
		}

		activeConfigDesc.setActiveLaunchTarget(target);
		for (Listener listener : listeners)
			listener.activeLaunchTargetChanged();
	}

	public LocalTarget getLocalLaunchTarget() {
		return localLaunchTarget;
	}
	
	@Override
	public void addLaunchTarget(ILaunchTarget target) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeLaunchTarget(ILaunchTarget target) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

}
