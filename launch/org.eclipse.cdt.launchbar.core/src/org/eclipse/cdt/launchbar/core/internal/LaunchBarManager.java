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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptorType;
import org.eclipse.cdt.launchbar.core.ILaunchObjectProvider;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
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
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class LaunchBarManager extends PlatformObject implements ILaunchBarManager, ILaunchConfigurationListener {

	private List<Listener> listeners = new LinkedList<>();
	private Map<String, ILaunchTargetType> targetTypes = new HashMap<>();
	private List<ILaunchDescriptorType> descriptorTypes = new ArrayList<>();
	private Map<String, ILaunchDescriptor> descriptors = new HashMap<>();
	private List<ILaunchObjectProvider> objectProviders = new ArrayList<>();
	// Map descriptor type to target type to provider
	private Map<String, Map<String, ILaunchConfigurationProvider>> configProviders = new HashMap<>();
	// Map descriptor type to target type
	private Map<String, String> defaultTargetTypes = new HashMap<>();
	private Map<Object, ILaunchDescriptor> objectDescriptorMap = new HashMap<>();

	private ILaunchDescriptor activeLaunchDesc;
	private ILaunchMode activeLaunchMode;
	private ILaunchTarget activeLaunchTarget;

	private ILaunchDescriptor lastLaunchDesc;

	private static final String PREF_ACTIVE_CONFIG_DESC = "activeConfigDesc";
	private static final String PREF_ACTIVE_LAUNCH_MODE = "activeLaunchMode";
	private static final String PREF_ACTIVE_LAUNCH_TARGET = "activeLaunchTarget";

	public LaunchBarManager() throws CoreException {
		final Map<ILaunchDescriptorType, Integer> typePriorities = new HashMap<>();

		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, "launchBarContributions");
		IExtension[] extensions = point.getExtensions();
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				String elementName = element.getName();
				if (elementName.equals("descriptorType")) {
					String id = element.getAttribute("id");
					String priorityStr = element.getAttribute("priority");
					ILaunchDescriptorType type = (ILaunchDescriptorType) element.createExecutableExtension("class");

					assert id.equals(type.getId());
					descriptorTypes.add(type);

					int priority = 1;
					if (priorityStr != null) {
						try {
							priority = Integer.parseInt(priorityStr);
						} catch (NumberFormatException e) {
							// Log it but keep going with the default
							Activator.log(e);
						}
					}
					typePriorities.put(type, priority);
				} else if (elementName.equals("targetType")) {
					String id = element.getAttribute("id");
					ILaunchTargetType targetType = (ILaunchTargetType) element.createExecutableExtension("class");

					assert id.equals(targetType.getId());
					targetTypes.put(id, targetType);
				} else if (elementName.equals("objectProvider")) {
					ILaunchObjectProvider objectProvider = (ILaunchObjectProvider) element.createExecutableExtension("class");
					objectProviders.add(objectProvider);
				} else if (elementName.equals("configProvider")) {
					String descriptorType = element.getAttribute("descriptorType");
					String targetType = element.getAttribute("targetType");
					// TODO don't instantiate this until we need it
					ILaunchConfigurationProvider configProvider = (ILaunchConfigurationProvider) element.createExecutableExtension("class");

					Map<String, ILaunchConfigurationProvider> targetTypes = configProviders.get(descriptorType);
					if (targetTypes == null) {
						targetTypes = new HashMap<>();
						configProviders.put(descriptorType, targetTypes);
					}
					targetTypes.put(targetType, configProvider);
					
					String isDefault = element.getAttribute("isDefault");
					if (isDefault != null && Boolean.valueOf(isDefault)) {
						defaultTargetTypes.put(descriptorType, targetType);
					}
				}
			}
		}

		Collections.sort(descriptorTypes, new Comparator<ILaunchDescriptorType>() {
			@Override
			public int compare(ILaunchDescriptorType o1, ILaunchDescriptorType o2) {
				int p1 = typePriorities.get(o1);
				int p2 = typePriorities.get(o2);
				// Reverse order, highest priority first
				if (p1 < p2)
					return 1;
				else if (p1 > p2)
					return -1;
				else
					return 0;
			}
		});

		// Load up the active from the preferences before loading the descriptors
		IEclipsePreferences store = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String activeConfigDescName = store.get(PREF_ACTIVE_CONFIG_DESC, null);

		for (ILaunchDescriptorType descriptorType : descriptorTypes) {
			descriptorType.init(this);
		}

		for (ILaunchTargetType targetType : targetTypes.values()) {
			targetType.init(this);
		}

		for (ILaunchObjectProvider objectProvider : objectProviders) {
			objectProvider.init(this);
		}

		for (Map<String, ILaunchConfigurationProvider> targetMap : configProviders.values()) {
			for (ILaunchConfigurationProvider configProvider : targetMap.values()) {
				configProvider.init(this);
			}
		}

		// Hook up the existing launch configurations and listen
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations()) {
			launchConfigurationAdded(configuration);
		}
		launchManager.addLaunchConfigurationListener(this);

		// Now that all the descriptors are loaded, set the one
		if (activeConfigDescName == null && !descriptors.isEmpty()) {
			activeConfigDescName = descriptors.values().iterator().next().getName();
		}

		if (activeConfigDescName != null) {
			ILaunchDescriptor configDesc = descriptors.get(activeConfigDescName);
			if (configDesc != null) {
				setActiveLaunchDescriptor(configDesc);
			}
		}
	}

	@Override
	public ILaunchDescriptor launchObjectAdded(Object element) {
		ILaunchDescriptor desc = objectDescriptorMap.get(element);
		if (desc != null)
			return desc;
		
		for (ILaunchDescriptorType descriptorType : descriptorTypes) {
			try {
				if (descriptorType.ownsLaunchObject(element)) {
					desc = descriptorType.getDescriptor(element);
					if (doAddDescriptor(element, desc))
						setActiveLaunchDescriptor(desc);
					return desc;
				}
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}
		}
		return null;
	}

	protected boolean doAddDescriptor(Object element, ILaunchDescriptor desc) {
		if (desc == null)
			return false;
		objectDescriptorMap.put(element, desc);
		String name = desc.getName();
		ILaunchDescriptor old = descriptors.get(name);
		if (old != null && !old.equals(desc)) {
			int indexConflict = descriptorTypes.indexOf(old.getType());
			int index = descriptorTypes.indexOf(desc.getType());
			if (indexConflict < index) {
				return false; // previous descriptor is higher priority, don't change name mapping
			}
		}
		descriptors.put(name, desc);
		return true;
	}

	protected boolean doRemoveDescriptor(Object element, ILaunchDescriptor desc) {
		if (desc == null)
			return false;
		String name = desc.getName();
		descriptors.remove(name);
		objectDescriptorMap.remove(element);
		// is there something else we can use which has same name?
		int index = -1;
		for (Iterator<ILaunchDescriptor> iterator = objectDescriptorMap.values().iterator(); iterator.hasNext();) {
			ILaunchDescriptor idesc = iterator.next();
			if (idesc.getName().equals(name)) {
				ILaunchDescriptor old = descriptors.get(name);
				int indexConflict = descriptorTypes.indexOf(old.getType());
				if (indexConflict < index) {
					continue; // previous descriptor is higher priority, don't change name mapping
				}
				descriptors.put(name, idesc);
				index = descriptorTypes.indexOf(idesc.getType());
			}
		}
		if (index >= 0)
			return false;
		return true;
	}

	@Override
	public void launchObjectRemoved(Object element) throws CoreException {
		ILaunchDescriptor desc = objectDescriptorMap.get(element);
		if (desc != null) {
			doRemoveDescriptor(element, desc);
			if (desc.equals(activeLaunchDesc)) {
				// Roll back to the last one and make sure we don't come back
				ILaunchDescriptor nextDesc = lastLaunchDesc;
				activeLaunchDesc = null;
				setActiveLaunchDescriptor(nextDesc);
			}
		}
	}

	@Override
	public ILaunchDescriptor[] getLaunchDescriptors() {
		ILaunchDescriptor[] descs = descriptors.values().toArray(new ILaunchDescriptor[descriptors.size()]);
		Arrays.sort(descs, new Comparator<ILaunchDescriptor>() {
			@Override
			public int compare(ILaunchDescriptor o1, ILaunchDescriptor o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return descs;
	}

	@Override
	public ILaunchDescriptor getActiveLaunchDescriptor() {
		return activeLaunchDesc;
	}

	@Override
	public void setActiveLaunchDescriptor(ILaunchDescriptor configDesc) throws CoreException {
		if (activeLaunchDesc != null && activeLaunchDesc == configDesc)
			return;
		lastLaunchDesc = activeLaunchDesc;
		activeLaunchDesc = configDesc;

		IEclipsePreferences store = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		if (activeLaunchDesc != null) {
			store.put(PREF_ACTIVE_CONFIG_DESC, activeLaunchDesc.getName());
		} else {
			store.remove(PREF_ACTIVE_CONFIG_DESC);
		}
		try {
			store.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}

		// Send notifications
		for (Listener listener : listeners) {
			listener.activeConfigurationDescriptorChanged();
		}

		if (activeLaunchDesc == null) {
			setActiveLaunchMode(null);
			setActiveLaunchTarget(null);
			return;
		}

		// Set active target
		String activeTargetId = store.node(activeLaunchDesc.getName()).get(PREF_ACTIVE_LAUNCH_TARGET, null);
		ILaunchTarget target = null;
		if (activeTargetId != null) {
			target = getLaunchTarget(activeTargetId);
		}
		if (target == null) {
			ILaunchTarget[] targets = getLaunchTargets();
			if (targets.length > 0) {
				target = targets[0];
			}
		}
		setActiveLaunchTarget(target);

		// Set active mode
		String activeModeName = store.node(activeLaunchDesc.getName()).get(PREF_ACTIVE_LAUNCH_MODE, null);
		ILaunchConfigurationType configType = getLaunchConfigurationType(activeLaunchDesc, activeLaunchTarget);
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchMode foundMode = null;
		if (activeModeName != null && configType.supportsMode(activeModeName)) {
			foundMode = launchManager.getLaunchMode(activeModeName);
		}
		if (foundMode == null && configType.supportsMode("debug")) {
			foundMode = launchManager.getLaunchMode("debug");
		}
		if (foundMode == null && configType.supportsMode("run")) {
			foundMode = launchManager.getLaunchMode("run");
		}
		setActiveLaunchMode(foundMode);
	}

	@Override
	public ILaunchMode[] getLaunchModes() throws CoreException {
		ILaunchConfigurationType configType = getLaunchConfigurationType(activeLaunchDesc, activeLaunchTarget);
		if (configType == null)
			return new ILaunchMode[0];

		List<ILaunchMode> modeList = new ArrayList<>();
		ILaunchMode[] modes = DebugPlugin.getDefault().getLaunchManager().getLaunchModes();
		for (ILaunchMode mode : modes) {
			if (configType.supportsMode(mode.getIdentifier())) {
				modeList.add(mode);
			}
		}

		return modeList.toArray(new ILaunchMode[modeList.size()]);
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

		for (Listener listener : listeners)
			listener.activeLaunchModeChanged();

		if (activeLaunchDesc == null)
			return;

		Preferences store = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(activeLaunchDesc.getName());
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
	}

	@Override
	public ILaunchTarget[] getLaunchTargets() {
		if (activeLaunchDesc == null)
			return new ILaunchTarget[0];

		List<ILaunchTarget> targetList = new ArrayList<>();
		Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(activeLaunchDesc.getType().getId());
		if (targetMap != null) {
			for (String id : targetMap.keySet()) {
				ILaunchTargetType type = targetTypes.get(id);
				if (type != null) {
					ILaunchTarget[] targets = type.getTargets();
					for (ILaunchTarget target : targets) {
						targetList.add(target);
					}
				}
			}
		}
		return targetList.toArray(new ILaunchTarget[targetList.size()]);
	}

	@Override
	public ILaunchTarget getActiveLaunchTarget() {
		return activeLaunchTarget;
	}

	@Override
	public void setActiveLaunchTarget(ILaunchTarget target) {
		if (activeLaunchTarget == target) return;

		activeLaunchTarget = target;

		for (Listener listener : listeners)
			listener.activeLaunchTargetChanged();

		if (activeLaunchDesc == null)
			return;

		Preferences store = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(activeLaunchDesc.getName());
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

		target.setActive();
	}

	@Override
	public ILaunchTarget getLaunchTarget(String id) {
		for (ILaunchTargetType type : targetTypes.values()) {
			ILaunchTarget target = type.getTarget(id);
			if (target != null)
				return target;
		}
		return null;
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		if (descriptor == null)
			return null;

		String descriptorTypeId = descriptor.getType().getId();
		Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(descriptorTypeId);
		if (targetMap != null) {
			String targetTypeId = target != null ? target.getType().getId() : defaultTargetTypes.get(descriptorTypeId);
			if (targetTypeId != null) {
				ILaunchConfigurationProvider configProvider = targetMap.get(targetTypeId);
				if (configProvider != null) {
					return configProvider.getLaunchConfigurationType(descriptor);
				}
			}
		}
		return null;
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		if (activeLaunchDesc == null || target == null)
			return null;

		Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(descriptor.getType().getId());
		if (targetMap != null) {
			ILaunchConfigurationProvider configProvider = targetMap.get(target.getType().getId());
			if (configProvider != null) {
				return configProvider.getLaunchConfiguration(descriptor);
			}
		}
		return null;
	}

	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	@Override
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		try {
			// TODO filter by launch configuration type to avoid loading plug-ins
			for (ILaunchDescriptorType descriptorType : descriptorTypes) {
				Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(descriptorType.getId());
				for (ILaunchConfigurationProvider configProvider : targetMap.values()) {
					if (configProvider.launchConfigurationAdded(configuration)) {
						return;
					}
				}
			}

			// No one claimed it, send it through the descriptorTypes
			launchObjectAdded(configuration);
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
	}

	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		try {
			// TODO filter by launch configuration type
			for (ILaunchDescriptorType descriptorType : descriptorTypes) {
				Map<String, ILaunchConfigurationProvider> targetMap = configProviders.get(descriptorType.getId());
				for (ILaunchConfigurationProvider configProvider : targetMap.values()) {
					if (configProvider.launchConfigurationRemoved(configuration)) {
						return;
					}
				}
			}

			launchObjectRemoved(configuration);
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
	}

	@Override
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		// Nothing to do on changes
	}

}
